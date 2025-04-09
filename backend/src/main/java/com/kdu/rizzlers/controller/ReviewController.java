package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.dto.ReviewSubmissionDTO;
import com.kdu.rizzlers.dto.ReviewValidationResponseDTO;
import com.kdu.rizzlers.entity.Review;
import com.kdu.rizzlers.exception.ApiError;
import com.kdu.rizzlers.exception.InvalidReviewException;
import com.kdu.rizzlers.service.GuestReviewService;
import com.kdu.rizzlers.service.ReviewInvitationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for handling guest reviews
 */
@RestController
@RequestMapping("/reviews")
@Slf4j
@Tag(name = "Reviews", description = "APIs for guest reviews management")
public class ReviewController {

    private final ReviewInvitationService reviewInvitationService;
    private final GuestReviewService guestReviewService;
    
    @Value("${frontend.review-url}")
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
    @Operation(summary = "Validate review token", description = "Validates a review token and returns booking details if valid")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token validation processed",
                   content = @Content(schema = @Schema(implementation = ReviewValidationResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid token",
                   content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<?> validateToken(
            @Parameter(description = "Review token to validate", required = true)
            @RequestParam String token) {
        
        try {
            if (token == null || token.trim().isEmpty()) {
                throw new InvalidReviewException("Review token is required");
            }
            
            log.info("Validating review token: {}", token);
            ReviewValidationResponseDTO validationResponse = reviewInvitationService.validateToken(token);
            
            if (validationResponse.isValid()) {
                log.info("Token valid for booking ID: {}", validationResponse.getBookingId());
            } else {
                log.warn("Invalid token: {}", validationResponse.getMessage());
                throw new InvalidReviewException(validationResponse.getMessage());
            }
            
            return ResponseEntity.ok(validationResponse);
        } catch (InvalidReviewException e) {
            log.warn("Invalid review token: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("error", "Bad Request");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            log.error("Error validating review token: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("error", "Internal Server Error");
            response.put("message", "An unexpected error occurred while validating the review token");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Submit a review
     * @param reviewSubmission the review submission DTO
     * @return success response or error message
     */
    @PostMapping("/submit")
    @Operation(summary = "Submit review", description = "Submits a guest review")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Review submitted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or token",
                   content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<?> submitReview(
            @Parameter(description = "Review submission details", required = true)
            @Valid @RequestBody ReviewSubmissionDTO reviewSubmission) {
        
        try {
            // Validate the submission data
            if (reviewSubmission == null) {
                throw new InvalidReviewException("Review submission data is required");
            }
            
            if (reviewSubmission.getToken() == null || reviewSubmission.getToken().trim().isEmpty()) {
                throw new InvalidReviewException("Review token is required");
            }
            
            log.info("Processing review submission for token: {}", reviewSubmission.getToken());
            
            // First validate the token to ensure we can retrieve property and room data if needed
            ReviewValidationResponseDTO validationResponse = reviewInvitationService.validateToken(reviewSubmission.getToken());
            if (!validationResponse.isValid()) {
                throw new InvalidReviewException(validationResponse.getMessage());
            }
            
            Optional<Review> reviewOpt = guestReviewService.submitReview(reviewSubmission);
            
            if (reviewOpt.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Thank you for your review!");
                response.put("reviewId", reviewOpt.get().getId());
                
                return ResponseEntity.ok(response);
            } else {
                throw new InvalidReviewException("Failed to submit your review. This link may be invalid or expired.");
            }
        } catch (InvalidReviewException e) {
            log.warn("Invalid review submission: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("error", "Bad Request");
            response.put("message", e.getMessage());
            response.put("success", false);
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            log.error("Error submitting review: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("error", "Internal Server Error");
            response.put("message", "An unexpected error occurred while submitting your review");
            response.put("success", false);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Trigger review invitations for a specific date (admin only)
     * @param date the checkout date in format YYYY-MM-DD
     * @param apiKey secret key for authorization
     * @return count of invitations sent
     */
    @PostMapping("/send-invitations")
    @Operation(summary = "Send review invitations", 
              description = "Triggers review invitations for bookings with a specific checkout date (admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Invitations sent successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid date format",
                   content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "403", description = "Unauthorized access",
                   content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<?> sendInvitations(
            @Parameter(description = "Checkout date in format YYYY-MM-DD", required = true)
            @RequestParam String date,
            
            @Parameter(description = "API key for authorization", required = true)
            @RequestParam String apiKey) {
        
        try {
            // Validate API key
            if (!"secret-admin-key".equals(apiKey)) {
                Map<String, Object> response = new HashMap<>();
                response.put("timestamp", LocalDateTime.now());
                response.put("status", HttpStatus.FORBIDDEN.value());
                response.put("error", "Forbidden");
                response.put("message", "Unauthorized access");
                response.put("success", false);
                
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            // Validate date
            if (date == null || date.trim().isEmpty()) {
                throw new IllegalArgumentException("Date is required");
            }
            
            java.time.LocalDate checkoutDate = java.time.LocalDate.parse(date);
            int sent = reviewInvitationService.sendReviewInvitationsForCheckoutDate(checkoutDate);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("sent", sent);
            response.put("date", date);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid date format: {}", date);
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("error", "Bad Request");
            response.put("message", "Invalid date format. Use YYYY-MM-DD.");
            response.put("success", false);
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            log.error("Error sending review invitations: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("error", "Internal Server Error");
            response.put("message", "An unexpected error occurred while sending review invitations");
            response.put("success", false);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
} 