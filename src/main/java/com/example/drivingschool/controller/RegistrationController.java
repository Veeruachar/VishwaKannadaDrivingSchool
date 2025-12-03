package com.example.drivingschool.controller;

import com.example.drivingschool.model.Registration;
import com.example.drivingschool.repository.RegistrationRepository;
import com.example.drivingschool.service.ExcelExporter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Controller
@Slf4j
public class RegistrationController {

    @Autowired
    private RegistrationRepository registrationRepository;

    // 0. NEW: Display the Index Page
    @GetMapping("/")
    public String showIndexPage() {
        return "index"; // refers to src/main/resources/templates/index.html
    }

    // 1. Display the Registration Form
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        log.info("registration get request");

        // Pass an empty Registration object to the form for data binding
        model.addAttribute("registration", new Registration());
        return "registration_form"; // Refers to src/main/resources/templates/registration_form.html
    }

    // 2. Handle Form Submission
    @PostMapping("/register")
    public String registerStudent(@ModelAttribute("registration") Registration registration) {

        log.info("registration request recieved : {}", registration);
        // Save the received data to the MySQL database
        registrationRepository.saveAndFlush(registration);

        log.info("saved successfuly");

        // Redirect to a success page
        return "redirect:/success";
    }

    // 3. Success Page
    @GetMapping("/success")
    public String registrationSuccess() {
        return "success_page"; // Refers to src/main/resources/templates/success_page.html
    }

    @GetMapping("/export/excel")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        // Set the response headers for an Excel file download
        response.setContentType("application/octet-stream");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=registrations_" + System.currentTimeMillis() + ".xlsx";
        response.setHeader(headerKey, headerValue);

        // Get all data from the database
        List<Registration> registrations = registrationRepository.findAll();

        // Create the Excel file and write data (see step 3)
        ExcelExporter exporter = new ExcelExporter(registrations);
        exporter.export(response);
    }

    // 4. Get Particular Student Details
    // Search by ID
    @GetMapping("/student/{id}")
    public String getStudentDetails(@PathVariable("id") Long id, Model model) {
        Registration registration = registrationRepository.findById(id).orElse(null);

        log.info("Registrations : {}",registration);
        if (registration == null) {
            model.addAttribute("errorMessage", "Student with ID " + id + " not found!");
            return "student_details"; // still return the same page
        }

        model.addAttribute("registration", registration);
        return "student_details";
    }


    // 5. Show Update Form
    @GetMapping("/student/edit/{id}")
    public String showUpdateForm(@PathVariable("id") Long id, Model model) {
        Registration registration = registrationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid student Id:" + id));
        model.addAttribute("registration", registration);
        return "update_form"; // src/main/resources/templates/update_form.html
    }

    // 6. Handle Update Submission
    @PostMapping("/student/update/{id}")
    public String updateStudent(@PathVariable("id") Long id,
                                @ModelAttribute("registration") Registration registration) {
        registration.setId(id); // ensure ID is set
        registrationRepository.saveAndFlush(registration);
        return "redirect:/student/" + id;
    }

    // 7. Delete Student
    @GetMapping("/student/delete/{id}")
    public String deleteStudent(@PathVariable("id") Long id,  Model model) {
        Registration registration = registrationRepository.findById(id).orElse(null);

        if (registration == null) {
            model.addAttribute("errorMessage", "Student with ID " + id + " not found!");
            return "student_details"; // still return the same page
        }

        model.addAttribute("registration", registration);
        registrationRepository.delete(registration);
        return "redirect:/"; // back to index after deletion
    }
}
