package com.example.drivingschool.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class Registration {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "first_name")
	private String firstName;

	@Column(name = "last_name")
	private String lastName;

	@Column(name = "email")
	private String email;

	@Column(name = "phone")
	private String phone;

	@Column(name = "course_type")
	private String courseType; // e.g., 'Basic', 'Advanced', 'Manual'

	// Getters and Setters (omitted for brevity)
	// You can use Lombok for this or let your IDE generate them
}