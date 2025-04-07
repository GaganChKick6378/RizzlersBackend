package com.kdu.rizzlers.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for requesting an OTP to view bookings
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyBookingsOtpRequestDTO {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    @Builder.Default
    private Integer propertyId = 10; // Default property ID if not specified
} 