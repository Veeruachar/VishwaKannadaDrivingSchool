package com.example.drivingschool.controller;

import com.example.drivingschool.model.Registration;
import com.example.drivingschool.repository.RegistrationRepository;
import com.example.drivingschool.service.ExcelExporter;
import com.example.drivingschool.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Controller
@Slf4j
public class RegistrationController {

    @Autowired
    private RegistrationRepository registrationRepository;

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
                                  Model model) throws IOException {

        // 1. Check for Duplicate
        if (registrationRepository.findByPhone(registration.getPhone()).isPresent()) {
            log.warn("Duplicate registration attempt for phone: {}", registration.getPhone());
            model.addAttribute("errorMessage", "A student with this phone number is already registered!");
            return "registration_form"; // Stays on the form with user input preserved
        }

        // 2. Save to Database
        if (!imageFile.isEmpty()) {
            registration.setProfileImage(imageFile.getBytes());
        }
        registrationRepository.saveAndFlush(registration);

        // 3. Safely process SMS
        try {
            String cleanPhone = sanitizePhoneNumber(registration.getPhone());
            String smsMessage = "Thanks " + registration.getFirstName() +
                    ", you're registered at Vishwakannada driving school! " +
                    "Your ID is: " + registration.getId();
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
        Registration registration = registrationRepository.findByPhone(phone).orElse(null);

        if (registration == null) {
            model.addAttribute("errorMessage", "Student with phone " + phone + " not found!");
            return "student_details";
        }

        if (registration.getProfileImage() != null) {
            String base64Image = Base64.getEncoder().encodeToString(registration.getProfileImage());
            model.addAttribute("imageData", base64Image);
        }

        model.addAttribute("registration", registration);
        return "student_details";
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
}