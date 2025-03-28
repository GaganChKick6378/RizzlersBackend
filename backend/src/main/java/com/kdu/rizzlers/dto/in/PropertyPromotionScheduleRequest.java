package com.kdu.rizzlers.dto.in;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyPromotionScheduleRequest {

    @NotNull(message = "Property ID is required")
    private Integer propertyId;

    @NotNull(message = "Promotion ID is required")
    private Integer promotionId;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    private String promoCode;
    
    @NotNull(message = "Price factor is required")
    @DecimalMin(value = "0.01", message = "Price factor must be greater than 0")
    private BigDecimal priceFactor;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;
    
    private Boolean isActive = true;
    
    private Boolean isVisible = true;
} 