package com.example.drivingschool.repository;

import com.example.drivingschool.model.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    // Custom query methods can be added here if needed

    Optional<Registration> findByPhone(String phone);
}