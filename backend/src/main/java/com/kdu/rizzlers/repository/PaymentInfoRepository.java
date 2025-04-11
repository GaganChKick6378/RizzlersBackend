package com.kdu.rizzlers.repository;

import com.kdu.rizzlers.entity.PaymentInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for PaymentInfo entity
 */
@Repository
public interface PaymentInfoRepository extends JpaRepository<PaymentInfo, Long> {
    
    /**
     * Find payment information for a specific booking
     * 
     * @param bookingId the booking ID
     * @return Optional of PaymentInfo if found, otherwise empty
     */
    Optional<PaymentInfo> findByBookingId(Integer bookingId);
    
    /**
     * Find all payment information for a list of booking IDs
     * 
     * @param bookingIds the list of booking IDs
     * @return List of PaymentInfo
     */
    List<PaymentInfo> findByBookingIdIn(List<Integer> bookingIds);
} 