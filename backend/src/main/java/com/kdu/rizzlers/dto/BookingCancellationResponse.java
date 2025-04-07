package com.kdu.rizzlers.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingCancellationResponse {
    private boolean success;
    private String message;
    
    // Response fields when booking is successfully cancelled
    private Integer bookingId;
    private Integer statusId;
    private Integer propertyId;
    private Integer guestId;
    private String status;
    
    // Static factory methods for common response types
    public static BookingCancellationResponse otpSent() {
        return BookingCancellationResponse.builder()
                .success(true)
                .message("Cancellation OTP has been sent to your email")
                .build();
    }
    
    public static BookingCancellationResponse error(String message) {
        return BookingCancellationResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
} 