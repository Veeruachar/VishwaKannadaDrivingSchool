package com.example.drivingschool.model;

import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "registrations_data")
@Data
public class Registration {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "first_name")
	private String firstName;

	@Column(name = "address")
	private String address;

	@Column(name = "phone")
	@NotBlank(message = "Phone number is required")
	@Pattern(regexp = "^\\d{10}$", message = "Phone number must be 10 digits")
	private String phone;

	@Column(name = "course_type")
	private String courseType;

	@Column(name = "dlnumber")
	private String dlnumber;

	@Column(name = "total_fees")
	private BigDecimal totalFees;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@Column(name = "admission_date")
	private LocalDate admissionDate;

	@Lob
	@Column(name = "profile_image", columnDefinition = "LONGBLOB")
	private byte[] profileImage;

	// Relationship to Payment Table
	@OneToMany(mappedBy = "registration", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private List<Payment> payments;
}