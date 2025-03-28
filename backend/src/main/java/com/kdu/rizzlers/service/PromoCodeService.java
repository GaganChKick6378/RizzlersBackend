package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.out.PropertyPromotionScheduleResponse;

import java.util.List;
import java.util.Optional;

/**
 * Service for handling promo code operations
 */
public interface PromoCodeService {

    /**
     * Validate a promo code
     * 
     * @param promoCode The promo code to validate
     * @return Optional containing the promotion details if valid, empty otherwise
     */
    Optional<PropertyPromotionScheduleResponse> validatePromoCode(String promoCode);
    
    /**
     * Get all active and visible promotions
     * 
     * @return List of all active and visible promotions
     */
    List<PropertyPromotionScheduleResponse> getAllVisiblePromotions();
} 