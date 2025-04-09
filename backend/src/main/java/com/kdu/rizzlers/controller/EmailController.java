package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.dto.in.BookingConfirmationRequest;
import com.kdu.rizzlers.dto.out.BookingConfirmationDetailsResponse;
import com.kdu.rizzlers.service.BookingConfirmationService;
import com.kdu.rizzlers.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling email-related endpoints
 */
@RestController
@RequestMapping("/emails")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Email", description = "APIs for sending emails")
public class EmailController {

    private final EmailService emailService;
    private final BookingConfirmationService bookingConfirmationService;

    /**
     * Endpoint for sending travel itinerary emails
     * 
     * @param bookingId The booking ID
     * @param guestId The guest ID
     * @param recipientEmail Email address to send the itinerary to (optional, defaults to guest email)
     * @return ResponseEntity with success or error message
     */
    @PostMapping("/travel-itinerary")
    @Operation(summary = "Send travel itinerary email", 
               description = "Sends a travel itinerary email for a booking")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email sent successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or booking not found"),
        @ApiResponse(responseCode = "500", description = "Failed to send email")
    })
    public ResponseEntity<?> sendTravelItineraryEmail(
            @Parameter(description = "Booking ID", required = true) 
            @RequestParam Integer bookingId,
            
            @Parameter(description = "Guest ID", required = true) 
            @RequestParam Integer guestId,
            
            @Parameter(description = "Email address to send the itinerary to (optional, defaults to guest email)") 
            @RequestParam(required = false) String recipientEmail) {
        
        log.info("Received request to send travel itinerary email for booking: {}, guest: {}", bookingId, guestId);
        
        // First, get booking confirmation details
        BookingConfirmationRequest request = new BookingConfirmationRequest(bookingId, guestId);
        BookingConfirmationDetailsResponse bookingDetails = bookingConfirmationService.getBookingConfirmationDetails(request);
        
        if (bookingDetails.getSuccess() == null || !bookingDetails.getSuccess()) {
            log.error("Failed to fetch booking details: {}", bookingDetails.getMessage());
            return ResponseEntity.badRequest().body(
                    "{\"error\": \"" + bookingDetails.getMessage() + "\"}");
        }
        
        // If recipientEmail is not provided, use the guest's email
        String emailTo = recipientEmail;
        if (emailTo == null || emailTo.isEmpty()) {
            emailTo = bookingDetails.getGuestInformation().getEmail();
            log.info("No recipient email provided, using guest email: {}", emailTo);
        }
        
        boolean success = emailService.sendTravelItineraryEmail(bookingDetails, emailTo);
        
        if (success) {
            return ResponseEntity.ok().body(
                    "{\"message\": \"Travel itinerary email sent successfully to " + emailTo + "\"}");
        } else {
            return ResponseEntity.internalServerError().body(
                    "{\"error\": \"Failed to send travel itinerary email\"}");
        }
    }
} 