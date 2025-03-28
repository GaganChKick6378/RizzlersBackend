package com.kdu.rizzlers.repository;

import com.kdu.rizzlers.entity.PromoCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface PromoCodeRepository extends JpaRepository<PromoCode, Long> {
    
    /**
     * Find a promo code by its code
     * 
     * @param promoCode The promo code to search for
     * @return The promo code entity if found
     */
    Optional<PromoCode> findByPromoCode(String promoCode);
    
    /**
     * Find a valid promo code (active and within date range) using JPA method naming convention
     * 
     * @param promoCode The promo code to validate
     * @param startDateCheck Current date to check against start date
     * @param endDateCheck Current date to check against end date
     * @return The promo code entity if valid
     */
    Optional<PromoCode> findByPromoCodeAndIsAvailableTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            String promoCode, 
            LocalDate startDateCheck, 
            LocalDate endDateCheck);
} 