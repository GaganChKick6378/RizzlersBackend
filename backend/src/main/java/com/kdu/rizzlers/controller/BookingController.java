package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.dto.BookingRequest;
import com.kdu.rizzlers.dto.BookingResponse;
import com.kdu.rizzlers.service.BookingService;
import com.kdu.rizzlers.service.impl.BookingServiceImpl;
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
public class BookingController {

    private final BookingService bookingService;

    /**
     * Endpoint for booking a room
     * 
     * @param bookingRequest the booking request details
     * @return ResponseEntity containing booking response or error
     */
    @PostMapping
    public ResponseEntity<BookingResponse> bookRoom(@Validated @RequestBody BookingRequest bookingRequest) {
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
    public ResponseEntity<String> getBookingStatus(@PathVariable Integer bookingId) {
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
    public ResponseEntity<?> getPaymentInfo(@PathVariable Integer bookingId) {
        log.info("Retrieving payment information for booking ID: {}", bookingId);
        
        Optional<Map<String, Object>> paymentInfo = ((BookingServiceImpl) bookingService).getDecryptedPaymentInfo(bookingId);
        
        if (paymentInfo.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(paymentInfo.get());
    }
} 