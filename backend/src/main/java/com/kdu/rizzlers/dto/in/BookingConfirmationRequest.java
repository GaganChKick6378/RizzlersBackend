package com.kdu.rizzlers.dto.in;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for booking confirmation details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingConfirmationRequest {
    
    @NotNull(message = "Booking ID is required")
    private Integer bookingId;
    
    @NotNull(message = "Guest ID is required")
    private Integer guestId;
} 