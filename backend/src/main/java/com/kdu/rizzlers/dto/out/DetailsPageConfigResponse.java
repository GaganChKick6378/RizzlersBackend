package com.kdu.rizzlers.dto.out;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response DTO for details page configuration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetailsPageConfigResponse {
    private Integer tenantId;
    private String page;
    
    // Image display configuration
    private Map<String, Object> showImages;
    
    // Description display configuration
    private Map<String, Object> showDescription;
    
    // Amenities display configuration
    private Map<String, Object> showAmenities;
    
    // Number of amenities to display configuration
    private Map<String, Object> numAmenities;
} 