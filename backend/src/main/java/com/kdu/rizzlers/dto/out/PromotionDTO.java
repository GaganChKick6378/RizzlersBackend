package com.kdu.rizzlers.dto.out;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromotionDTO {
    @JsonProperty("promotion_id")
    private Integer promotionId;
    
    @JsonProperty("promotion_title")
    private String promotionTitle;
    
    @JsonProperty("promotion_description")
    private String promotionDescription;
    
    @JsonProperty("price_factor")
    private Double priceFactor;
    
    @JsonProperty("minimum_days_of_stay")
    private Integer minimumDaysOfStay;
    
    @JsonProperty("is_deactivated")
    private Boolean isDeactivated;
} 