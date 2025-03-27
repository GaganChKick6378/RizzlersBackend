package com.kdu.rizzlers.repository;

import com.kdu.rizzlers.entity.PropertyPromotionSchedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class PropertyPromotionScheduleRepositoryTest {

    @Autowired
    private PropertyPromotionScheduleRepository repository;
    
    @BeforeEach
    public void setup() {
        // Clean up the repository
        repository.deleteAll();
        
        // Add test data programmatically
        LocalDateTime now = LocalDateTime.now();
        
        PropertyPromotionSchedule promotion1 = PropertyPromotionSchedule.builder()
                .propertyId(1)
                .promotionId(101)
                .priceFactor(BigDecimal.valueOf(0.8))
                .startDate(LocalDate.now().minusDays(10))
                .endDate(LocalDate.now().plusDays(10))
                .isActive(true)
                .build();
        promotion1.setCreatedAt(now);
        
        PropertyPromotionSchedule promotion2 = PropertyPromotionSchedule.builder()
                .propertyId(1)
                .promotionId(102)
                .priceFactor(BigDecimal.valueOf(0.85))
                .startDate(LocalDate.now().minusDays(20))
                .endDate(LocalDate.now().minusDays(5))
                .isActive(true)
                .build();
        promotion2.setCreatedAt(now);
        
        PropertyPromotionSchedule promotion3 = PropertyPromotionSchedule.builder()
                .propertyId(1)
                .promotionId(103)
                .priceFactor(BigDecimal.valueOf(0.75))
                .startDate(LocalDate.now().plusDays(5))
                .endDate(LocalDate.now().plusDays(20))
                .isActive(true)
                .build();
        promotion3.setCreatedAt(now);
        
        PropertyPromotionSchedule promotion4 = PropertyPromotionSchedule.builder()
                .propertyId(2)
                .promotionId(201)
                .priceFactor(BigDecimal.valueOf(0.9))
                .startDate(LocalDate.now().minusDays(15))
                .endDate(LocalDate.now().plusDays(15))
                .isActive(true)
                .build();
        promotion4.setCreatedAt(now);
        
        PropertyPromotionSchedule promotion5 = PropertyPromotionSchedule.builder()
                .propertyId(1)
                .promotionId(104)
                .priceFactor(BigDecimal.valueOf(0.7))
                .startDate(LocalDate.now().minusDays(5))
                .endDate(LocalDate.now().plusDays(5))
                .isActive(false)
                .build();
        promotion5.setCreatedAt(now);
                
        // Save each entity separately to avoid batch insert issues
        repository.save(promotion1);
        repository.save(promotion2);
        repository.save(promotion3);
        repository.save(promotion4);
        repository.save(promotion5);
    }

    @Test
    public void testFindAllByPropertyId() {
        // Given
        Integer propertyId = 1;
        
        // When
        List<PropertyPromotionSchedule> promotions = repository.findAllByPropertyId(propertyId);
        
        // Then
        assertFalse(promotions.isEmpty());
        assertEquals(4, promotions.size()); // We have 4 promotions for property 1
        promotions.forEach(promotion -> assertEquals(propertyId, promotion.getPropertyId()));
    }

    @Test
    public void testFindActivePromotionsForPropertyInPeriod() {
        // Given
        Integer propertyId = 1;
        LocalDate startDate = LocalDate.now().minusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(5);
        
        // When
        List<PropertyPromotionSchedule> promotions = repository.findActivePromotionsForPropertyInPeriod(
                propertyId, startDate, endDate);
        
        // Then
        assertFalse(promotions.isEmpty());
        // Based on our test data, all 3 active promotions for property 1 overlap with our date range
        assertEquals(3, promotions.size()); 
        promotions.forEach(promotion -> {
            assertEquals(propertyId, promotion.getPropertyId());
            assertTrue(promotion.getIsActive());
            // Verify that the promotion period overlaps with our test period
            assertTrue(
                (promotion.getStartDate().isBefore(endDate) || promotion.getStartDate().isEqual(endDate)) &&
                (promotion.getEndDate().isAfter(startDate) || promotion.getEndDate().isEqual(startDate))
            );
        });
    }

    @Test
    public void testFindByPropertyIdAndIsActiveTrue() {
        // Given
        Integer propertyId = 1;
        
        // When
        List<PropertyPromotionSchedule> promotions = repository.findByPropertyIdAndIsActiveTrue(propertyId);
        
        // Then
        assertFalse(promotions.isEmpty());
        assertEquals(3, promotions.size()); // 3 active promotions for property 1
        promotions.forEach(promotion -> {
            assertEquals(propertyId, promotion.getPropertyId());
            assertTrue(promotion.getIsActive());
        });
    }

    @Test
    public void testIsDateInPromotionPeriod() {
        // Given
        LocalDate startDate = LocalDate.now().minusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(5);
        LocalDate dateInRange = LocalDate.now();
        LocalDate dateBeforeRange = LocalDate.now().minusDays(10);
        LocalDate dateAfterRange = LocalDate.now().plusDays(10);
        
        PropertyPromotionSchedule promotion = PropertyPromotionSchedule.builder()
                .propertyId(1)
                .promotionId(1)
                .startDate(startDate)
                .endDate(endDate)
                .priceFactor(BigDecimal.valueOf(0.8))
                .isActive(true)
                .build();
                
        // When & Then
        assertTrue(promotion.isDateInPromotionPeriod(dateInRange));
        assertFalse(promotion.isDateInPromotionPeriod(dateBeforeRange));
        assertFalse(promotion.isDateInPromotionPeriod(dateAfterRange));
        
        // Test with inactive promotion
        promotion.setIsActive(false);
        assertFalse(promotion.isDateInPromotionPeriod(dateInRange));
    }
} 