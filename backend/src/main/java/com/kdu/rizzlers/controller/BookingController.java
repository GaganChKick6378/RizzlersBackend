package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.dto.BookingRequest;
import com.kdu.rizzlers.dto.BookingResponse;
import com.kdu.rizzlers.exception.ApiError;
import com.kdu.rizzlers.exception.InvalidBookingException;
import com.kdu.rizzlers.exception.OverlappingBookingException;
import com.kdu.rizzlers.exception.ResourceNotFoundException;
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

import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.util.HashMap;
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
                     content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "409", description = "Overlapping booking", 
                     content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<?> bookRoom(
            @Parameter(description = "Booking request details", required = true) 
            @Validated @RequestBody BookingRequest bookingRequest) {
        try {
            // Validate booking request
            if (bookingRequest == null) {
                throw new InvalidBookingException("Booking request cannot be null");
            }
            
            if (bookingRequest.getPropertyId() == null) {
                throw new InvalidBookingException("Property ID is required");
            }
            
            if (bookingRequest.getRoomTypeId() == null) {
                throw new InvalidBookingException("Room type ID is required");
            }
            
            // if (bookingRequest.getStartDate() == null || bookingRequest.getEndDate() == null) {
            //     throw new InvalidBookingException("Start date and end date are required");
            // }
            
            // if (bookingRequest.getStartDate().isBefore(ChronoLocalDate.from(LocalDateTime.now()))) {
            //     throw new InvalidBookingException("Start date must be in the future");
            // }
            
            // if (bookingRequest.getStartDate().isAfter(bookingRequest.getEndDate())) {
            //     throw new InvalidBookingException("Start date cannot be after end date");
            // }
            
            log.info("Received booking request for propertyId={}, roomTypeId={}, startDate={}, endDate={}",
                    bookingRequest.getPropertyId(), bookingRequest.getRoomTypeId(),
                    bookingRequest.getStartDate(), bookingRequest.getEndDate());
            
            BookingResponse response = bookingService.bookRoom(bookingRequest);
            
            if (!response.getSuccess()) {
                throw new InvalidBookingException(response.getMessage());
            }
            
            return ResponseEntity.ok(response);
        } catch (InvalidBookingException e) {
            log.warn("Invalid booking request: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("error", "Bad Request");
            response.put("message", e.getMessage());
            response.put("success", false);
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (OverlappingBookingException e) {
            log.warn("Overlapping booking: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("status", HttpStatus.CONFLICT.value());
            response.put("error", "Conflict");
            response.put("message", e.getMessage());
            response.put("success", false);
            
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } catch (Exception e) {
            log.error("Error processing booking request: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("error", "Internal Server Error");
            response.put("message", "An unexpected error occurred while processing your booking request");
            response.put("success", false);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
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
        @ApiResponse(responseCode = "404", description = "Booking not found",
                   content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<?> getBookingStatus(
            @Parameter(description = "ID of the booking to check", required = true) 
            @PathVariable Integer bookingId) {
        try {
            // This is a placeholder for future implementation
            if (bookingId == null || bookingId <= 0) {
                throw new InvalidBookingException("Valid booking ID is required");
            }
            
            log.info("Checking status for booking ID: {}", bookingId);
            
            // For now, just return a placeholder message
            // In a real implementation, you'd query the database and throw ResourceNotFoundException if not found
            boolean bookingExists = true; // This would be a real check in the future
            
            if (!bookingExists) {
                throw new ResourceNotFoundException("Booking with ID " + bookingId + " not found");
            }
            
            return ResponseEntity.ok("Booking status check for ID " + bookingId + " is not implemented yet");
        } catch (InvalidBookingException e) {
            log.warn("Invalid booking ID: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("error", "Bad Request");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (ResourceNotFoundException e) {
            log.warn("Booking not found: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("status", HttpStatus.NOT_FOUND.value());
            response.put("error", "Not Found");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            log.error("Error checking booking status: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("error", "Internal Server Error");
            response.put("message", "An unexpected error occurred while checking the booking status");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
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
        @ApiResponse(responseCode = "404", description = "Booking payment information not found",
                   content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<?> getPaymentInfo(
            @Parameter(description = "ID of the booking to retrieve payment for", required = true) 
            @PathVariable Integer bookingId) {
        try {
            if (bookingId == null || bookingId <= 0) {
                throw new InvalidBookingException("Valid booking ID is required");
            }
            
            log.info("Retrieving payment information for booking ID: {}", bookingId);
            
            Optional<Map<String, Object>> paymentInfo = ((BookingServiceImpl) bookingService).getDecryptedPaymentInfo(bookingId);
            
            if (paymentInfo.isEmpty()) {
                throw new ResourceNotFoundException("Payment information for booking ID " + bookingId + " not found");
            }
            
            return ResponseEntity.ok(paymentInfo.get());
        } catch (InvalidBookingException e) {
            log.warn("Invalid booking ID: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("error", "Bad Request");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (ResourceNotFoundException e) {
            log.warn("Payment information not found: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("status", HttpStatus.NOT_FOUND.value());
            response.put("error", "Not Found");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            log.error("Error retrieving payment information: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("error", "Internal Server Error");
            response.put("message", "An unexpected error occurred while retrieving payment information");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
} 