package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.out.PromotionDTO;

import java.util.List;
import java.util.Map;

/**
 * Service for executing GraphQL queries specifically for promotions
 */
public interface PromotionGraphQLService {
    /**
     * Fetch all promotions from the GraphQL API
     * 
     * @return List of all promotions
     */
    List<PromotionDTO> fetchAllPromotions();
    
    /**
     * Fetch a specific promotion by its ID from the GraphQL API
     * 
     * @param promotionId The ID of the promotion to fetch
     * @return Map containing the promotion details (price_factor, promotion_title, promotion_description)
     */
    Map<String, Object> fetchPromotion(Integer promotionId);
} 