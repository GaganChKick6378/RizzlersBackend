package com.kdu.rizzlers.dto.in;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;
} 