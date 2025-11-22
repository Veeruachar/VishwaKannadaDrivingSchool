package com.example.drivingschool.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "registrations_data")
@Data
public class Registration {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "first_name")
	private String firstName;

	@Column(name = "address")
	private String address;

	@Column(name = "email")
	private String email;

	@Column(name = "phone")
	private String phone;

	@Column(name = "course_type")
	private String courseType;

	@Column(name = "dlnumber")
	private String dlnumber;
}