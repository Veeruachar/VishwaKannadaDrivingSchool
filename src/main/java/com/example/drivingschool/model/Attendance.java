package com.example.drivingschool.model;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "attendance")
@Data
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "attendance_date")
    private LocalDate date;

    @Column(name = "status")
    private String status; // e.g., Present, Absent

    @Column(name = "topic_covered")
    private String topicCovered; // e.g., Steering control, Reversing

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registration_id")
    private Registration registration;

    @PrePersist
    protected void onCreate() {
        if (this.date == null) this.date = LocalDate.now();
    }
}