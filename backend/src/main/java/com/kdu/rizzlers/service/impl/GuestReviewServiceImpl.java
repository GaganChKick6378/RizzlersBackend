package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.dto.ReviewSubmissionDTO;
import com.kdu.rizzlers.entity.Review;
import com.kdu.rizzlers.entity.ReviewInvitation;
import com.kdu.rizzlers.repository.GuestReviewRepository;
import com.kdu.rizzlers.repository.ReviewInvitationRepository;
import com.kdu.rizzlers.service.GuestReviewService;
import com.kdu.rizzlers.service.ReviewInvitationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of GuestReviewService
 */
@Service
@Slf4j
public class GuestReviewServiceImpl implements GuestReviewService {

    private final GuestReviewRepository guestReviewRepository;
    private final ReviewInvitationRepository reviewInvitationRepository;
    private final ReviewInvitationService reviewInvitationService;

    @Autowired
    public GuestReviewServiceImpl(
            GuestReviewRepository guestReviewRepository,
            ReviewInvitationRepository reviewInvitationRepository,
            ReviewInvitationService reviewInvitationService) {
        this.guestReviewRepository = guestReviewRepository;
        this.reviewInvitationRepository = reviewInvitationRepository;
        this.reviewInvitationService = reviewInvitationService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public Optional<Review> submitReview(ReviewSubmissionDTO reviewSubmission) {
        if (reviewSubmission == null || reviewSubmission.getToken() == null || reviewSubmission.getToken().isEmpty()) {
            log.error("Invalid review submission: token is missing");
            return Optional.empty();
        }
        
        try {
            // Validate the token and get the invitation
            Optional<ReviewInvitation> invitationOpt = reviewInvitationRepository.findByToken(reviewSubmission.getToken());
            
            if (invitationOpt.isEmpty()) {
                log.error("Review submission failed: token not found");
                return Optional.empty();
            }
            
            ReviewInvitation invitation = invitationOpt.get();
            
            // Check if already completed
            if (invitation.getIsCompleted()) {
                log.error("Review submission failed: review already submitted for this token");
                return Optional.empty();
            }
            
            // Check if expired
            if (ZonedDateTime.now().isAfter(invitation.getExpiresAt())) {
                log.error("Review submission failed: token has expired");
                return Optional.empty();
            }
            
            // Check if a review already exists with this invitation ID
            if (guestReviewRepository.existsByInvitationId(invitation.getId())) {
                log.error("Review submission failed: a review already exists for this invitation");
                return Optional.empty();
            }
            
            // Get property and room type information if not provided
            Integer propertyId = reviewSubmission.getPropertyId();
            Integer roomTypeId = reviewSubmission.getRoomTypeId();
            
            if (propertyId == null || roomTypeId == null) {
                // Validate the token to get property and room type information
                var validationResponse = reviewInvitationService.validateToken(reviewSubmission.getToken());
                if (validationResponse.isValid()) {
                    if (propertyId == null) {
                        propertyId = validationResponse.getPropertyId();
                    }
                    if (roomTypeId == null) {
                        roomTypeId = validationResponse.getRoomTypeId();
                    }
                }
            }
            
            // Ensure we have both IDs before proceeding
            if (propertyId == null || roomTypeId == null) {
                log.error("Review submission failed: property ID and room type ID are required");
                return Optional.empty();
            }
            
            // Create a new review
            Review review = Review.builder()
                    .invitationId(invitation.getId())
                    .bookingId(invitation.getBookingId())
                    .guestId(invitation.getGuestId())
                    .propertyId(propertyId)
                    .roomTypeId(roomTypeId)
                    .cleanlinessRating(reviewSubmission.getCleanlinessRating())
                    .staffServiceRating(reviewSubmission.getStaffServiceRating())
                    .comfortRating(reviewSubmission.getComfortRating())
                    .locationRating(reviewSubmission.getLocationRating())
                    .valueRating(reviewSubmission.getValueRating())
                    .overallRating(reviewSubmission.getOverallRating())
                    .comment(reviewSubmission.getComment())
                    .images(reviewSubmission.getImages())
                    .submittedAt(ZonedDateTime.now())
                    .build();
            
            // Save the review
            Review savedReview = guestReviewRepository.save(review);
            
            // Mark the invitation as completed
            reviewInvitationService.markInvitationCompleted(invitation.getToken());
            
            log.info("Review successfully submitted for booking {}", invitation.getBookingId());
            return Optional.of(savedReview);
            
        } catch (Exception e) {
            log.error("Error submitting review: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Review> getReviewsByRoomType(Integer roomTypeId) {
        if (roomTypeId == null) {
            return Collections.emptyList();
        }
        
        try {
            return guestReviewRepository.findByRoomTypeId(roomTypeId);
        } catch (Exception e) {
            log.error("Error fetching reviews by room type: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<Review> getReviewsByProperty(Integer propertyId) {
        if (propertyId == null) {
            return Collections.emptyList();
        }
        
        try {
            return guestReviewRepository.findByPropertyId(propertyId);
        } catch (Exception e) {
            log.error("Error fetching reviews by property: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
} 