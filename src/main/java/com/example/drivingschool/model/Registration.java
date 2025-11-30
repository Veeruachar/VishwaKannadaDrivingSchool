package com.example.drivingschool.model;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "registrations_data")
@Data
public class Registration {

	@Id
	@Column(name = "id")
	private Long id;

	@Column(name = "first_name")
	private String firstName;

	@Column(name = "address")
	private String address;

	@Column(name = "phone")
	private String phone;

	@Column(name = "course_type")
	private String courseType;

	@Column(name = "dlnumber")
	private String dlnumber;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@Column(name = "admission_date")
	private LocalDate admissionDate;
}