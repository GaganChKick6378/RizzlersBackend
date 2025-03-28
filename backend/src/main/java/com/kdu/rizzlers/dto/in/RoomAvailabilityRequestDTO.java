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
public class RoomAvailabilityRequestDTO {
    
    private Integer propertyId;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    /**
     * Total number of guests for pricing and capacity calculation.
     * This is the primary value used for determining room capacity requirements.
     * If provided, this overrides the sum of adults, kids, and seniorCitizens.
     */
    private Integer guests;
    
    /**
     * Structured breakdown of different guest types
     */
    @Builder.Default
    private Map<String, Integer> guestCount = new HashMap<>();
    
    @Builder.Default
    private Integer adults = 2;
    
    @Builder.Default
    private Integer seniorCitizens = 0;
    
    @Builder.Default
    private Integer kids = 0;
    
    @Builder.Default
    private Integer roomCount = 1;
    
    // Pagination parameters
    @Builder.Default
    private Integer page = 0;
    
    @Builder.Default
    private Integer size = 10;
    
    /**
     * Get the total guest count for pricing and capacity calculations
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
} 