package com.kdu.rizzlers.dto.out;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response DTO for checkout page configuration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutPageConfigResponse {
    @JsonIgnore
    private Integer tenantId;
    
    @JsonIgnore
    private String page;
    
    // Traveler information configuration
    private Map<String, Object> travelerInfo;
    
    // Billing information configuration
    private Map<String, Object> billingInfo;
    
    // Payment information configuration
    private Map<String, Object> paymentInfo;
} 