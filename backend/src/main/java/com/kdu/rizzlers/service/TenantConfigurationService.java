package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.in.TenantConfigurationRequest;
import com.kdu.rizzlers.dto.out.LandingPageConfigResponse;
import com.kdu.rizzlers.dto.out.TenantConfigurationResponse;

import java.util.List;

public interface TenantConfigurationService {
    TenantConfigurationResponse createConfiguration(TenantConfigurationRequest request);
    TenantConfigurationResponse getConfigurationById(Long id);
    List<TenantConfigurationResponse> getAllConfigurations();
    List<TenantConfigurationResponse> getConfigurationsByTenantId(Integer tenantId);
    List<TenantConfigurationResponse> getConfigurationsByTenantIdAndPage(Integer tenantId, String page);
    TenantConfigurationResponse getConfigurationByTenantIdAndPageAndField(Integer tenantId, String page, String field);
    TenantConfigurationResponse updateConfiguration(Long id, TenantConfigurationRequest request);
    void deleteConfiguration(Long id);
    
    // New method to get complete landing page configuration
    LandingPageConfigResponse getLandingPageConfiguration(Integer tenantId);

    /**
     * Get landing page configuration for a specific tenant
     * 
     * @param tenantId The tenant ID
     * @param fetchPropertyDetails Whether to fetch property details from GraphQL service
     * @return Landing page configuration response
     */
    LandingPageConfigResponse getLandingPageConfiguration(Integer tenantId, boolean fetchPropertyDetails);
} 