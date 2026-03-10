package com.example.drivingschool.controller;

import com.example.drivingschool.model.Registration;
import com.example.drivingschool.repository.RegistrationRepository;
import com.example.drivingschool.service.ExcelExporter;
import com.example.drivingschool.service.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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
    public String registerStudent(@ModelAttribute("registration") Registration registration,
                                  @RequestParam("imageFile") MultipartFile imageFile) throws IOException {

        log.info("registration request received: {}", registration);

        if (!imageFile.isEmpty()) {
            registration.setProfileImage(imageFile.getBytes());
        }

        registrationRepository.saveAndFlush(registration);
        String smsMessage = "Thanks Mr/Mrs"+ registration.getFirstName() +
                "for joining Vishwakannada driving school your id is : " +
                registration.getId() +"\n Have a nice learning Thanks";
        smsService.sendSms(registration.getPhone(),smsMessage);
        log.info("saved successfully");
        return "redirect:/success";
    }

    @GetMapping("/success")
    public String registrationSuccess() {
        return "success_page";
    }

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

        // Convert byte array to Base64 for display in HTML
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

        // Fetch existing record to retain image if no new one is uploaded
        Registration existingRecord = registrationRepository.findByPhone(phone).orElseThrow();

        registration.setId(existingRecord.getId());

        if (!imageFile.isEmpty()) {
            registration.setProfileImage(imageFile.getBytes());
        } else {
            // Keep the old image if a new file wasn't selected
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