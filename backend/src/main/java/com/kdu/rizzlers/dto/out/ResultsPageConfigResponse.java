package com.kdu.rizzlers.dto.out;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Response object for the results page configuration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultsPageConfigResponse {
    private Integer tenantId;
    private String page;
    private List<GuestTypeDefinitionResponse> guestTypes;
    private List<TenantPropertyAssignmentResponse> properties;
    
    // Results page specific fields
    private Map<String, Object> filters;
    private Map<String, Object> sorting;
    private Map<String, Object> pagination;
    private Map<String, Object> displayOptions;
} 