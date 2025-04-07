package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.dto.in.BookingConfirmationRequest;
import com.kdu.rizzlers.dto.out.BookingConfirmationDetailsResponse;
import com.kdu.rizzlers.service.BookingConfirmationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for booking confirmation details operations
 */
@RestController
@RequestMapping("/booking-confirmation")
@RequiredArgsConstructor
@Slf4j
public class BookingConfirmationController {

    private final BookingConfirmationService bookingConfirmationService;
    
    /**
     * Get detailed booking confirmation information
     * 
     * @param request The request containing booking ID and guest ID
     * @return Detailed booking confirmation information
     */
    @PostMapping("/details")
    public ResponseEntity<BookingConfirmationDetailsResponse> getBookingConfirmationDetails(
            @Valid @RequestBody BookingConfirmationRequest request) {
        
        log.info("Received request for booking confirmation details: bookingId={}, guestId={}",
                request.getBookingId(), request.getGuestId());
        
        BookingConfirmationDetailsResponse response = 
                bookingConfirmationService.getBookingConfirmationDetails(request);
        
        if (Boolean.FALSE.equals(response.getSuccess())) {
            log.warn("Failed to retrieve booking confirmation details: {}", response.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get detailed booking confirmation information using path variables
     * 
     * @param bookingId The booking ID
     * @param guestId The guest ID
     * @return Detailed booking confirmation information
     */
    @GetMapping("/{bookingId}/{guestId}")
    public ResponseEntity<BookingConfirmationDetailsResponse> getBookingConfirmationDetailsByPath(
            @PathVariable Integer bookingId,
            @PathVariable Integer guestId) {
        
        log.info("Received GET request for booking confirmation details: bookingId={}, guestId={}",
                bookingId, guestId);
        
        BookingConfirmationRequest request = new BookingConfirmationRequest(bookingId, guestId);
        BookingConfirmationDetailsResponse response = 
                bookingConfirmationService.getBookingConfirmationDetails(request);
        
        if (Boolean.FALSE.equals(response.getSuccess())) {
            log.warn("Failed to retrieve booking confirmation details: {}", response.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        
        return ResponseEntity.ok(response);
    }
} 