package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.dto.BookingRequest;
import com.kdu.rizzlers.dto.BookingResponse;
import com.kdu.rizzlers.service.BookingService;
import com.kdu.rizzlers.service.impl.BookingServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * REST controller for handling room booking operations
 */
@RestController
@RequestMapping("/booking")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Booking Management", description = "APIs for managing bookings")
public class BookingController {

    private final BookingService bookingService;

    /**
     * Endpoint for booking a room
     * 
     * @param bookingRequest the booking request details
     * @return ResponseEntity containing booking response or error
     */
    @PostMapping
    @Operation(summary = "Book a room", description = "Creates a new booking for the specified room and date range")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Booking successful", 
                     content = @Content(schema = @Schema(implementation = BookingResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid booking request", 
                     content = @Content(schema = @Schema(implementation = BookingResponse.class)))
    })
    public ResponseEntity<BookingResponse> bookRoom(
            @Parameter(description = "Booking request details", required = true) 
            @Validated @RequestBody BookingRequest bookingRequest) {
        log.info("Received booking request for propertyId={}, roomTypeId={}, startDate={}, endDate={}",
                bookingRequest.getPropertyId(), bookingRequest.getRoomTypeId(),
                bookingRequest.getStartDate(), bookingRequest.getEndDate());
        
        BookingResponse response = bookingService.bookRoom(bookingRequest);
        
        if (!response.getSuccess()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint to check the status of a booking
     * 
     * @param bookingId the booking ID to check
     * @return ResponseEntity containing booking details or not found
     */
    @GetMapping("/{bookingId}")
    @Operation(summary = "Get booking status", description = "Retrieves the status of a booking by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Booking status found"),
        @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public ResponseEntity<String> getBookingStatus(
            @Parameter(description = "ID of the booking to check", required = true) 
            @PathVariable Integer bookingId) {
        // This is a placeholder for future implementation
        log.info("Checking status for booking ID: {}", bookingId);
        return ResponseEntity.ok("Booking status check for ID " + bookingId + " is not implemented yet");
    }

    /**
     * Endpoint to get payment information for a booking (admin only)
     * 
     * @param bookingId the booking ID
     * @return ResponseEntity containing payment information or not found
     */
    @GetMapping("/{bookingId}/payment")
    @Operation(summary = "Get payment information", description = "Retrieves payment details for a booking (admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment information retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Booking payment information not found")
    })
    public ResponseEntity<?> getPaymentInfo(
            @Parameter(description = "ID of the booking to retrieve payment for", required = true) 
            @PathVariable Integer bookingId) {
        log.info("Retrieving payment information for booking ID: {}", bookingId);
        
        Optional<Map<String, Object>> paymentInfo = ((BookingServiceImpl) bookingService).getDecryptedPaymentInfo(bookingId);
        
        if (paymentInfo.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(paymentInfo.get());
    }
} 