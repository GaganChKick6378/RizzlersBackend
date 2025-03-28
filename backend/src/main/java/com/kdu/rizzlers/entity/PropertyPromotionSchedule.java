package com.kdu.rizzlers.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "property_promotion_schedule")
public class PropertyPromotionSchedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "property_id", nullable = false)
    private Integer propertyId;

    @Column(name = "promotion_id", nullable = false)
    private Integer promotionId;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "promo_code", unique = true)
    private String promoCode;

    @Column(name = "price_factor", nullable = false, precision = 5, scale = 2)
    private BigDecimal priceFactor = BigDecimal.valueOf(1.0);

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "is_visible")
    private Boolean isVisible = true;

    // Helper method to check if a given date falls within this promotion period
    public boolean isDateInPromotionPeriod(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate) && Boolean.TRUE.equals(isActive);
    }
    
    // Helper method to check if a promotion is valid and visible
    public boolean isValidAndVisible(LocalDate currentDate) {
        return isDateInPromotionPeriod(currentDate) && Boolean.TRUE.equals(isVisible);
    }
} 