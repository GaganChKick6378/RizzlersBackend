package com.kdu.rizzlers.dto.out;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for promo code validation with only essential fields
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromoCodeResponse {
    private Integer promotionId;
    private String title;
    private String description;
    private BigDecimal priceFactor;
} 