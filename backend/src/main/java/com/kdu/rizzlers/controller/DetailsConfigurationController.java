package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.dto.out.DetailsPageConfigResponse;
import com.kdu.rizzlers.service.TenantConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for details page configuration endpoints.
 */
@RestController
@RequestMapping("/details-configuration")
public class DetailsConfigurationController {

    private static final Logger log = LoggerFactory.getLogger(DetailsConfigurationController.class);

    @Autowired
    private TenantConfigurationService tenantConfigurationService;

    /**
     * Get details page configuration for a specific tenant
     *
     * @param tenantId The tenant ID
     * @return Details page configuration response
     */
    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<DetailsPageConfigResponse> getDetailsPageConfiguration(@PathVariable Integer tenantId) {
        log.info("Request received for details page configuration for tenant: {}", tenantId);
        DetailsPageConfigResponse config = tenantConfigurationService.getDetailsPageConfiguration(tenantId);
        return ResponseEntity.ok(config);
    }

    /**
     * Get details page configuration as a map for easier client-side consumption
     *
     * @param tenantId The tenant ID
     * @return Map containing details page configuration
     */
    @GetMapping("/tenant/{tenantId}/map")
    public ResponseEntity<Map<String, Object>> getDetailsPageConfigurationAsMap(@PathVariable Integer tenantId) {
        log.info("Request received for details page configuration as map for tenant: {}", tenantId);
        
        DetailsPageConfigResponse fullConfig = tenantConfigurationService.getDetailsPageConfiguration(tenantId);
        
        // Extract only the configuration fields for easier consumption by clients
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("showImages", fullConfig.getShowImages());
        configMap.put("showDescription", fullConfig.getShowDescription());
        configMap.put("showAmenities", fullConfig.getShowAmenities());
        configMap.put("numAmenities", fullConfig.getNumAmenities());
        
        return ResponseEntity.ok(configMap);
    }
} 