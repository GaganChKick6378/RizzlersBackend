package com.kdu.rizzlers.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class PropertyPromotionScheduleTest {

    @Test
    public void testPropertyPromotionSchedule_IsDateInPromotionPeriod() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(5);
        LocalDate dateInRange = LocalDate.now();
        LocalDate dateBeforeRange = LocalDate.now().minusDays(10);
        LocalDate dateAfterRange = LocalDate.now().plusDays(10);
        
        PropertyPromotionSchedule promotion = new PropertyPromotionSchedule();
        promotion.setPropertyId(1);
        promotion.setPromotionId(101);
        promotion.setStartDate(startDate);
        promotion.setEndDate(endDate);
        promotion.setPriceFactor(BigDecimal.valueOf(0.8));
        promotion.setIsActive(true);
        
        // When & Then
        assertTrue(promotion.isDateInPromotionPeriod(dateInRange));
        assertFalse(promotion.isDateInPromotionPeriod(dateBeforeRange));
        assertFalse(promotion.isDateInPromotionPeriod(dateAfterRange));
    }
    
    @Test
    public void testPropertyPromotionSchedule_InactiveDateInRange() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(5);
        LocalDate dateInRange = LocalDate.now();
        
        PropertyPromotionSchedule promotion = new PropertyPromotionSchedule();
        promotion.setPropertyId(1);
        promotion.setPromotionId(101);
        promotion.setStartDate(startDate);
        promotion.setEndDate(endDate);
        promotion.setPriceFactor(BigDecimal.valueOf(0.8));
        promotion.setIsActive(false); // Inactive promotion
        
        // When & Then
        assertFalse(promotion.isDateInPromotionPeriod(dateInRange));
    }
    
    @Test
    public void testPropertyPromotionSchedule_Builder() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(5);
        
        // When
        PropertyPromotionSchedule promotion = PropertyPromotionSchedule.builder()
            .id(1L)
            .propertyId(1)
            .promotionId(101)
            .startDate(startDate)
            .endDate(endDate)
            .priceFactor(BigDecimal.valueOf(0.8))
            .isActive(true)
            .build();
        
        // Then
        assertEquals(1L, promotion.getId());
        assertEquals(1, promotion.getPropertyId());
        assertEquals(101, promotion.getPromotionId());
        assertEquals(startDate, promotion.getStartDate());
        assertEquals(endDate, promotion.getEndDate());
        assertEquals(BigDecimal.valueOf(0.8), promotion.getPriceFactor());
        assertTrue(promotion.getIsActive());
    }
    
    @Test
    public void testPropertyPromotionSchedule_DefaultPriceFactor() {
        // When
        PropertyPromotionSchedule promotion = new PropertyPromotionSchedule();
        
        // Then
        assertEquals(BigDecimal.valueOf(1.0), promotion.getPriceFactor());
    }
    
    @Test
    public void testPropertyPromotionSchedule_DefaultIsActive() {
        // When
        PropertyPromotionSchedule promotion = new PropertyPromotionSchedule();
        
        // Then
        assertTrue(promotion.getIsActive());
    }
    
    @Test
    public void testPropertyPromotionSchedule_Getters() {
        // Given
        PropertyPromotionSchedule promotion = new PropertyPromotionSchedule();
        promotion.setId(1L);
        promotion.setPropertyId(1);
        promotion.setPromotionId(101);
        LocalDate startDate = LocalDate.now().minusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(5);
        promotion.setStartDate(startDate);
        promotion.setEndDate(endDate);
        promotion.setPriceFactor(BigDecimal.valueOf(0.8));
        promotion.setIsActive(true);
        
        // When & Then
        assertEquals(1L, promotion.getId());
        assertEquals(1, promotion.getPropertyId());
        assertEquals(101, promotion.getPromotionId());
        assertEquals(startDate, promotion.getStartDate());
        assertEquals(endDate, promotion.getEndDate());
        assertEquals(BigDecimal.valueOf(0.8), promotion.getPriceFactor());
        assertTrue(promotion.getIsActive());
    }
} 