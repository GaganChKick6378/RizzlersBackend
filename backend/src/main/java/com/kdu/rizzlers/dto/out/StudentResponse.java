package com.kdu.rizzlers.dto.out;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String enrollmentNumber;
    private Integer yearOfStudy;
    private BigDecimal cgpa;
    private String department;
    private LocalDate dateOfBirth;
    private String mobileNumber;
    private String address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 