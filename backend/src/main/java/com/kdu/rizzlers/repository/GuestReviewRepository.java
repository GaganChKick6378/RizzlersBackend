package com.kdu.rizzlers.repository;

import com.kdu.rizzlers.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for guest reviews after stays
 */
@Repository
public interface GuestReviewRepository extends JpaRepository<Review, Long> {
    
    /**
     * Find reviews by room type ID
     * @param roomTypeId the room type ID
     * @return List of reviews for the room type
     */
    List<Review> findByRoomTypeId(Integer roomTypeId);
    
    /**
     * Find reviews by property ID
     * @param propertyId the property ID
     * @return List of reviews for the property
     */
    List<Review> findByPropertyId(Integer propertyId);
    
    /**
     * Find a review by its invitation ID
     * @param invitationId the invitation ID
     * @return Optional of Review if found
     */
    Optional<Review> findByInvitationId(Long invitationId);
    
    /**
     * Check if a review exists for an invitation
     * @param invitationId the invitation ID
     * @return true if a review exists
     */
    boolean existsByInvitationId(Long invitationId);
} 