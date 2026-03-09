package com.example.drivingschool.controller;

import com.example.drivingschool.model.Registration;
import com.example.drivingschool.repository.RegistrationRepository;
import com.example.drivingschool.service.ExcelExporter;
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

    @GetMapping("/student/edit/{id}")
    public String showUpdateForm(@PathVariable("id") Long id, Model model) {
        Registration registration = registrationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid student Id:" + id));
        model.addAttribute("registration", registration);
        return "update_form";
    }

    @PostMapping("/student/update/{id}")
    public String updateStudent(@PathVariable("id") Long id,
                                @ModelAttribute("registration") Registration registration,
                                @RequestParam("imageFile") MultipartFile imageFile) throws IOException {

        // Fetch existing record to retain image if no new one is uploaded
        Registration existingRecord = registrationRepository.findById(id).orElseThrow();

        registration.setId(id);

        if (!imageFile.isEmpty()) {
            registration.setProfileImage(imageFile.getBytes());
        } else {
            // Keep the old image if a new file wasn't selected
            registration.setProfileImage(existingRecord.getProfileImage());
        }

        registrationRepository.saveAndFlush(registration);
        return "redirect:/student/" + id;
    }

    @GetMapping("/student/delete/{id}")
    public String deleteStudent(@PathVariable("id") Long id, Model model) {
        Registration registration = registrationRepository.findById(id).orElse(null);

        if (registration == null) {
            model.addAttribute("errorMessage", "Student with ID " + id + " not found!");
            return "student_details";
        }

        registrationRepository.delete(registration);
        return "redirect:/";
    }
}