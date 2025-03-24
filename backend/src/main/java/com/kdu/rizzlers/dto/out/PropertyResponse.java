package com.kdu.rizzlers.dto.out;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for Property information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyResponse {
    
    @JsonProperty("property_id")
    private Integer propertyId;
    
    @JsonProperty("property_name")
    private String propertyName;
    
    @JsonProperty("property_address")
    private String propertyAddress;
    
    @JsonProperty("contact_number")
    private String contactNumber;
    
    @JsonProperty("tenant_id")
    private Integer tenantId;
} 