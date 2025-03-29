package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.dto.in.CombinedPromotionRequestDTO;
import com.kdu.rizzlers.dto.in.PromotionEligibilityRequestDTO;
import com.kdu.rizzlers.dto.out.PromotionDTO;
import com.kdu.rizzlers.entity.PropertyPromotion;
import com.kdu.rizzlers.repository.PropertyPromotionRepository;
import com.kdu.rizzlers.service.PromotionGraphQLService;
import com.kdu.rizzlers.service.PromotionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private final PromotionGraphQLService promotionGraphQLService;
    private final PropertyPromotionRepository propertyPromotionRepository;

    @Override
    public List<PromotionDTO> getAllPromotions() {
        try {
            return promotionGraphQLService.fetchAllPromotions();
        } catch (Exception e) {
            log.error("Error fetching promotions", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<PromotionDTO> getEligiblePromotions(PromotionEligibilityRequestDTO request) {
        List<PromotionDTO> allPromotions = getAllPromotions();
        
        return allPromotions.stream()
                .filter(promotion -> !promotion.getIsDeactivated())
                .filter(promotion -> isEligibleForPromotion(promotion, request))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<PromotionDTO> getCombinedPromotionsForProperty(Integer propertyId, LocalDate startDate, LocalDate endDate) {
        log.info("Fetching combined promotions for property: {}, date range: {} to {}", 
                propertyId, startDate, endDate);
        
        // Fetch data sequentially - first GraphQL
        List<PromotionDTO> graphQlPromotions = getAllPromotions();
        
        // Then database
        List<PropertyPromotion> dbPromotions = propertyPromotionRepository
                .findActiveAndVisiblePromotionsForPropertyInDateRange(propertyId, startDate, endDate);
        log.info("Found {} property-specific promotions in database for property: {}, date range: {} to {}", 
                dbPromotions.size(), propertyId, startDate, endDate);
        
        // Combine the results
        Set<Integer> promotionIds = new HashSet<>();
        List<PromotionDTO> result = new ArrayList<>();
        
        // Add GraphQL promotions first (they might be overridden by DB promotions)
        log.info("Adding active GraphQL promotions");
        graphQlPromotions.stream()
            .filter(promotion -> !promotion.getIsDeactivated())
            .forEach(promotion -> {
                result.add(promotion);
                promotionIds.add(promotion.getPromotionId());
                log.debug("Added GraphQL promotion: {}", promotion.getPromotionId());
            });
        
        // Add DB promotions, potentially overriding GraphQL ones with same ID
        // Note: dbPromotions are already filtered for isActive=true AND isVisible=true at the repository level
        log.info("Adding database promotions (already filtered for isActive=true AND isVisible=true)");
        for (PropertyPromotion dbPromotion : dbPromotions) {
            PromotionDTO promotionDTO = dbPromotion.toDTO().toPromotionDTO();
            
            // Either add new promotion or replace existing one
            if (promotionIds.contains(promotionDTO.getPromotionId())) {
                // Replace the existing promotion
                log.debug("Replacing existing promotion with ID: {}", promotionDTO.getPromotionId());
                result.removeIf(p -> p.getPromotionId().equals(promotionDTO.getPromotionId()));
            }
            
            result.add(promotionDTO);
            promotionIds.add(promotionDTO.getPromotionId());
            log.debug("Added database promotion: {}", promotionDTO.getPromotionId());
        }
        
        log.info("Combined {} promotions in total", result.size());
        return result;
    }
    
    @Override
    @Deprecated
    public List<PromotionDTO> getCombinedPromotions(LocalDate startDate, LocalDate endDate) {
        log.warn("Using deprecated method. Please use getCombinedPromotionsForProperty instead.");
        
        // Fetch data sequentially - first GraphQL
        List<PromotionDTO> graphQlPromotions = getAllPromotions();
        
        // Then database
        List<PropertyPromotion> dbPromotions = propertyPromotionRepository
                .findActiveAndVisiblePromotionsInDateRange(startDate, endDate);
        
        // Combine the results
        Set<Integer> promotionIds = new HashSet<>();
        List<PromotionDTO> result = new ArrayList<>();
        
        // Add GraphQL promotions first (they might be overridden by DB promotions)
        graphQlPromotions.stream()
            .filter(promotion -> !promotion.getIsDeactivated())
            .forEach(promotion -> {
                result.add(promotion);
                promotionIds.add(promotion.getPromotionId());
            });
        
        // Add DB promotions, potentially overriding GraphQL ones with same ID
        // Note: dbPromotions are already filtered for isActive=true AND isVisible=true at the repository level
        for (PropertyPromotion dbPromotion : dbPromotions) {
            PromotionDTO promotionDTO = dbPromotion.toDTO().toPromotionDTO();
            
            // Either add new promotion or replace existing one
            if (promotionIds.contains(promotionDTO.getPromotionId())) {
                // Replace the existing promotion
                result.removeIf(p -> p.getPromotionId().equals(promotionDTO.getPromotionId()));
            }
            
            result.add(promotionDTO);
            promotionIds.add(promotionDTO.getPromotionId());
        }
        
        return result;
    }
    
    @Override
    public List<PromotionDTO> getEligiblePropertyPromotions(CombinedPromotionRequestDTO request) {
        log.info("Fetching eligible property promotions for property: {}, date range: {} to {}, adults: {}, seniors: {}, kids: {}, " +
                "military: {}, kdu member: {}, upfront payment: {}", 
                request.getPropertyId(), request.getStartDate(), request.getEndDate(), 
                request.getAdults(), request.getSeniorCitizens(), request.getKids(),
                request.getIsMilitaryPersonnel(), request.getIsKduMember(), request.getIsUpfrontPayment());
        
        // Get combined promotions for the property (GraphQL + database)
        List<PromotionDTO> combinedPromotions = getCombinedPromotionsForProperty(
                request.getPropertyId(), request.getStartDate(), request.getEndDate());
        
        log.info("Retrieved {} combined promotions before eligibility filtering", combinedPromotions.size());
        
        // Filter the combined promotions by eligibility criteria
        List<PromotionDTO> eligiblePromotions = combinedPromotions.stream()
                .filter(promotion -> !promotion.getIsDeactivated())
                .filter(promotion -> isEligibleForPromotion(promotion, request))
                .collect(Collectors.toList());
        
        log.info("Filtered down to {} eligible promotions", eligiblePromotions.size());
        return eligiblePromotions;
    }
    
    private boolean isEligibleForPromotion(PromotionDTO promotion, PromotionEligibilityRequestDTO request) {
        // Check minimum stay requirement
        if (request.getLengthOfStay() < promotion.getMinimumDaysOfStay()) {
            return false;
        }
        
        // Check specific promotion criteria
        switch (promotion.getPromotionTitle()) {
            case "SENIOR_CITIZEN_DISCOUNT":
                return request.hasSeniorCitizens();
                
            case "KDU Membership Discount":
                return request.getIsKduMember();
                
            case "Long weekend discount":
                return request.getLengthOfStay() >= 3 && request.includesFullWeekend();
                
            case "Military personnel discount":
                return request.getIsMilitaryPersonnel();
                
            case "Upfront payment discount":
                return request.getIsUpfrontPayment();
                
            case "Weekend discount":
                return request.getLengthOfStay() >= 2 && request.includesWeekend();
                
            default:
                // For unrecognized promotions, just check minimum stay
                return true;
        }
    }

    // Overloaded method to support CombinedPromotionRequestDTO
    private boolean isEligibleForPromotion(PromotionDTO promotion, CombinedPromotionRequestDTO request) {
        // Check minimum stay requirement
        if (request.getLengthOfStay() < promotion.getMinimumDaysOfStay()) {
            return false;
        }
        
        // Check specific promotion criteria
        switch (promotion.getPromotionTitle()) {
            case "SENIOR_CITIZEN_DISCOUNT":
                return request.hasSeniorCitizens();
                
            case "KDU Membership Discount":
                return request.getIsKduMember();
                
            case "Long weekend discount":
                return request.getLengthOfStay() >= 3 && request.includesFullWeekend();
                
            case "Military personnel discount":
                return request.getIsMilitaryPersonnel();
                
            case "Upfront payment discount":
                return request.getIsUpfrontPayment();
                
            case "Weekend discount":
                return request.getLengthOfStay() >= 2 && request.includesWeekend();
                
            default:
                // For unrecognized promotions, just check minimum stay
                return true;
        }
    }
} 