package com.kdu.rizzlers.repository;

import com.kdu.rizzlers.entity.ReviewInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewInvitationRepository extends JpaRepository<ReviewInvitation, Long> {
    
    /**
     * Find a review invitation by its token
     * @param token the unique token
     * @return Optional of ReviewInvitation if found
     */
    Optional<ReviewInvitation> findByToken(String token);
    
    /**
     * Find a review invitation by booking ID and completion status
     * @param bookingId the booking ID
     * @param isCompleted whether the invitation has been completed
     * @return Optional of ReviewInvitation if found
     */
    Optional<ReviewInvitation> findByBookingIdAndIsCompleted(Integer bookingId, Boolean isCompleted);
    
    /**
     * Find all incomplete invitations that have expired
     * @param now the current date/time
     * @return List of expired invitations
     */
    List<ReviewInvitation> findByIsCompletedFalseAndExpiresAtBefore(ZonedDateTime now);
    
    /**
     * Check if a review invitation exists for a booking
     * @param bookingId the booking ID
     * @return true if an invitation exists
     */
    boolean existsByBookingId(Integer bookingId);
} 