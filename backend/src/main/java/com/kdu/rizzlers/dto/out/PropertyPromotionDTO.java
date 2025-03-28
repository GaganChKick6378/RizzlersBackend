package com.kdu.rizzlers.dto.out;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyPromotionDTO {
    private Long id;
    
    @JsonProperty("property_id")
    private Integer propertyId;
    
    @JsonProperty("promotion_id")
    private Integer promotionId;
    
    @JsonProperty("price_factor")
    private Double priceFactor;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("start_date")
    private LocalDate startDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("end_date")
    private LocalDate endDate;
    
    @JsonProperty("is_active")
    private Boolean isActive;
    
    @JsonProperty("is_visible")
    private Boolean isVisible;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("promo_code")
    private String promoCode;
    
    /**
     * Convert to the standard PromotionDTO format
     */
    public PromotionDTO toPromotionDTO() {
        return PromotionDTO.builder()
                .promotionId(promotionId)
                .promotionTitle(title)
                .promotionDescription(description)
                .priceFactor(priceFactor)
                .minimumDaysOfStay(1) // Default to 1 as per requirement
                .isDeactivated(!isActive || !isVisible) // Promotion is considered deactivated if not active or not visible
                .build();
    }
} 