package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.dto.GuestBookingsResponseDTO;
import com.kdu.rizzlers.dto.MyBookingsOtpRequestDTO;
import com.kdu.rizzlers.dto.MyBookingsOtpVerificationDTO;
import com.kdu.rizzlers.service.GuestBookingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for handling guest bookings requests
 */
@RestController
@RequestMapping("/guest-bookings")
@RequiredArgsConstructor
@Slf4j
public class GuestBookingsController {
    
    private final GuestBookingsService guestBookingsService;
    
    /**
     * Request an OTP for accessing "My Bookings"
     * 
     * @param request DTO containing email and property ID
     * @return Response DTO with status information
     */
    @PostMapping("/request-otp")
    public ResponseEntity<GuestBookingsResponseDTO> requestOtp(@Valid @RequestBody MyBookingsOtpRequestDTO request) {
        log.info("Received request for OTP from email: {}, propertyId: {}", request.getEmail(), request.getPropertyId());
        
        GuestBookingsResponseDTO response = guestBookingsService.requestOtp(request);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Verify OTP and retrieve bookings
     * 
     * @param request DTO containing email, OTP, and property ID
     * @return Response DTO with bookings list or error message
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<GuestBookingsResponseDTO> verifyOtpAndGetBookings(
            @Valid @RequestBody MyBookingsOtpVerificationDTO request) {
        log.info("Received OTP verification request from email: {}, propertyId: {}", 
                request.getEmail(), request.getPropertyId());
        
        GuestBookingsResponseDTO response = guestBookingsService.verifyOtpAndGetBookings(request);
        
        return ResponseEntity.ok(response);
    }
}