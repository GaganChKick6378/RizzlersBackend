package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.dto.out.DetailsPageConfigResponse;
import com.kdu.rizzlers.service.TenantConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Details Configuration", description = "APIs for managing details page configuration")
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
    @Operation(summary = "Get details page configuration", 
               description = "Retrieves the full details page configuration for a specific tenant")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Configuration retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DetailsPageConfigResponse.class)))
    })
    public ResponseEntity<DetailsPageConfigResponse> getDetailsPageConfiguration(
            @Parameter(description = "Tenant ID", required = true) 
            @PathVariable Integer tenantId) {
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
    @Operation(summary = "Get details page configuration as map", 
               description = "Retrieves the details page configuration as a simplified map for easier client-side consumption")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Configuration map retrieved successfully")
    })
    public ResponseEntity<Map<String, Object>> getDetailsPageConfigurationAsMap(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId) {
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