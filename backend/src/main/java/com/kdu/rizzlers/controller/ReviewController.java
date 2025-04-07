package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.dto.ReviewSubmissionDTO;
import com.kdu.rizzlers.dto.ReviewValidationResponseDTO;
import com.kdu.rizzlers.entity.Review;
import com.kdu.rizzlers.service.GuestReviewService;
import com.kdu.rizzlers.service.ReviewInvitationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for handling guest reviews
 */
@RestController
@RequestMapping("/reviews")
@Slf4j
public class ReviewController {

    private final ReviewInvitationService reviewInvitationService;
    private final GuestReviewService guestReviewService;
    
    @Value("${frontend.review-url:http://localhost:3000/review}")
    private String frontendReviewUrl;

    @Autowired
    public ReviewController(
            ReviewInvitationService reviewInvitationService,
            GuestReviewService guestReviewService) {
        this.reviewInvitationService = reviewInvitationService;
        this.guestReviewService = guestReviewService;
    }

    /**
     * Validate a review token
     * @param token the token to validate
     * @return ValidationResponse with status and booking details if valid
     */
    @GetMapping("/validate")
    public ResponseEntity<ReviewValidationResponseDTO> validateToken(@RequestParam String token) {
        log.info("Validating review token: {}", token);
        ReviewValidationResponseDTO validationResponse = reviewInvitationService.validateToken(token);
        
        if (validationResponse.isValid()) {
            log.info("Token valid for booking ID: {}", validationResponse.getBookingId());
        } else {
            log.warn("Invalid token: {}", validationResponse.getMessage());
        }
        
        return ResponseEntity.ok(validationResponse);
    }

    /**
     * Submit a review
     * @param reviewSubmission the review submission DTO
     * @return success response or error message
     */
    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submitReview(@Valid @RequestBody ReviewSubmissionDTO reviewSubmission) {
        log.info("Processing review submission for token: {}", reviewSubmission.getToken());
        
        // First validate the token to ensure we can retrieve property and room data if needed
        ReviewValidationResponseDTO validationResponse = reviewInvitationService.validateToken(reviewSubmission.getToken());
        if (!validationResponse.isValid()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", validationResponse.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
        
        Optional<Review> reviewOpt = guestReviewService.submitReview(reviewSubmission);
        
        if (reviewOpt.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Thank you for your review!");
            response.put("reviewId", reviewOpt.get().getId());
            
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to submit your review. This link may be invalid or expired.");
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Trigger review invitations for a specific date (admin only)
     * @param date the checkout date in format YYYY-MM-DD
     * @param apiKey secret key for authorization
     * @return count of invitations sent
     */
    @PostMapping("/send-invitations")
    public ResponseEntity<Map<String, Object>> sendInvitations(
            @RequestParam String date,
            @RequestParam String apiKey) {
        
        // Simple API key check - in a real app, use proper authentication
        if (!"secret-admin-key".equals(apiKey)) {
            return ResponseEntity.status(403).body(Map.of(
                "success", false,
                "message", "Unauthorized"
            ));
        }
        
        try {
            java.time.LocalDate checkoutDate = java.time.LocalDate.parse(date);
            int sent = reviewInvitationService.sendReviewInvitationsForCheckoutDate(checkoutDate);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "sent", sent,
                "date", date
            ));
        } catch (Exception e) {
            log.error("Error sending review invitations: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Invalid date format. Use YYYY-MM-DD."
            ));
        }
    }
} 