package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.ReviewSubmissionDTO;
import com.kdu.rizzlers.entity.Review;

import java.util.List;
import java.util.Optional;

/**
 * Service for handling guest reviews after stays
 */
public interface GuestReviewService {
    
    /**
     * Submit a new review
     * @param reviewSubmission the review submission DTO
     * @return the created review or empty if failed
     */
    Optional<Review> submitReview(ReviewSubmissionDTO reviewSubmission);
    
    /**
     * Get reviews for a room type
     * @param roomTypeId the room type ID
     * @return list of reviews
     */
    List<Review> getReviewsByRoomType(Integer roomTypeId);
    
    /**
     * Get reviews for a property
     * @param propertyId the property ID
     * @return list of reviews
     */
    List<Review> getReviewsByProperty(Integer propertyId);
} 