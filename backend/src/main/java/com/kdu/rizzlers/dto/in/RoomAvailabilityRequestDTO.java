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
public class RoomAvailabilityRequestDTO {
    
    private Integer propertyId;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
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
     * Calculate the total guest count from adults, senior citizens, and kids
     * @return The total number of guests
     */
    public Integer getTotalGuestCount() {
        return (adults != null ? adults : 0) + 
               (seniorCitizens != null ? seniorCitizens : 0) + 
               (kids != null ? kids : 0);
    }
} 