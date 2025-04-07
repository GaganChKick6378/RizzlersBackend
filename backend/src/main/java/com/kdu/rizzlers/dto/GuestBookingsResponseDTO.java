package com.kdu.rizzlers.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for guest bookings
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GuestBookingsResponseDTO {
    
    private boolean success;
    private String message;
    
    @Builder.Default
    private boolean otpSent = false;
    
    private List<GuestBookingDTO> bookings;
    
    /**
     * Create a success response with bookings
     */
    public static GuestBookingsResponseDTO success(List<GuestBookingDTO> bookings) {
        return GuestBookingsResponseDTO.builder()
                .success(true)
                .message("Bookings retrieved successfully")
                .bookings(bookings)
                .build();
    }
    
    /**
     * Create an error response
     */
    public static GuestBookingsResponseDTO error(String message) {
        return GuestBookingsResponseDTO.builder()
                .success(false)
                .message(message)
                .build();
    }
    
    /**
     * Create a response indicating that OTP was sent
     */
    public static GuestBookingsResponseDTO otpSent() {
        return GuestBookingsResponseDTO.builder()
                .success(true)
                .message("OTP has been sent to your email")
                .otpSent(true)
                .build();
    }
} 