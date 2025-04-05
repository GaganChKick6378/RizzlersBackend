package com.kdu.rizzlers.repository;

import com.kdu.rizzlers.entity.BillingInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for BillingInfo entity
 */
@Repository
public interface BillingInfoRepository extends JpaRepository<BillingInfo, Long> {
    
    /**
     * Find billing information for a specific booking
     * 
     * @param bookingId the booking ID
     * @return Optional of BillingInfo if found, otherwise empty
     */
    Optional<BillingInfo> findByBookingId(Integer bookingId);
    
    /**
     * Find all billing information for a list of booking IDs
     * 
     * @param bookingIds the list of booking IDs
     * @return List of BillingInfo
     */
    List<BillingInfo> findByBookingIdIn(List<Integer> bookingIds);
} 