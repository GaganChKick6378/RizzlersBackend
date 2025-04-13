package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.dto.BookingCancellationRequest;
import com.kdu.rizzlers.dto.BookingCancellationResponse;
import com.kdu.rizzlers.service.BookingCancellationService;
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
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling booking cancellation requests
 */
@RestController
@RequestMapping("/bookings/cancellation")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Booking Cancellation", description = "APIs for booking cancellation operations")
public class BookingCancellationController {

    private final BookingCancellationService bookingCancellationService;
    
    /**
     * Initiate booking cancellation by sending an OTP
     * 
     * @param request The booking cancellation request
     * @return Response with OTP status
     */
    @PostMapping("/request")
    @Operation(summary = "Request cancellation", description = "Initiate booking cancellation by sending an OTP")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OTP sent successfully",
                     content = @Content(schema = @Schema(implementation = BookingCancellationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request",
                     content = @Content(schema = @Schema(implementation = BookingCancellationResponse.class)))
    })
    public ResponseEntity<BookingCancellationResponse> requestCancellation(
            @Parameter(description = "Booking cancellation request with booking ID and guest ID", required = true)
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
    @Operation(summary = "Verify cancellation", description = "Verify OTP and complete booking cancellation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Booking cancelled successfully",
                     content = @Content(schema = @Schema(implementation = BookingCancellationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid OTP or request",
                     content = @Content(schema = @Schema(implementation = BookingCancellationResponse.class)))
    })
    public ResponseEntity<BookingCancellationResponse> verifyCancellation(
            @Parameter(description = "Booking cancellation request with OTP", required = true)
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
    
    /**
     * Cancel booking directly for authenticated users with Cognito tokens
     * 
     * @param request The booking cancellation request with ID token
     * @return Response with cancellation details
     */
    @PostMapping("/authenticated")
    @Operation(summary = "Authenticated cancellation", description = "Cancel booking directly for authenticated users with Cognito ID token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Booking cancelled successfully",
                     content = @Content(schema = @Schema(implementation = BookingCancellationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid token or request",
                     content = @Content(schema = @Schema(implementation = BookingCancellationResponse.class))),
        @ApiResponse(responseCode = "401", description = "Authentication failed",
                     content = @Content(schema = @Schema(implementation = BookingCancellationResponse.class)))
    })
    public ResponseEntity<BookingCancellationResponse> authenticatedCancellation(
            @Parameter(description = "Booking cancellation request with ID token", required = true)
            @Valid @RequestBody BookingCancellationRequest request) {
        log.info("Received authenticated cancellation request for booking ID: {}, guest ID: {}", 
                request.getBookingId(), request.getGuestId());
        
        if (request.getIdToken() == null || request.getIdToken().trim().isEmpty()) {
            return ResponseEntity.status(401).body(
                    BookingCancellationResponse.error("ID token is required"));
        }
        
        BookingCancellationResponse response = bookingCancellationService.authenticatedCancellation(
                request.getGuestId(), request.getBookingId(), request.getIdToken());
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else if (response.getMessage() != null && response.getMessage().contains("Authentication failed")) {
            return ResponseEntity.status(401).body(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
} 