package com.kdu.rizzlers.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingCancellationRequest {
    
    @NotNull(message = "Guest ID is required")
    private Integer guestId;
    
    @NotNull(message = "Booking ID is required")
    private Integer bookingId;
    
    // Used only for OTP verification
    private String otp;
} 