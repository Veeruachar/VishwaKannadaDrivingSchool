package com.example.drivingschool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class DrivingSchoolApplication {

	public static void main(String[] args) {
		SpringApplication.run(DrivingSchoolApplication.class, args);
	}

}
