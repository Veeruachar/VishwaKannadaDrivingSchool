package com.example.drivingschool.controller;

import com.example.drivingschool.model.Attendance;
import com.example.drivingschool.model.Payment;
import com.example.drivingschool.model.Registration;
import com.example.drivingschool.repository.AttendanceRepository;
import com.example.drivingschool.repository.PaymentRepository;
import com.example.drivingschool.repository.RegistrationRepository;
import com.example.drivingschool.service.ExcelExporter;
import com.example.drivingschool.service.SmsService;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.awt.Color;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

@Controller
@Slf4j
public class RegistrationController {

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private SmsService smsService;

    @GetMapping("/")
    public String showIndexPage() {
        return "index";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        log.info("registration get request");
        model.addAttribute("registration", new Registration());
        return "registration_form";
    }

    @PostMapping("/register")
    @Transactional
    public String registerStudent(@ModelAttribute("registration") Registration registration,
                                  @RequestParam("imageFile") MultipartFile imageFile,
                                  @RequestParam(value = "initialPayment", required = false) BigDecimal initialPayment,
                                  @RequestParam(value = "paymentMode", defaultValue = "Cash") String paymentMode,
                                  Model model) throws IOException {

        if (registrationRepository.findByPhone(registration.getPhone()).isPresent()) {
            log.warn("Duplicate registration attempt for phone: {}", registration.getPhone());
            model.addAttribute("errorMessage", "A student with this phone number is already registered!");
            return "registration_form"; // Stays on the form with user input preserved
        }

        if (!imageFile.isEmpty()) {
            registration.setProfileImage(imageFile.getBytes());
        }
        Registration savedReg = registrationRepository.saveAndFlush(registration);

        if (initialPayment != null && initialPayment.compareTo(BigDecimal.ZERO) > 0) {
            Payment payment = new Payment();
            payment.setAmountPaid(initialPayment.doubleValue());
            payment.setPaymentMode(paymentMode);
            payment.setRegistration(savedReg);
            payment.setRemarks("Initial payment at registration");
            paymentRepository.save(payment);
        }

        // 3. Safely process SMS
        try {
            int bal = registration.getTotalFees().intValue() - initialPayment.intValue();
            String cleanPhone = sanitizePhoneNumber(registration.getPhone());
            String smsMessage = "Thanks " + registration.getFirstName() +
                    ", you're registered at Vishwakannada driving school! " +
                    "Your ID is: " + registration.getId() + " \n you paid : " + initialPayment.toString() + "balance to be paid is : " + bal;
            smsService.sendSms(cleanPhone, smsMessage);
        } catch (Exception e) {
            log.error("SMS failed to send : {}", e.getMessage());
        }

        return "redirect:/success";
    }

    private String sanitizePhoneNumber(String phone) {
        if (phone.startsWith("+")) return phone;
        return "+91" + phone;
    }

    @GetMapping("/success")
    public String registrationSuccess() {
        return "success_page";
    }

    // ... (rest of your existing methods remain the same)

    @GetMapping("/export/excel")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=registrations_" + System.currentTimeMillis() + ".xlsx";
        response.setHeader(headerKey, headerValue);

        List<Registration> registrations = registrationRepository.findAll();
        ExcelExporter exporter = new ExcelExporter(registrations);
        exporter.export(response);
    }

    @GetMapping("/student/{phone}")
    public String getStudentDetails(@PathVariable("phone") String phone, Model model) {
        Registration reg = registrationRepository.findByPhone(phone).orElse(null);

        if (reg == null) {
            model.addAttribute("errorMessage", "Student with phone " + phone + " not found!");
            return "student_details";
        }

        // 1. Calculate Financials (Normal Java Loop)
        BigDecimal totalPaid = BigDecimal.ZERO;
        if (reg.getPayments() != null) {
            for (Payment p : reg.getPayments()) {
                if (p.getAmountPaid() != null) totalPaid = totalPaid.add(BigDecimal.valueOf(p.getAmountPaid()));
            }
        }
        BigDecimal balance = (reg.getTotalFees() != null ? reg.getTotalFees() : BigDecimal.ZERO).subtract(totalPaid);

        // 2. Calculate Attendance Progress
        int classesTaken = (reg.getAttendances() != null) ? reg.getAttendances().size() : 0;
        int totalClassesAllowed = 30; // Assuming 30 classes standard
        int remainingClasses = totalClassesAllowed - classesTaken;

        // 3. Prepare Model
        model.addAttribute("registration", reg);
        model.addAttribute("totalPaid", totalPaid);
        model.addAttribute("balance", balance);
        model.addAttribute("classesTaken", classesTaken);
        model.addAttribute("remainingClasses", Math.max(0, remainingClasses));

        if (reg.getProfileImage() != null) {
            model.addAttribute("imageData", Base64.getEncoder().encodeToString(reg.getProfileImage()));
        }

        return "student_details";
    }

    @PostMapping("/student/add-payment")
    @Transactional
    public String addPayment(@RequestParam("registrationId") Long registrationId,
                             @RequestParam("amountPaid") BigDecimal amountPaid,
                             @RequestParam("paymentMode") String paymentMode,
                             @RequestParam("remarks") String remarks) {

        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Student ID"));

        Payment payment = new Payment();
        payment.setAmountPaid(amountPaid.doubleValue());
        payment.setPaymentMode(paymentMode);
        payment.setRemarks(remarks);
        payment.setRegistration(registration);

        paymentRepository.save(payment);
        return "redirect:/student/" + registration.getPhone();
    }

    @GetMapping("/student/receipt/{phone}")
    public void downloadReceipt(@PathVariable("phone") String phone, HttpServletResponse response) throws IOException {
        Registration reg = registrationRepository.findByPhone(phone)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        BigDecimal totalPaid = BigDecimal.ZERO;
        for (Payment p : reg.getPayments()) {
            if (p.getAmountPaid() != null) totalPaid = totalPaid.add(BigDecimal.valueOf(p.getAmountPaid()));
        }

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=Receipt_" + phone + ".pdf");

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        // 1. Decorative Header
        Font schoolNameFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, Color.BLUE);
        Paragraph schoolName = new Paragraph("VISHWA KANNADA DRIVING SCHOOL", schoolNameFont);
        schoolName.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(schoolName);

        Font contactFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Paragraph contactInfo = new Paragraph("Official Payment Receipt | Manvi,pin:584123 Karnataka\nContact: +91 90000 00000", contactFont);
        contactInfo.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(contactInfo);

        document.add(new Paragraph("\n"));
        document.add(new LineSeparator());
        document.add(new Paragraph("\n"));

        // 2. Receipt Info
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Paragraph receiptTitle = new Paragraph("PAYMENT RECEIPT", titleFont);
        receiptTitle.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(receiptTitle);
        document.add(new Paragraph("\n"));

        // 3. Info Table
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);

        String billId  = reg.getAdmissionDate().getYear() + "/" + reg.getId() + "/"+ reg.getPayments().get(0).getId();
        addTableCell(table, "Bill id:", billId, true);
        addTableCell(table, "Student Name:", reg.getFirstName(), true);
        addTableCell(table, "Phone Number:", reg.getPhone(), true);
        addTableCell(table, "Course:", reg.getCourseType(), true);
        addTableCell(table, "Total Fees:", "Rs. " + reg.getTotalFees(), true);
        addTableCell(table, "Total Paid:", "Rs. " + totalPaid, true);
        addTableCell(table, "Payment Status:", "FULLY PAID", true);

        document.add(table);

        // 4. History Table
        document.add(new Paragraph("\nPayment Breakdown:", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
        PdfPTable historyTable = new PdfPTable(3);
        historyTable.setWidthPercentage(100);
        historyTable.setSpacingBefore(10f);

        historyTable.addCell("Date");
        historyTable.addCell("Mode");
        historyTable.addCell("Amount");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        for (Payment p : reg.getPayments()) {
            historyTable.addCell(p.getPaymentDate().format(formatter));
            historyTable.addCell(p.getPaymentMode());
            historyTable.addCell("Rs. " + p.getAmountPaid());
        }
        document.add(historyTable);

        // 5. Signature Area
        document.add(new Paragraph("\n\n\n\n"));
        Paragraph signature = new Paragraph("__________________________\nAuthorized Signatory", contactFont);
        signature.setAlignment(Paragraph.ALIGN_RIGHT);
        document.add(signature);

        document.close();
    }

    private void addTableCell(PdfPTable table, String label, String value, boolean bold) {
        Font font = bold ? FontFactory.getFont(FontFactory.HELVETICA_BOLD) : FontFactory.getFont(FontFactory.HELVETICA);
        table.addCell(new Phrase(label, font));
        table.addCell(new Phrase(value));
    }

    @GetMapping("/student/edit/{phone}")
    public String showUpdateForm(@PathVariable("phone") String phone, Model model) {
        Registration registration = registrationRepository.findByPhone(phone)
                .orElseThrow(() -> new IllegalArgumentException("Invalid student Id:" + phone));
        model.addAttribute("registration", registration);
        return "update_form";
    }

    @PostMapping("/student/update/{phone}")
    public String updateStudent(@PathVariable("phone") String phone,
                                @ModelAttribute("registration") Registration registration,
                                @RequestParam("imageFile") MultipartFile imageFile) throws IOException {

        Registration existingRecord = registrationRepository.findByPhone(phone).orElseThrow();
        registration.setId(existingRecord.getId());

        if (!imageFile.isEmpty()) {
            registration.setProfileImage(imageFile.getBytes());
        } else {
            registration.setProfileImage(existingRecord.getProfileImage());
        }

        registration.setPhone(existingRecord.getPhone());
        registration.setTotalFees(existingRecord.getTotalFees());

        registrationRepository.saveAndFlush(registration);
        return "redirect:/student/" + phone;
    }

    @GetMapping("/student/delete/{phone}")
    public String deleteStudent(@PathVariable("phone") String phone, Model model) {
        Registration registration = registrationRepository.findByPhone(phone).orElse(null);

        if (registration == null) {
            model.addAttribute("errorMessage", "Student with Phone " + phone + " not found!");
            return "student_details";
        }

        registrationRepository.delete(registration);
        return "redirect:/";
    }

    @PostMapping("/student/add-attendance")
    @Transactional
    public String addAttendance(@RequestParam("registrationId") Long regId,
                                @RequestParam("topic") String topic) {
        Registration reg = registrationRepository.findById(regId).orElseThrow();
        Attendance att = new Attendance();
        att.setRegistration(reg);
        att.setDate(LocalDate.now());
        att.setStatus("Present");
        att.setTopicCovered(topic);
        attendanceRepository.save(att);
        return "redirect:/student/" + reg.getPhone();
    }
}