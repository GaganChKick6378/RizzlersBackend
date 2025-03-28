package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.dto.in.CombinedPromotionRequestDTO;
import com.kdu.rizzlers.dto.out.PromotionDTO;
import com.kdu.rizzlers.service.PromotionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/promotions")
public class PromotionController {

    private final PromotionService promotionService;
    
    /**
     * Get all promotions available in the system
     * 
     * @return List of all promotions
     */
    @GetMapping
    public ResponseEntity<List<PromotionDTO>> getAllPromotions() {
        log.info("Request to get all promotions");
        List<PromotionDTO> promotions = promotionService.getAllPromotions();
        return ResponseEntity.ok(promotions);
    }
    
    /**
     * Get eligible promotions based on the criteria provided
     * Combines both GraphQL promotions and property-specific promotions from the database
     * 
     * @param request The criteria to check for eligibility including:
     *               - propertyId: The property ID
     *               - startDate: The check-in date
     *               - endDate: The check-out date
     *               - guests: Total number of guests for pricing calculations (overrides sum of individual guest types)
     *               - guestCount: Object with guest types and their counts
     *                 (e.g., {"adult": 2, "kid": 1, "seniorCitizen": 0})
     *               - adults: Number of adult guests (default: 0)
     *               - seniorCitizens: Number of senior citizens (default: 0)
     *               - kids: Number of kids (default: 0)
     *               - isMilitaryPersonnel: Whether the guest is military personnel (default: false)
     *               - isKduMember: Whether the guest is a KDU member (default: false)
     *               - isUpfrontPayment: Whether the guest is making full payment upfront (default: false)
     * @return List of eligible promotions
     */
    @PostMapping("/eligible")
    public ResponseEntity<List<PromotionDTO>> getEligiblePromotions(
            @RequestBody CombinedPromotionRequestDTO request) {
        
        int totalGuestCount = request.getTotalGuestCount();
        
        log.info("POST request to find eligible promotions with parameters:");
        log.info("- Property ID: {}", request.getPropertyId());
        log.info("- Date Range: {} to {}", request.getStartDate(), request.getEndDate());
        log.info("- Explicit guests field: {}", request.getGuests());
        log.info("- Guest count map: {}", request.getGuestCount());
        log.info("- Legacy fields: adults={}, seniors={}, kids={}", 
                request.getAdults(), request.getSeniorCitizens(), request.getKids());
        log.info("- Final total guest count used: {}", totalGuestCount);
        log.info("- Military personnel: {}", request.getIsMilitaryPersonnel());
        log.info("- KDU member: {}", request.getIsKduMember());
        log.info("- Upfront payment: {}", request.getIsUpfrontPayment());
        log.info("- Length of stay: {}", request.getLengthOfStay());
        log.info("- Includes weekend: {}", request.includesWeekend());
        
        List<PromotionDTO> eligiblePromotions;
        
        if (request.getPropertyId() == null) {
            log.warn("PropertyId is null, using GraphQL-only eligible promotions");
            eligiblePromotions = promotionService.getEligiblePromotions(request.toEligibilityRequest());
        } else {
            log.info("Using combined GraphQL and RDS eligible promotions for property: {}", request.getPropertyId());
            eligiblePromotions = promotionService.getEligiblePropertyPromotions(request);
        }
        
        log.info("Found {} eligible promotions", eligiblePromotions.size());
        
        return ResponseEntity.ok(eligiblePromotions);
    }
} 