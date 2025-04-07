package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.dto.BookingCancellationRequest;
import com.kdu.rizzlers.dto.BookingCancellationResponse;
import com.kdu.rizzlers.service.BookingCancellationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling booking cancellation requests
 */
@RestController
@RequestMapping("/bookings/cancellation")
@RequiredArgsConstructor
@Slf4j
public class BookingCancellationController {

    private final BookingCancellationService bookingCancellationService;
    
    /**
     * Initiate booking cancellation by sending an OTP
     * 
     * @param request The booking cancellation request
     * @return Response with OTP status
     */
    @PostMapping("/request")
    public ResponseEntity<BookingCancellationResponse> requestCancellation(
            @Valid @RequestBody BookingCancellationRequest request) {
        log.info("Received cancellation request for booking ID: {}, guest ID: {}", 
                request.getBookingId(), request.getGuestId());
        
        BookingCancellationResponse response = bookingCancellationService.requestCancellation(
                request.getGuestId(), request.getBookingId());
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Verify OTP and cancel booking
     * 
     * @param request The booking cancellation request with OTP
     * @return Response with cancellation details
     */
    @PostMapping("/verify")
    public ResponseEntity<BookingCancellationResponse> verifyCancellation(
            @Valid @RequestBody BookingCancellationRequest request) {
        log.info("Received OTP verification for booking ID: {}, guest ID: {}", 
                request.getBookingId(), request.getGuestId());
        
        if (request.getOtp() == null || request.getOtp().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                    BookingCancellationResponse.error("OTP is required"));
        }
        
        BookingCancellationResponse response = bookingCancellationService.verifyCancellation(
                request.getGuestId(), request.getBookingId(), request.getOtp());
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
} 