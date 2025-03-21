package com.kdu.rizzlers.dto.out;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for Tenant Property Assignment with additional property details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantPropertyAssignmentResponse {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("tenant_id")
    private Integer tenantId;
    
    @JsonProperty("property_id")
    private Integer propertyId;
    
    @JsonProperty("property_name")
    private String propertyName;
    
    @JsonProperty("property_address")
    private String propertyAddress;
    
    @JsonProperty("contact_number")
    private String contactNumber;
    
    @JsonProperty("is_assigned")
    private Boolean isAssigned;
    
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}