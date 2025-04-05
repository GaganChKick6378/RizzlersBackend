package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.dto.TravelItineraryDto;
import com.kdu.rizzlers.service.EmailService;
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
public class EmailController {

    private final EmailService emailService;

    /**
     * Endpoint for sending travel itinerary emails
     * 
     * @param itinerary The travel itinerary data
     * @param recipientEmail The recipient's email address
     * @return ResponseEntity with success or error message
     */
    @PostMapping("/travel-itinerary")
    public ResponseEntity<?> sendTravelItineraryEmail(
            @RequestBody TravelItineraryDto itinerary,
            @RequestParam String recipientEmail) {
        
        log.info("Received request to send travel itinerary email to: {}", recipientEmail);
        
        boolean success = emailService.sendTravelItineraryEmail(itinerary, recipientEmail);
        
        if (success) {
            return ResponseEntity.ok().body(
                    "{\"message\": \"Travel itinerary email sent successfully to " + recipientEmail + "\"}");
        } else {
            return ResponseEntity.internalServerError().body(
                    "{\"error\": \"Failed to send travel itinerary email\"}");
        }
    }
} 