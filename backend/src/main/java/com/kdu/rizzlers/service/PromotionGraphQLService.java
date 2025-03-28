package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.out.PromotionDTO;

import java.util.List;

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
} 