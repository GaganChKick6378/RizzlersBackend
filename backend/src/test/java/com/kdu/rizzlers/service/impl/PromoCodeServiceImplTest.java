package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.dto.out.PropertyPromotionScheduleResponse;
import com.kdu.rizzlers.entity.PropertyPromotionSchedule;
import com.kdu.rizzlers.repository.PropertyPromotionScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromoCodeServiceImplTest {

    @Mock
    private PropertyPromotionScheduleRepository promotionScheduleRepository;

    @InjectMocks
    private PromoCodeServiceImpl promoCodeService;

    private PropertyPromotionSchedule validPromotion;
    private PropertyPromotionSchedule secondPromotion;
    private LocalDate currentDate;

    @BeforeEach
    void setup() {
        currentDate = LocalDate.now();
        
        validPromotion = PropertyPromotionSchedule.builder()
                .id(1L)
                .propertyId(100)
                .promotionId(200)
                .title("Summer Special")
                .description("20% off for summer bookings")
                .promoCode("SUMMER20")
                .priceFactor(new BigDecimal("0.80")) // 20% off = 0.80 factor
                .startDate(currentDate.minusDays(10))
                .endDate(currentDate.plusDays(30))
                .isActive(true)
                .isVisible(true)
                .build();

        secondPromotion = PropertyPromotionSchedule.builder()
                .id(2L)
                .propertyId(100)
                .promotionId(201)
                .title("Early Bird")
                .description("15% off for early bookings")
                .promoCode("EARLY15")
                .priceFactor(new BigDecimal("0.85")) // 15% off = 0.85 factor
                .startDate(currentDate.minusDays(5))
                .endDate(currentDate.plusDays(20))
                .isActive(true)
                .isVisible(true)
                .build();
    }

    @Test
    @DisplayName("Should validate a valid promo code")
    void validatePromoCode_withValidCode_shouldReturnPromotion() {
        // Arrange
        when(promotionScheduleRepository.findByPromoCodeAndIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Optional.of(validPromotion));

        // Act
        Optional<PropertyPromotionScheduleResponse> result = promoCodeService.validatePromoCode("SUMMER20");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(validPromotion.getId(), result.get().getId());
        assertEquals(validPromotion.getPropertyId(), result.get().getPropertyId());
        assertEquals(validPromotion.getPromotionId(), result.get().getPromotionId());
        assertEquals(validPromotion.getTitle(), result.get().getTitle());
        assertEquals(validPromotion.getDescription(), result.get().getDescription());
        assertEquals(validPromotion.getPromoCode(), result.get().getPromoCode());
        assertEquals(validPromotion.getPriceFactor(), result.get().getPriceFactor());
        assertEquals(validPromotion.getStartDate(), result.get().getStartDate());
        assertEquals(validPromotion.getEndDate(), result.get().getEndDate());
        assertEquals(validPromotion.getIsActive(), result.get().getIsActive());
        assertEquals(validPromotion.getIsVisible(), result.get().getIsVisible());
    }

    @Test
    @DisplayName("Should return empty for invalid promo code")
    void validatePromoCode_withInvalidCode_shouldReturnEmpty() {
        // Arrange
        when(promotionScheduleRepository.findByPromoCodeAndIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        // Act
        Optional<PropertyPromotionScheduleResponse> result = promoCodeService.validatePromoCode("INVALID");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should return empty for null promo code")
    void validatePromoCode_withNullCode_shouldReturnEmpty() {
        // Act
        Optional<PropertyPromotionScheduleResponse> result = promoCodeService.validatePromoCode(null);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should return empty for empty promo code")
    void validatePromoCode_withEmptyCode_shouldReturnEmpty() {
        // Act
        Optional<PropertyPromotionScheduleResponse> result = promoCodeService.validatePromoCode("  ");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should return all visible promotions")
    void getAllVisiblePromotions_shouldReturnAllVisiblePromotions() {
        // Arrange
        List<PropertyPromotionSchedule> promotions = Arrays.asList(validPromotion, secondPromotion);
        when(promotionScheduleRepository.findByIsActiveTrueAndIsVisibleTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(promotions);

        // Act
        List<PropertyPromotionScheduleResponse> result = promoCodeService.getAllVisiblePromotions();

        // Assert
        assertEquals(2, result.size());
        
        // Verify first promotion
        assertEquals(validPromotion.getId(), result.get(0).getId());
        assertEquals(validPromotion.getPropertyId(), result.get(0).getPropertyId());
        assertEquals(validPromotion.getTitle(), result.get(0).getTitle());
        
        // Verify second promotion
        assertEquals(secondPromotion.getId(), result.get(1).getId());
        assertEquals(secondPromotion.getPropertyId(), result.get(1).getPropertyId());
        assertEquals(secondPromotion.getTitle(), result.get(1).getTitle());
    }

    @Test
    @DisplayName("Should return empty list when no visible promotions exist")
    void getAllVisiblePromotions_withNoPromotions_shouldReturnEmptyList() {
        // Arrange
        when(promotionScheduleRepository.findByIsActiveTrueAndIsVisibleTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // Act
        List<PropertyPromotionScheduleResponse> result = promoCodeService.getAllVisiblePromotions();

        // Assert
        assertTrue(result.isEmpty());
    }
} 