package com.kdu.rizzlers.entity;

import com.kdu.rizzlers.dto.out.PropertyPromotionDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "property_promotion_schedule")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyPromotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "property_id")
    private Integer propertyId;
    
    @Column(name = "promotion_id")
    private Integer promotionId;
    
    @Column(name = "price_factor")
    private Double priceFactor;
    
    @Column(name = "start_date")
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(name = "is_active")
    private Boolean isActive;
    
    @Column(name = "is_visible")
    private Boolean isVisible;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "title")
    private String title;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "promo_code")
    private String promoCode;
    
    /**
     * Convert to DTO
     */
    public PropertyPromotionDTO toDTO() {
        return PropertyPromotionDTO.builder()
                .id(id)
                .propertyId(propertyId)
                .promotionId(promotionId)
                .priceFactor(priceFactor)
                .startDate(startDate)
                .endDate(endDate)
                .isActive(isActive)
                .isVisible(isVisible)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .title(title)
                .description(description)
                .promoCode(promoCode)
                .build();
    }
} 