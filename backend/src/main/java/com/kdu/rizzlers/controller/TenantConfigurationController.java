package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.dto.in.TenantConfigurationRequest;
import com.kdu.rizzlers.dto.out.CheckoutPageConfigResponse;
import com.kdu.rizzlers.dto.out.LandingPageConfigResponse;
import com.kdu.rizzlers.dto.out.ResultsPageConfigResponse;
import com.kdu.rizzlers.dto.out.TenantConfigurationResponse;
import com.kdu.rizzlers.cache.CacheService;
import com.kdu.rizzlers.service.TenantConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/tenant-configurations")
@Tag(name = "Tenant Configuration", description = "APIs for managing tenant UI configurations")
public class TenantConfigurationController {

    private static final Logger log = LoggerFactory.getLogger(TenantConfigurationController.class);

    @Autowired
    private TenantConfigurationService tenantConfigurationService;

    @Autowired
    private CacheService cacheService;

    @PostMapping
    @Operation(summary = "Create configuration", description = "Creates a new tenant configuration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Configuration created successfully",
                   content = @Content(schema = @Schema(implementation = TenantConfigurationResponse.class)))
    })
    public ResponseEntity<TenantConfigurationResponse> createConfiguration(
            @Parameter(description = "Tenant configuration request", required = true)
            @Valid @RequestBody TenantConfigurationRequest request) {
        String cacheKey = "tenant-config:create";
        TenantConfigurationResponse cached = cacheService.get(cacheKey, TenantConfigurationResponse.class);
        if (cached != null) {
            return ResponseEntity.ok(cached);
        }
        TenantConfigurationResponse response = tenantConfigurationService.createConfiguration(request);
        cacheService.set(cacheKey, response, 10, TimeUnit.MINUTES);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get configuration by ID", description = "Retrieves a tenant configuration by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Configuration found",
                   content = @Content(schema = @Schema(implementation = TenantConfigurationResponse.class))),
        @ApiResponse(responseCode = "404", description = "Configuration not found")
    })
    public ResponseEntity<TenantConfigurationResponse> getConfigurationById(
            @Parameter(description = "Configuration ID", required = true)
            @PathVariable Long id) {
        String cacheKey = "tenant-config:id:" + id;
        TenantConfigurationResponse cached = cacheService.get(cacheKey, TenantConfigurationResponse.class);
        if (cached != null) {
            return ResponseEntity.ok(cached);
        }
        TenantConfigurationResponse response = tenantConfigurationService.getConfigurationById(id);
        cacheService.set(cacheKey, response, 10, TimeUnit.MINUTES);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all configurations", description = "Retrieves all tenant configurations")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "All configurations retrieved",
                   content = @Content(schema = @Schema(implementation = TenantConfigurationResponse.class)))
    })
    public ResponseEntity<List<TenantConfigurationResponse>> getAllConfigurations() {
        String cacheKey = "tenant-config:all";
        List<TenantConfigurationResponse> cached = cacheService.get(cacheKey, List.class);
        if (cached != null) {
            return ResponseEntity.ok(cached);
        }
        List<TenantConfigurationResponse> configs = tenantConfigurationService.getAllConfigurations();
        cacheService.set(cacheKey, configs, 10, TimeUnit.MINUTES);
        return ResponseEntity.ok(configs);
    }

    @GetMapping("/tenant/{tenantId}")
    @Operation(summary = "Get configurations by tenant ID", 
              description = "Retrieves all configurations for a specific tenant")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Configurations retrieved",
                   content = @Content(schema = @Schema(implementation = TenantConfigurationResponse.class)))
    })
    public ResponseEntity<List<TenantConfigurationResponse>> getConfigurationsByTenantId(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId) {
        String cacheKey = "tenant-config:tenant:" + tenantId;
        List<TenantConfigurationResponse> cached = cacheService.get(cacheKey, List.class);
        if (cached != null) {
            return ResponseEntity.ok(cached);
        }
        List<TenantConfigurationResponse> configs = tenantConfigurationService.getConfigurationsByTenantId(tenantId);
        cacheService.set(cacheKey, configs, 10, TimeUnit.MINUTES);
        return ResponseEntity.ok(configs);
    }

    @GetMapping("/tenant/{tenantId}/page/{page}")
    @Operation(summary = "Get configurations by tenant ID and page", 
              description = "Retrieves configurations for a specific tenant and page")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Configurations retrieved",
                   content = @Content(schema = @Schema(implementation = TenantConfigurationResponse.class)))
    })
    public ResponseEntity<List<TenantConfigurationResponse>> getConfigurationsByTenantIdAndPage(
            @Parameter(description = "Tenant ID", required = true) 
            @PathVariable Integer tenantId, 
            
            @Parameter(description = "Page name", required = true) 
            @PathVariable String page) {
        String cacheKey = "tenant-config:tenant:" + tenantId + ":page:" + page;
        List<TenantConfigurationResponse> cached = cacheService.get(cacheKey, List.class);
        if (cached != null) {
            return ResponseEntity.ok(cached);
        }
        List<TenantConfigurationResponse> configs = tenantConfigurationService.getConfigurationsByTenantIdAndPage(tenantId, page);
        cacheService.set(cacheKey, configs, 10, TimeUnit.MINUTES);
        return ResponseEntity.ok(configs);
    }

    @GetMapping("/tenant/{tenantId}/page/{page}/field/{field}")
    @Operation(summary = "Get configuration by tenant ID, page and field", 
              description = "Retrieves a specific configuration for a tenant, page and field")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Configuration retrieved",
                   content = @Content(schema = @Schema(implementation = TenantConfigurationResponse.class))),
        @ApiResponse(responseCode = "404", description = "Configuration not found")
    })
    public ResponseEntity<TenantConfigurationResponse> getConfigurationByTenantIdAndPageAndField(
            @Parameter(description = "Tenant ID", required = true) 
            @PathVariable Integer tenantId, 
            
            @Parameter(description = "Page name", required = true) 
            @PathVariable String page, 
            
            @Parameter(description = "Field name", required = true) 
            @PathVariable String field) {
        String cacheKey = "tenant-config:tenant:" + tenantId + ":page:" + page + ":field:" + field;
        TenantConfigurationResponse cached = cacheService.get(cacheKey, TenantConfigurationResponse.class);
        if (cached != null) {
            return ResponseEntity.ok(cached);
        }
        TenantConfigurationResponse config = tenantConfigurationService.getConfigurationByTenantIdAndPageAndField(tenantId, page, field);
        cacheService.set(cacheKey, config, 10, TimeUnit.MINUTES);
        return ResponseEntity.ok(config);
    }
    
    @GetMapping("/tenant/{tenantId}/landing")
    @Operation(summary = "Get landing page configuration", 
              description = "Retrieves landing page configuration for a tenant")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Configuration retrieved",
                   content = @Content(schema = @Schema(implementation = LandingPageConfigResponse.class)))
    })
    public ResponseEntity<LandingPageConfigResponse> getLandingPageConfiguration(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId) {
        String cacheKey = "tenant-config:tenant:" + tenantId + ":landing";
        LandingPageConfigResponse cached = cacheService.get(cacheKey, LandingPageConfigResponse.class);
        if (cached != null) {
            return ResponseEntity.ok(cached);
        }
        LandingPageConfigResponse config = tenantConfigurationService.getLandingPageConfiguration(tenantId);
        cacheService.set(cacheKey, config, 10, TimeUnit.MINUTES);
        return ResponseEntity.ok(config);
    }

    @GetMapping("/tenant/{tenantId}/landing/basic")
    @Operation(summary = "Get basic landing page configuration", 
              description = "Retrieves basic landing page configuration for a tenant with optional property details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Configuration retrieved",
                   content = @Content(schema = @Schema(implementation = LandingPageConfigResponse.class)))
    })
    public ResponseEntity<LandingPageConfigResponse> getBasicLandingPageConfiguration(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            
            @Parameter(description = "Whether to fetch property details", example = "false")
            @RequestParam(value = "fetch_property_details", defaultValue = "false") boolean fetchPropertyDetails) {
        String cacheKey = "tenant-config:tenant:" + tenantId + ":landing:basic:" + fetchPropertyDetails;
        LandingPageConfigResponse cached = cacheService.get(cacheKey, LandingPageConfigResponse.class);
        if (cached != null) {
            return ResponseEntity.ok(cached);
        }
        LandingPageConfigResponse config = tenantConfigurationService.getLandingPageConfiguration(tenantId, fetchPropertyDetails);
        cacheService.set(cacheKey, config, 10, TimeUnit.MINUTES);
        return ResponseEntity.ok(config);
    }

    @GetMapping("/tenant/{tenantId}/results")
    @Operation(summary = "Get results page configuration", 
              description = "Retrieves results page configuration for a tenant")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Configuration retrieved")
    })
    public ResponseEntity<Map<String, Object>> getResultsPageConfiguration(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId) {
        String cacheKey = "tenant-config:tenant:" + tenantId + ":results";
        Map<String, Object> cached = cacheService.get(cacheKey, Map.class);
        if (cached != null) {
            return ResponseEntity.ok(cached);
        }
        ResultsPageConfigResponse fullConfig = tenantConfigurationService.getResultsPageConfiguration(tenantId);
        
        // Add logging to see what's coming from the service
        log.info("Tenant {} results page config - filters map keys: {}", tenantId, 
                 fullConfig.getFilters().keySet());
        
        log.info("Tenant {} results page config - filters enabled: {}", tenantId, 
                 fullConfig.getFilters().containsKey("enabled") ? fullConfig.getFilters().get("enabled") : "not found");
        
        if (fullConfig.getFilters().containsKey("show")) {
            log.info("Tenant {} results page config - filters 'show' value: {}", tenantId, fullConfig.getFilters().get("show"));
        }
        
        // Log property image if available
        if (fullConfig.getPropertyImage() != null) {
            log.info("Tenant {} results page config - property image: {}", tenantId, fullConfig.getPropertyImage());
        }
        
        // Extract only the configuration fields requested
        Map<String, Object> configOnly = new HashMap<>();
        
        // Create a modified filters map that respects the 'show' property if it exists
        Map<String, Object> filtersMap = new HashMap<>(fullConfig.getFilters());
        if (filtersMap.containsKey("show")) {
            // If 'show' exists and is false, set 'enabled' to false as well
            Boolean showValue = (Boolean) filtersMap.get("show");
            if (showValue != null && !showValue) {
                filtersMap.put("enabled", false);
                log.info("Tenant {} - Setting 'enabled' to false because 'show' is false", tenantId);
            }
        }
        
        configOnly.put("filters", filtersMap);
        configOnly.put("sorting", fullConfig.getSorting());
        configOnly.put("pagination", fullConfig.getPagination());
        configOnly.put("displayOptions", fullConfig.getDisplayOptions());
        configOnly.put("propertyImage", fullConfig.getPropertyImage());
        
        // Log the final response
        log.info("Tenant {} results page final response - filters enabled: {}", tenantId, 
                 configOnly.get("filters") instanceof Map ? ((Map<String, Object>)configOnly.get("filters")).get("enabled") : "not a map");
        
        cacheService.set(cacheKey, configOnly, 10, TimeUnit.MINUTES);
        return ResponseEntity.ok(configOnly);
    }

    @GetMapping("/tenant/{tenantId}/results/basic")
    @Operation(summary = "Get basic results page configuration", 
              description = "Retrieves basic results page configuration for a tenant with optional property details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Configuration retrieved")
    })
    public ResponseEntity<Map<String, Object>> getBasicResultsPageConfiguration(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId,
            
            @Parameter(description = "Whether to fetch property details", example = "false")
            @RequestParam(value = "fetch_property_details", defaultValue = "false") boolean fetchPropertyDetails) {
        
        String cacheKey = "tenant-config:tenant:" + tenantId + ":results:basic:" + fetchPropertyDetails;
        Map<String, Object> cached = cacheService.get(cacheKey, Map.class);
        if (cached != null) {
            return ResponseEntity.ok(cached);
        }
        ResultsPageConfigResponse fullConfig = tenantConfigurationService.getResultsPageConfiguration(tenantId, fetchPropertyDetails);
        
        // Add logging
        log.info("Basic endpoint: Tenant {} results page config - filters map keys: {}", tenantId, 
                 fullConfig.getFilters().keySet());
                 
        // Log property image if available
        if (fullConfig.getPropertyImage() != null) {
            log.info("Basic endpoint: Tenant {} results page config - property image: {}", tenantId, fullConfig.getPropertyImage());
        }
        
        // Extract only the configuration fields requested
        Map<String, Object> configOnly = new HashMap<>();
        
        // Create a modified filters map that respects the 'show' property if it exists
        Map<String, Object> filtersMap = new HashMap<>(fullConfig.getFilters());
        if (filtersMap.containsKey("show")) {
            // If 'show' exists and is false, set 'enabled' to false as well
            Boolean showValue = (Boolean) filtersMap.get("show");
            if (showValue != null && !showValue) {
                filtersMap.put("enabled", false);
                log.info("Basic endpoint: Tenant {} - Setting 'enabled' to false because 'show' is false", tenantId);
            }
        }
        
        configOnly.put("filters", filtersMap);
        configOnly.put("sorting", fullConfig.getSorting());
        configOnly.put("pagination", fullConfig.getPagination());
        configOnly.put("displayOptions", fullConfig.getDisplayOptions());
        configOnly.put("propertyImage", fullConfig.getPropertyImage());
        
        // Log the final response
        log.info("Basic endpoint: Tenant {} results page final response - filters enabled: {}", tenantId, 
                 configOnly.get("filters") instanceof Map ? ((Map<String, Object>)configOnly.get("filters")).get("enabled") : "not a map");
        
        cacheService.set(cacheKey, configOnly, 10, TimeUnit.MINUTES);
        return ResponseEntity.ok(configOnly);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update configuration", description = "Updates an existing tenant configuration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Configuration updated successfully",
                   content = @Content(schema = @Schema(implementation = TenantConfigurationResponse.class))),
        @ApiResponse(responseCode = "404", description = "Configuration not found")
    })
    public ResponseEntity<TenantConfigurationResponse> updateConfiguration(
            @Parameter(description = "Configuration ID", required = true)
            @PathVariable Long id, 
            
            @Parameter(description = "Updated configuration request", required = true)
            @Valid @RequestBody TenantConfigurationRequest request) {
        String cacheKey = "tenant-config:update:" + id;
        TenantConfigurationResponse cached = cacheService.get(cacheKey, TenantConfigurationResponse.class);
        if (cached != null) {
            return ResponseEntity.ok(cached);
        }
        TenantConfigurationResponse response = tenantConfigurationService.updateConfiguration(id, request);
        cacheService.set(cacheKey, response, 10, TimeUnit.MINUTES);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete configuration", description = "Deletes a tenant configuration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Configuration deleted"),
        @ApiResponse(responseCode = "404", description = "Configuration not found")
    })
    public ResponseEntity<Void> deleteConfiguration(
            @Parameter(description = "Configuration ID", required = true)
            @PathVariable Long id) {
        String cacheKey = "tenant-config:delete:" + id;
        cacheService.delete(cacheKey);
        tenantConfigurationService.deleteConfiguration(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tenant/{tenantId}/checkout")
    @Operation(summary = "Get checkout page configuration", 
              description = "Retrieves checkout page configuration for a tenant")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Configuration retrieved",
                   content = @Content(schema = @Schema(implementation = CheckoutPageConfigResponse.class)))
    })
    public ResponseEntity<CheckoutPageConfigResponse> getCheckoutPageConfiguration(
            @Parameter(description = "Tenant ID", required = true)
            @PathVariable Integer tenantId) {
        String cacheKey = "tenant-config:tenant:" + tenantId + ":checkout";
        CheckoutPageConfigResponse cached = cacheService.get(cacheKey, CheckoutPageConfigResponse.class);
        if (cached != null) {
            return ResponseEntity.ok(cached);
        }
        CheckoutPageConfigResponse config = tenantConfigurationService.getCheckoutPageConfiguration(tenantId);
        cacheService.set(cacheKey, config, 10, TimeUnit.MINUTES);
        return ResponseEntity.ok(config);
    }
} 