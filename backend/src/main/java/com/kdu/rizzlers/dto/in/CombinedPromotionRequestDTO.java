package com.kdu.rizzlers.dto.in;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

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
    
    /**
     * Total number of guests for pricing and promotion calculations.
     * If provided, this overrides the sum of adults, kids, and seniorCitizens.
     */
    private Integer guests;
    
    /**
     * Structured breakdown of different guest types
     */
    @Builder.Default
    private Map<String, Integer> guestCount = new HashMap<>();
    
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
     * Get the total guest count for promotion calculations
     * Prioritizes the explicit guests field if provided
     * 
     * @return The total number of guests
     */
    public Integer getTotalGuestCount() {
        // If explicit guests count is provided, use that
        if (guests != null) {
            return guests;
        }
        
        // If guestCount map is not empty, sum its values
        if (guestCount != null && !guestCount.isEmpty()) {
            return guestCount.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
        }
        
        // Otherwise use the legacy fields
        return (adults != null ? adults : 0) + 
               (seniorCitizens != null ? seniorCitizens : 0) + 
               (kids != null ? kids : 0);
    }
    
    /**
     * Convert this combined request to a PromotionEligibilityRequestDTO
     * @return A new PromotionEligibilityRequestDTO with the eligibility fields from this request
     */
    public PromotionEligibilityRequestDTO toEligibilityRequest() {
        return PromotionEligibilityRequestDTO.builder()
                .startDate(this.startDate)
                .endDate(this.endDate)
                .guests(this.guests)
                .guestCount(this.guestCount)
                .adults(this.adults)
                .seniorCitizens(this.seniorCitizens)
                .kids(this.kids)
                .isMilitaryPersonnel(this.isMilitaryPersonnel)
                .isKduMember(this.isKduMember)
                .isUpfrontPayment(this.isUpfrontPayment)
                .build();
    }
} 