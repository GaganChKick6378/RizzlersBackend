package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.dto.in.CombinedPromotionRequestDTO;
import com.kdu.rizzlers.dto.in.PromotionEligibilityRequestDTO;
import com.kdu.rizzlers.dto.out.DailyRoomRateDTO;
import com.kdu.rizzlers.dto.out.PromotionDTO;
import com.kdu.rizzlers.entity.PropertyPromotion;
import com.kdu.rizzlers.repository.PropertyPromotionRepository;
import com.kdu.rizzlers.service.PromotionGraphQLService;
import com.kdu.rizzlers.service.PromotionService;
import com.kdu.rizzlers.service.RoomRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private final PromotionGraphQLService promotionGraphQLService;
    private final PropertyPromotionRepository propertyPromotionRepository;
    private final RoomRateService roomRateService;

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
        
        // Fetch all daily rates for the property and date range
        List<DailyRoomRateDTO> dailyRates = roomRateService.getDailyRatesWithPromotions(
                null, // tenantId is not needed as per the implementation
                request.getPropertyId()
        );
        
        // Filter daily rates to only include dates within the request range
        // IMPORTANT: Exclude the end date (checkout date) from pricing calculations
        List<DailyRoomRateDTO> filteredDailyRates = dailyRates.stream()
                .filter(rate -> 
                    (rate.getDate().isEqual(request.getStartDate()) || rate.getDate().isAfter(request.getStartDate())) && 
                    rate.getDate().isBefore(request.getEndDate())) // Exclude the end date
                .collect(Collectors.toList());
        
        log.info("Found {} daily rates within the requested date range (excluding checkout date)", filteredDailyRates.size());
        
        // Calculate original prices (average of daily rates)
        double originalPrice = 0;
        if (!filteredDailyRates.isEmpty()) {
            originalPrice = filteredDailyRates.stream()
                    .mapToDouble(DailyRoomRateDTO::getMinimumRate)
                    .average()
                    .orElse(0);
        }
        
        log.info("Average original price: {}", originalPrice);
        
        // Create a map of date -> standard rate for looking up standard rates by date
        Map<LocalDate, Double> dateToRateMap = filteredDailyRates.stream()
                .collect(Collectors.toMap(DailyRoomRateDTO::getDate, DailyRoomRateDTO::getMinimumRate));
        
        // Get all property-specific promotions from the repository
        List<PropertyPromotion> rdsPromotions = propertyPromotionRepository
                .findActiveAndVisiblePromotionsForPropertyInDateRange(
                        request.getPropertyId(), request.getStartDate(), request.getEndDate().minusDays(1)); // Adjust end date
        
        // Create a map of promotion ID -> promotion for fast lookup
        Map<Integer, PropertyPromotion> promotionIdToPromotionMap = rdsPromotions.stream()
                .collect(Collectors.toMap(PropertyPromotion::getPromotionId, p -> p, (p1, p2) -> p1));
        
        log.info("Found {} RDS promotions for the date range", promotionIdToPromotionMap.size());
        
        // Calculate discounted prices for each promotion
        for (PromotionDTO promotion : eligiblePromotions) {
            promotion.setOriginalPrice(originalPrice);
            Integer promotionId = promotion.getPromotionId();
            
            // Check if this is an RDS promotion by looking up in our map
            if (promotionIdToPromotionMap.containsKey(promotionId)) {
                log.debug("Processing RDS promotion ID: {}", promotionId);
                PropertyPromotion rdsPromotion = promotionIdToPromotionMap.get(promotionId);
                
                // Calculate day-by-day prices for the booking period
                double totalStandardPrice = 0;
                double totalDiscountedPrice = 0;
                int daysCount = 0;
                
                // For each date in the booking range (excluding checkout date)
                LocalDate currentDate = request.getStartDate();
                while (currentDate.isBefore(request.getEndDate())) { // Exclude the end date
                    Double standardRate = dateToRateMap.get(currentDate);
                    if (standardRate != null) {
                        // Add to total standard price
                        totalStandardPrice += standardRate;
                        
                        // Check if this date is within the promotion's date range
                        if (!currentDate.isBefore(rdsPromotion.getStartDate()) && 
                            !currentDate.isAfter(rdsPromotion.getEndDate())) {
                            // Apply discount for this date
                            totalDiscountedPrice += standardRate * rdsPromotion.getPriceFactor();
                            log.debug("Date {} has promotion: standard rate {} * factor {} = {}", 
                                    currentDate, standardRate, rdsPromotion.getPriceFactor(), 
                                    standardRate * rdsPromotion.getPriceFactor());
                        } else {
                            // No promotion for this date
                            totalDiscountedPrice += standardRate;
                            log.debug("Date {} has NO promotion: standard rate {}", currentDate, standardRate);
                        }
                        daysCount++;
                    }
                    currentDate = currentDate.plusDays(1);
                }
                
                // Calculate averages
                double avgStandardPrice = daysCount > 0 ? totalStandardPrice / daysCount : 0;
                double avgDiscountedPrice = daysCount > 0 ? totalDiscountedPrice / daysCount : 0;
                
                promotion.setOriginalPrice(avgStandardPrice);
                promotion.setDiscountedPrice(avgDiscountedPrice);
                
                log.info("RDS promotion {}: original price = {}, discounted price = {}", 
                        promotionId, avgStandardPrice, avgDiscountedPrice);
            } 
            // For GraphQL promotions, apply a single price factor
            else {
                log.debug("Processing GraphQL promotion ID: {}", promotionId);
                double discountedPrice = originalPrice * promotion.getPriceFactor();
                promotion.setDiscountedPrice(discountedPrice);
                
                log.info("GraphQL promotion {}: original price = {}, price factor = {}, discounted price = {}", 
                        promotionId, originalPrice, promotion.getPriceFactor(), discountedPrice);
            }
        }
        
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