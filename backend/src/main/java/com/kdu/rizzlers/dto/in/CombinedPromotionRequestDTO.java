package com.kdu.rizzlers.dto.in;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CombinedPromotionRequestDTO {
    
    private Integer propertyId;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    @Builder.Default
    private Integer adults = 0;
    
    @Builder.Default
    private Integer seniorCitizens = 0;
    
    @Builder.Default
    private Integer kids = 0;
    
    // Additional flags for specific promotions
    @Builder.Default
    private Boolean isMilitaryPersonnel = false;
    
    @Builder.Default
    private Boolean isKduMember = false;
    
    @Builder.Default
    private Boolean isUpfrontPayment = false;
    
    /**
     * Calculate the total length of stay in days
     * @return Number of days between start and end date (inclusive)
     */
    public long getLengthOfStay() {
        if (startDate == null || endDate == null) {
            return 0;
        }
        // Add 1 because the end date is inclusive in our context
        return endDate.toEpochDay() - startDate.toEpochDay() + 1;
    }
    
    /**
     * Check if the booking includes a weekend (Saturday and Sunday)
     * @return true if the booking includes at least one weekend day
     */
    public boolean includesWeekend() {
        if (startDate == null || endDate == null) {
            return false;
        }
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            int dayOfWeek = currentDate.getDayOfWeek().getValue();
            // 6 = Saturday, 7 = Sunday
            if (dayOfWeek == 6 || dayOfWeek == 7) {
                return true;
            }
            currentDate = currentDate.plusDays(1);
        }
        return false;
    }
    
    /**
     * Check if the booking includes both Saturday and Sunday (full weekend)
     * @return true if the booking includes a full weekend
     */
    public boolean includesFullWeekend() {
        if (startDate == null || endDate == null) {
            return false;
        }
        
        boolean hasSaturday = false;
        boolean hasSunday = false;
        
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            int dayOfWeek = currentDate.getDayOfWeek().getValue();
            if (dayOfWeek == 6) {  // Saturday
                hasSaturday = true;
            } else if (dayOfWeek == 7) {  // Sunday
                hasSunday = true;
            }
            
            if (hasSaturday && hasSunday) {
                return true;
            }
            
            currentDate = currentDate.plusDays(1);
        }
        return false;
    }
    
    /**
     * Check if there are senior citizens in the booking
     * @return true if there is at least one senior citizen
     */
    public boolean hasSeniorCitizens() {
        return seniorCitizens != null && seniorCitizens > 0;
    }
    
    /**
     * Convert this combined request to a PromotionEligibilityRequestDTO
     * @return A new PromotionEligibilityRequestDTO with the eligibility fields from this request
     */
    public PromotionEligibilityRequestDTO toEligibilityRequest() {
        return PromotionEligibilityRequestDTO.builder()
                .startDate(this.startDate)
                .endDate(this.endDate)
                .adults(this.adults)
                .seniorCitizens(this.seniorCitizens)
                .kids(this.kids)
                .isMilitaryPersonnel(this.isMilitaryPersonnel)
                .isKduMember(this.isKduMember)
                .isUpfrontPayment(this.isUpfrontPayment)
                .build();
    }
} 