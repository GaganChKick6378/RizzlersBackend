package com.kdu.rizzlers.dto.in;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
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
    
    /**
     * Pagination details
     */
    @Builder.Default
    private PaginationDTO pagination = new PaginationDTO();
    
    /**
     * Legacy pagination parameters - used if pagination object is not provided
     */
    @Builder.Default
    private Integer page = 0;
    
    @Builder.Default
    private Integer size = 10;
    
    /**
     * Filters for room search
     */
    private FilterDTO filters;
    
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
    
    /**
     * Get the page number from either the pagination object or legacy field
     */
    public int getPageNumber() {
        Integer pageNumber = null;
        
        // Try to get from pagination object first
        if (pagination != null && pagination.getPage() != null) {
            System.out.println("DEBUG: Getting page from pagination object: " + pagination.getPage());
            pageNumber = pagination.getPage();
        } 
        // Fall back to legacy field
        else if (page != null) {
            System.out.println("DEBUG: Getting page from legacy field: " + page);
            pageNumber = page;
        }
        
        // Default to 0 if not provided or invalid
        System.out.println("DEBUG: Final page number: " + (pageNumber != null && pageNumber >= 0 ? pageNumber : 0));
        return pageNumber != null && pageNumber >= 0 ? pageNumber : 0;
    }
    
    /**
     * Get the page size from either the pagination object or legacy field
     */
    public int getPageSize() {
        Integer sizeValue = null;
        
        // Try to get from pagination object first
        if (pagination != null && pagination.getSize() != null) {
            System.out.println("DEBUG: Getting size from pagination object: " + pagination.getSize());
            sizeValue = pagination.getSize();
        } 
        // Fall back to legacy field
        else if (size != null) {
            System.out.println("DEBUG: Getting size from legacy field: " + size);
            sizeValue = size;
        }
        
        // Default to 10 if not provided, ensure it's at least 1
        System.out.println("DEBUG: Final page size: " + (sizeValue != null && sizeValue > 0 ? sizeValue : 10));
        return sizeValue != null && sizeValue > 0 ? sizeValue : 10;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaginationDTO {
        @Builder.Default
        private Integer page = 0;
        
        @Builder.Default
        private Integer size = 10;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilterDTO {
        private List<String> roomType;
        private List<Integer> ratings;
        private List<String> amenities;
        private List<Integer> priceRange; // [min, max]
        private String sort; // e.g., "price-low-high", "price-high-low", "rating-high-low"
    }
} 