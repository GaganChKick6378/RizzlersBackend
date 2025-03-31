package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.out.PropertyPromotionScheduleResponse;
import com.kdu.rizzlers.entity.PropertyPromotionSchedule;
import com.kdu.rizzlers.repository.PropertyPromotionScheduleRepository;
import com.kdu.rizzlers.service.impl.PromoCodeServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromoCodeServiceTest {

    @Mock
    private PropertyPromotionScheduleRepository promotionRepository;

    @InjectMocks
    private PromoCodeServiceImpl promoCodeService;

    @Test
    @DisplayName("PromoCodeService interface is correctly implemented")
    void testInterface() {
        // This test verifies that PromoCodeServiceImpl implements PromoCodeService
        PromoCodeService service = promoCodeService;
        assertNotNull(service);
    }

    @Test
    @DisplayName("validatePromoCode should return empty when promo code is invalid")
    void validatePromoCode_whenInvalid_shouldReturnEmpty() {
        when(promotionRepository.findByPromoCodeAndIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Optional.empty());

        Optional<PropertyPromotionScheduleResponse> result = promoCodeService.validatePromoCode("INVALID");
        
        assertTrue(result.isEmpty());
        verify(promotionRepository).findByPromoCodeAndIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                eq("INVALID"), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    @DisplayName("validatePromoCode should return promotion when promo code is valid")
    void validatePromoCode_whenValid_shouldReturnPromotion() {
        // Create mock promotion
        PropertyPromotionSchedule promotion = new PropertyPromotionSchedule();
        promotion.setId(1L);
        promotion.setPromoCode("VALID10");
        promotion.setTitle("Valid Promotion");
        promotion.setDescription("10% off for valid promotion");
        
        when(promotionRepository.findByPromoCodeAndIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Optional.of(promotion));

        Optional<PropertyPromotionScheduleResponse> result = promoCodeService.validatePromoCode("VALID10");
        
        assertTrue(result.isPresent());
        assertEquals("VALID10", result.get().getPromoCode());
        assertEquals("Valid Promotion", result.get().getTitle());
        verify(promotionRepository).findByPromoCodeAndIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                eq("VALID10"), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    @DisplayName("getAllVisiblePromotions should return list of all visible promotions")
    void getAllVisiblePromotions_shouldReturnListOfAllVisiblePromotions() {
        // Create mock promotions
        List<PropertyPromotionSchedule> promotions = new ArrayList<>();
        PropertyPromotionSchedule promotion1 = new PropertyPromotionSchedule();
        promotion1.setId(1L);
        promotion1.setPromoCode("PROMO1");
        promotion1.setTitle("Promotion 1");
        
        PropertyPromotionSchedule promotion2 = new PropertyPromotionSchedule();
        promotion2.setId(2L);
        promotion2.setPromoCode("PROMO2");
        promotion2.setTitle("Promotion 2");
        
        promotions.add(promotion1);
        promotions.add(promotion2);
        
        when(promotionRepository.findByIsActiveTrueAndIsVisibleTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(promotions);

        List<PropertyPromotionScheduleResponse> result = promoCodeService.getAllVisiblePromotions();
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("PROMO1", result.get(0).getPromoCode());
        assertEquals("PROMO2", result.get(1).getPromoCode());
        verify(promotionRepository).findByIsActiveTrueAndIsVisibleTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                any(LocalDate.class), any(LocalDate.class));
    }
} 