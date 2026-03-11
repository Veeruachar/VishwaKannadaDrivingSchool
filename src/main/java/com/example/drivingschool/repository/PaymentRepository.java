package com.example.drivingschool.repository;

import com.example.drivingschool.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // Basic CRUD operations are handled by JpaRepository
}
