package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.dto.out.PropertyPromotionScheduleResponse;
import com.kdu.rizzlers.entity.PropertyPromotionSchedule;
import com.kdu.rizzlers.repository.PropertyPromotionScheduleRepository;
import com.kdu.rizzlers.service.PromoCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of the PromoCodeService that uses PropertyPromotionSchedule
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PromoCodeServiceImpl implements PromoCodeService {

    private final PropertyPromotionScheduleRepository promotionScheduleRepository;

    @Override
    public Optional<PropertyPromotionScheduleResponse> validatePromoCode(String promoCode) {
        if (promoCode == null || promoCode.trim().isEmpty()) {
            log.warn("Promo code is null or empty");
            return Optional.empty();
        }
        
        log.info("Validating promo code: {}", promoCode);
        
        // Use the current date for validation
        LocalDate currentDate = LocalDate.now();
        
        // Find valid promo code (active and within date range, regardless of visibility)
        return promotionScheduleRepository.findByPromoCodeAndIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                promoCode, currentDate, currentDate)
                .map(this::mapToResponse);
    }
    
    @Override
    public List<PropertyPromotionScheduleResponse> getAllVisiblePromotions() {
        log.info("Getting all visible promotions");
        
        // Use the current date for validation
        LocalDate currentDate = LocalDate.now();
        
        // Find all valid promotions that are active, visible, and within date range
        return promotionScheduleRepository.findByIsActiveTrueAndIsVisibleTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                currentDate, currentDate)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Maps a PropertyPromotionSchedule entity to a PropertyPromotionScheduleResponse DTO
     * 
     * @param promotion The PropertyPromotionSchedule entity
     * @return The PropertyPromotionScheduleResponse DTO
     */
    private PropertyPromotionScheduleResponse mapToResponse(PropertyPromotionSchedule promotion) {
        return PropertyPromotionScheduleResponse.builder()
                .id(promotion.getId())
                .propertyId(promotion.getPropertyId())
                .promotionId(promotion.getPromotionId())
                .title(promotion.getTitle())
                .description(promotion.getDescription())
                .promoCode(promotion.getPromoCode())
                .priceFactor(promotion.getPriceFactor())
                .startDate(promotion.getStartDate())
                .endDate(promotion.getEndDate())
                .isActive(promotion.getIsActive())
                .isVisible(promotion.getIsVisible())
                .createdAt(promotion.getCreatedAt())
                .updatedAt(promotion.getUpdatedAt())
                .build();
    }
} 