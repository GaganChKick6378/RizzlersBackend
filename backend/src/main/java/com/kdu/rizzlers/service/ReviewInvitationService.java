package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.ReviewValidationResponseDTO;
import com.kdu.rizzlers.entity.ReviewInvitation;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Service for managing review invitations
 */
public interface ReviewInvitationService {
    
    /**
     * Generate and send review invitations for checkouts on a given date
     * @param checkoutDate the date to check for checkouts (default today)
     * @return number of invitations sent
     */
    int sendReviewInvitationsForCheckoutDate(LocalDate checkoutDate);
    
    /**
     * Validate a review token
     * @param token the token to validate
     * @return a validation response DTO
     */
    ReviewValidationResponseDTO validateToken(String token);
    
    /**
     * Mark a review invitation as completed
     * @param token the token of the invitation
     * @return true if successful
     */
    boolean markInvitationCompleted(String token);
    
    /**
     * Find a review invitation by token
     * @param token the token
     * @return Optional of ReviewInvitation
     */
    Optional<ReviewInvitation> findByToken(String token);
} 