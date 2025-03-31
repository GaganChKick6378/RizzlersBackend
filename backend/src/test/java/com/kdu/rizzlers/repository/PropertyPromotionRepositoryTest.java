package com.kdu.rizzlers.repository;

import com.kdu.rizzlers.entity.PropertyPromotion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class PropertyPromotionRepositoryTest {

    @Autowired
    private PropertyPromotionRepository propertyPromotionRepository;

    @Test
    @DisplayName("Should find active promotions in date range")
    @Sql("/sql/insert-test-promotions.sql")
    void findActivePromotionsInDateRange() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(5);

        // Act
        List<PropertyPromotion> promotions = propertyPromotionRepository.findActivePromotionsInDateRange(startDate, endDate);

        // Assert
        assertFalse(promotions.isEmpty());
        promotions.forEach(promotion -> {
            assertTrue(promotion.getIsActive());
            assertFalse(promotion.getEndDate().isBefore(startDate));
            assertFalse(promotion.getStartDate().isAfter(endDate));
        });
    }

    @Test
    @DisplayName("Should find active and visible promotions in date range")
    @Sql("/sql/insert-test-promotions.sql")
    void findActiveAndVisiblePromotionsInDateRange() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(5);

        // Act
        List<PropertyPromotion> promotions = propertyPromotionRepository.findActiveAndVisiblePromotionsInDateRange(startDate, endDate);

        // Assert
        assertFalse(promotions.isEmpty());
        promotions.forEach(promotion -> {
            assertTrue(promotion.getIsActive());
            assertTrue(promotion.getIsVisible());
            assertFalse(promotion.getEndDate().isBefore(startDate));
            assertFalse(promotion.getStartDate().isAfter(endDate));
        });
    }

    @Test
    @DisplayName("Should find active promotions for property in date range")
    @Sql("/sql/insert-test-promotions.sql")
    void findActivePromotionsForPropertyInDateRange() {
        // Arrange
        Integer propertyId = 1;
        LocalDate startDate = LocalDate.now().minusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(5);

        // Act
        List<PropertyPromotion> promotions = propertyPromotionRepository.findActivePromotionsForPropertyInDateRange(
                propertyId, startDate, endDate);

        // Assert
        assertFalse(promotions.isEmpty());
        promotions.forEach(promotion -> {
            assertTrue(promotion.getIsActive());
            assertEquals(propertyId, promotion.getPropertyId());
            assertFalse(promotion.getEndDate().isBefore(startDate));
            assertFalse(promotion.getStartDate().isAfter(endDate));
        });
    }

    @Test
    @DisplayName("Should find active and visible promotions for property in date range")
    @Sql("/sql/insert-test-promotions.sql")
    void findActiveAndVisiblePromotionsForPropertyInDateRange() {
        // Arrange
        Integer propertyId = 1;
        LocalDate startDate = LocalDate.now().minusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(5);

        // Act
        List<PropertyPromotion> promotions = propertyPromotionRepository.findActiveAndVisiblePromotionsForPropertyInDateRange(
                propertyId, startDate, endDate);

        // Assert
        assertFalse(promotions.isEmpty());
        promotions.forEach(promotion -> {
            assertTrue(promotion.getIsActive());
            assertTrue(promotion.getIsVisible());
            assertEquals(propertyId, promotion.getPropertyId());
            assertFalse(promotion.getEndDate().isBefore(startDate));
            assertFalse(promotion.getStartDate().isAfter(endDate));
        });
    }

    @Test
    @DisplayName("Should convert entity to DTO")
    void toDTO() {
        // Arrange
        PropertyPromotion promotion = PropertyPromotion.builder()
                .id(1L)
                .propertyId(1)
                .promotionId(1)
                .title("Test Promotion")
                .description("Test Description")
                .promoCode("TEST123")
                .priceFactor(0.9)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(10))
                .isActive(true)
                .isVisible(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Act
        var dto = promotion.toDTO();

        // Assert
        assertEquals(promotion.getId(), dto.getId());
        assertEquals(promotion.getPropertyId(), dto.getPropertyId());
        assertEquals(promotion.getPromotionId(), dto.getPromotionId());
        assertEquals(promotion.getTitle(), dto.getTitle());
        assertEquals(promotion.getDescription(), dto.getDescription());
        assertEquals(promotion.getPromoCode(), dto.getPromoCode());
        assertEquals(promotion.getPriceFactor(), dto.getPriceFactor());
        assertEquals(promotion.getStartDate(), dto.getStartDate());
        assertEquals(promotion.getEndDate(), dto.getEndDate());
        assertEquals(promotion.getIsActive(), dto.getIsActive());
        assertEquals(promotion.getIsVisible(), dto.getIsVisible());
        assertEquals(promotion.getCreatedAt(), dto.getCreatedAt());
        assertEquals(promotion.getUpdatedAt(), dto.getUpdatedAt());
    }
} 