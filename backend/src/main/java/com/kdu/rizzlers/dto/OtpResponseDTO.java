package com.kdu.rizzlers.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OtpResponseDTO {
    
    private Boolean success;
    private String message;
    
    // Session-related fields, only included when OTP is successfully verified
    private String sessionToken;
    private ZonedDateTime sessionExpiryTime;
    
    // OTP-related fields
    private Integer otpExpiryMinutes;
    private ZonedDateTime otpExpiryTime;
    private Integer remainingAttempts;
} 