package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.dto.GuestBookingsResponseDTO;
import com.kdu.rizzlers.dto.MyBookingsOtpRequestDTO;
import com.kdu.rizzlers.dto.MyBookingsOtpVerificationDTO;
import com.kdu.rizzlers.service.GuestBookingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Guest Bookings", description = "APIs for managing guest bookings")
public class GuestBookingsController {
    
    private final GuestBookingsService guestBookingsService;
    
    /**
     * Request an OTP for accessing "My Bookings"
     * 
     * @param request DTO containing email and property ID
     * @return Response DTO with status information
     */
    @PostMapping("/request-otp")
    @Operation(summary = "Request OTP", description = "Request an OTP for accessing My Bookings")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OTP request processed",
                    content = @Content(schema = @Schema(implementation = GuestBookingsResponseDTO.class)))
    })
    public ResponseEntity<GuestBookingsResponseDTO> requestOtp(
            @Parameter(description = "Request containing email and property ID", required = true)
            @Valid @RequestBody MyBookingsOtpRequestDTO request) {
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
    @Operation(summary = "Verify OTP and get bookings", 
              description = "Verify OTP and retrieve bookings for a guest")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OTP verification processed",
                    content = @Content(schema = @Schema(implementation = GuestBookingsResponseDTO.class)))
    })
    public ResponseEntity<GuestBookingsResponseDTO> verifyOtpAndGetBookings(
            @Parameter(description = "Request containing email, OTP, and property ID", required = true)
            @Valid @RequestBody MyBookingsOtpVerificationDTO request) {
        log.info("Received OTP verification request from email: {}, propertyId: {}", 
                request.getEmail(), request.getPropertyId());
        
        GuestBookingsResponseDTO response = guestBookingsService.verifyOtpAndGetBookings(request);
        
        return ResponseEntity.ok(response);
    }
}