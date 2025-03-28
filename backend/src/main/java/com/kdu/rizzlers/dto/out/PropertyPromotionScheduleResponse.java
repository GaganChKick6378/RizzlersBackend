package com.kdu.rizzlers.dto.out;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyPromotionScheduleResponse {
    private Long id;
    private Integer propertyId;
    private Integer promotionId;
    private String title;
    private String description;
    private String promoCode;
    private BigDecimal priceFactor;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;
    private Boolean isVisible;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 