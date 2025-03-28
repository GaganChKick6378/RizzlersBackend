package com.kdu.rizzlers.dto.in;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for validating promo codes
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromoCodeValidateRequest {
    
    @NotBlank(message = "Promo code is required")
    private String promoCode;
} 