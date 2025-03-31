package com.kdu.rizzlers.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PropertyPromotionScheduleTest {

    @Test
    @DisplayName("Should create a promotion with builder pattern")
    void testBuilder() {
        // Arrange
        Long id = 1L;
        Integer propertyId = 100;
        Integer promotionId = 200;
        String title = "Summer Special";
        String description = "10% off summer bookings";
        String promoCode = "SUMMER10";
        BigDecimal priceFactor = new BigDecimal("0.90");
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(30);
        Boolean isActive = true;
        Boolean isVisible = true;

        // Act
        PropertyPromotionSchedule promotion = PropertyPromotionSchedule.builder()
                .id(id)
                .propertyId(propertyId)
                .promotionId(promotionId)
                .title(title)
                .description(description)
                .promoCode(promoCode)
                .priceFactor(priceFactor)
                .startDate(startDate)
                .endDate(endDate)
                .isActive(isActive)
                .isVisible(isVisible)
                .build();

        // Assert
        assertEquals(id, promotion.getId());
        assertEquals(propertyId, promotion.getPropertyId());
        assertEquals(promotionId, promotion.getPromotionId());
        assertEquals(title, promotion.getTitle());
        assertEquals(description, promotion.getDescription());
        assertEquals(promoCode, promotion.getPromoCode());
        assertEquals(priceFactor, promotion.getPriceFactor());
        assertEquals(startDate, promotion.getStartDate());
        assertEquals(endDate, promotion.getEndDate());
        assertEquals(isActive, promotion.getIsActive());
        assertEquals(isVisible, promotion.getIsVisible());
    }

    @Test
    @DisplayName("Should check if date is in promotion period")
    void isDateInPromotionPeriod() {
        // Arrange
        LocalDate now = LocalDate.now();
        PropertyPromotionSchedule promotion = PropertyPromotionSchedule.builder()
                .startDate(now.minusDays(5))
                .endDate(now.plusDays(5))
                .isActive(true)
                .build();

        // Act & Assert
        assertTrue(promotion.isDateInPromotionPeriod(now)); // Current date is within range
        assertTrue(promotion.isDateInPromotionPeriod(now.minusDays(5))); // Start date is in range
        assertTrue(promotion.isDateInPromotionPeriod(now.plusDays(5))); // End date is in range
        assertFalse(promotion.isDateInPromotionPeriod(now.minusDays(6))); // Before start date
        assertFalse(promotion.isDateInPromotionPeriod(now.plusDays(6))); // After end date
        
        // Test with inactive promotion
        promotion.setIsActive(false);
        assertFalse(promotion.isDateInPromotionPeriod(now)); // Current date but inactive
    }

    @Test
    @DisplayName("Should check if promotion is valid and visible")
    void isValidAndVisible() {
        // Arrange
        LocalDate now = LocalDate.now();
        PropertyPromotionSchedule promotion = PropertyPromotionSchedule.builder()
                .startDate(now.minusDays(5))
                .endDate(now.plusDays(5))
                .isActive(true)
                .isVisible(true)
                .build();

        // Act & Assert
        assertTrue(promotion.isValidAndVisible(now)); // Active and visible promotion
        
        // Test with invisible promotion
        promotion.setIsVisible(false);
        assertFalse(promotion.isValidAndVisible(now)); // Active but invisible
        
        // Test with inactive promotion
        promotion.setIsActive(false);
        promotion.setIsVisible(true);
        assertFalse(promotion.isValidAndVisible(now)); // Inactive but visible
        
        // Test with date outside range
        promotion.setIsActive(true);
        assertFalse(promotion.isValidAndVisible(now.minusDays(10))); // Before start date
        assertFalse(promotion.isValidAndVisible(now.plusDays(10))); // After end date
    }

    @Test
    @DisplayName("Should set default values for new promotion")
    void defaultValues() {
        // Act
        PropertyPromotionSchedule promotion = new PropertyPromotionSchedule();

        // Assert
        assertEquals(BigDecimal.valueOf(1.0), promotion.getPriceFactor());
        assertTrue(promotion.getIsActive());
        assertTrue(promotion.getIsVisible());
    }

    @Test
    @DisplayName("Should verify equals and hashCode methods")
    void testEqualsAndHashCode() {
        // Arrange
        PropertyPromotionSchedule promotion1 = PropertyPromotionSchedule.builder()
                .id(1L)
                .propertyId(100)
                .promotionId(200)
                .build();

        PropertyPromotionSchedule promotion2 = PropertyPromotionSchedule.builder()
                .id(1L)
                .propertyId(100)
                .promotionId(200)
                .build();

        PropertyPromotionSchedule promotion3 = PropertyPromotionSchedule.builder()
                .id(2L)
                .propertyId(100)
                .promotionId(200)
                .build();

        // Act & Assert
        assertEquals(promotion1, promotion2);
        assertEquals(promotion1.hashCode(), promotion2.hashCode());
        assertNotEquals(promotion1, promotion3);
        assertNotEquals(promotion1.hashCode(), promotion3.hashCode());
    }
} 