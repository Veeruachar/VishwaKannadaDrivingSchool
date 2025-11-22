package com.example.drivingschool.repository;

import com.example.drivingschool.model.Registration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    // Custom query methods can be added here if needed
}