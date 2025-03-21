package com.kdu.rizzlers.dto.out;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyRoomRateDTO {
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    
    @JsonProperty("minimum_rate")
    private Double minimumRate;
    
    @JsonProperty("has_promotion")
    private Boolean hasPromotion;
    
    @JsonProperty("promotion_id")
    private Integer promotionId;
    
    @JsonProperty("price_factor")
    private Double priceFactor;
    
    @JsonProperty("discounted_rate")
    private Double discountedRate;
} 