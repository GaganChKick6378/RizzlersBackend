package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.in.CombinedPromotionRequestDTO;
import com.kdu.rizzlers.dto.in.PromotionEligibilityRequestDTO;
import com.kdu.rizzlers.dto.out.PromotionDTO;

import java.time.LocalDate;
import java.util.List;

public interface PromotionService {
    
    /**
     * Get all promotions from the GraphQL service
     * 
     * @return List of all promotions
     */
    List<PromotionDTO> getAllPromotions();
    
    /**
     * Get promotions that are eligible for the given criteria
     * 
     * @param request The criteria to check for eligibility
     * @return List of eligible promotions
     */
    List<PromotionDTO> getEligiblePromotions(PromotionEligibilityRequestDTO request);
    
    /**
     * Get all promotions (from both GraphQL and database) that are available for the given date range
     * 
     * @param startDate The start date
     * @param endDate The end date
     * @return Combined list of promotions from both sources
     * @deprecated Use {@link #getCombinedPromotionsForProperty(Integer, LocalDate, LocalDate)} instead
     */
    @Deprecated
    List<PromotionDTO> getCombinedPromotions(LocalDate startDate, LocalDate endDate);
    
    /**
     * Get all promotions (from both GraphQL and database) that are available for the given property and date range
     * 
     * @param propertyId The property ID
     * @param startDate The start date
     * @param endDate The end date
     * @return Combined list of promotions from both sources for the specified property
     */
    List<PromotionDTO> getCombinedPromotionsForProperty(Integer propertyId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Get eligible property-specific promotions based on the criteria provided
     * This combines both GraphQL and database promotions and filters them by eligibility
     * 
     * @param request The combined request with property ID and eligibility criteria
     * @return List of eligible promotions from both GraphQL and database
     */
    List<PromotionDTO> getEligiblePropertyPromotions(CombinedPromotionRequestDTO request);
} 