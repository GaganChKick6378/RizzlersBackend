package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.dto.in.TenantConfigurationRequest;
import com.kdu.rizzlers.dto.out.LandingPageConfigResponse;
import com.kdu.rizzlers.dto.out.ResultsPageConfigResponse;
import com.kdu.rizzlers.dto.out.TenantConfigurationResponse;
import com.kdu.rizzlers.service.TenantConfigurationService;
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

@RestController
@RequestMapping("/tenant-configurations")
public class TenantConfigurationController {

    private static final Logger log = LoggerFactory.getLogger(TenantConfigurationController.class);

    @Autowired
    private TenantConfigurationService tenantConfigurationService;

    @PostMapping
    public ResponseEntity<TenantConfigurationResponse> createConfiguration(@Valid @RequestBody TenantConfigurationRequest request) {
        return new ResponseEntity<>(tenantConfigurationService.createConfiguration(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TenantConfigurationResponse> getConfigurationById(@PathVariable Long id) {
        return ResponseEntity.ok(tenantConfigurationService.getConfigurationById(id));
    }

    @GetMapping
    public ResponseEntity<List<TenantConfigurationResponse>> getAllConfigurations() {
        return ResponseEntity.ok(tenantConfigurationService.getAllConfigurations());
    }

    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<List<TenantConfigurationResponse>> getConfigurationsByTenantId(@PathVariable Integer tenantId) {
        return ResponseEntity.ok(tenantConfigurationService.getConfigurationsByTenantId(tenantId));
    }

    @GetMapping("/tenant/{tenantId}/page/{page}")
    public ResponseEntity<List<TenantConfigurationResponse>> getConfigurationsByTenantIdAndPage(
            @PathVariable Integer tenantId, @PathVariable String page) {
        return ResponseEntity.ok(tenantConfigurationService.getConfigurationsByTenantIdAndPage(tenantId, page));
    }

    @GetMapping("/tenant/{tenantId}/page/{page}/field/{field}")
    public ResponseEntity<TenantConfigurationResponse> getConfigurationByTenantIdAndPageAndField(
            @PathVariable Integer tenantId, @PathVariable String page, @PathVariable String field) {
        return ResponseEntity.ok(tenantConfigurationService.getConfigurationByTenantIdAndPageAndField(tenantId, page, field));
    }
    
    @GetMapping("/tenant/{tenantId}/landing")
    public ResponseEntity<LandingPageConfigResponse> getLandingPageConfiguration(@PathVariable Integer tenantId) {
        return ResponseEntity.ok(tenantConfigurationService.getLandingPageConfiguration(tenantId));
    }

    @GetMapping("/tenant/{tenantId}/landing/basic")
    public ResponseEntity<LandingPageConfigResponse> getBasicLandingPageConfiguration(
            @PathVariable Integer tenantId,
            @RequestParam(value = "fetch_property_details", defaultValue = "false") boolean fetchPropertyDetails) {
        
        LandingPageConfigResponse config = tenantConfigurationService.getLandingPageConfiguration(tenantId, fetchPropertyDetails);
        return ResponseEntity.ok(config);
    }

    @GetMapping("/tenant/{tenantId}/results")
    public ResponseEntity<Map<String, Object>> getResultsPageConfiguration(@PathVariable Integer tenantId) {
        ResultsPageConfigResponse fullConfig = tenantConfigurationService.getResultsPageConfiguration(tenantId);
        
        // Add logging to see what's coming from the service
        log.info("Tenant {} results page config - filters map keys: {}", tenantId, 
                 fullConfig.getFilters().keySet());
        
        log.info("Tenant {} results page config - filters enabled: {}", tenantId, 
                 fullConfig.getFilters().containsKey("enabled") ? fullConfig.getFilters().get("enabled") : "not found");
        
        if (fullConfig.getFilters().containsKey("show")) {
            log.info("Tenant {} results page config - filters 'show' value: {}", tenantId, fullConfig.getFilters().get("show"));
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
        
        // Log the final response
        log.info("Tenant {} results page final response - filters enabled: {}", tenantId, 
                 configOnly.get("filters") instanceof Map ? ((Map<String, Object>)configOnly.get("filters")).get("enabled") : "not a map");
        
        return ResponseEntity.ok(configOnly);
    }

    @GetMapping("/tenant/{tenantId}/results/basic")
    public ResponseEntity<Map<String, Object>> getBasicResultsPageConfiguration(
            @PathVariable Integer tenantId,
            @RequestParam(value = "fetch_property_details", defaultValue = "false") boolean fetchPropertyDetails) {
        
        ResultsPageConfigResponse fullConfig = tenantConfigurationService.getResultsPageConfiguration(tenantId, fetchPropertyDetails);
        
        // Add logging
        log.info("Basic endpoint: Tenant {} results page config - filters map keys: {}", tenantId, 
                 fullConfig.getFilters().keySet());
                 
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
        
        // Log the final response
        log.info("Basic endpoint: Tenant {} results page final response - filters enabled: {}", tenantId, 
                 configOnly.get("filters") instanceof Map ? ((Map<String, Object>)configOnly.get("filters")).get("enabled") : "not a map");
        
        return ResponseEntity.ok(configOnly);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TenantConfigurationResponse> updateConfiguration(
            @PathVariable Long id, @Valid @RequestBody TenantConfigurationRequest request) {
        return ResponseEntity.ok(tenantConfigurationService.updateConfiguration(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConfiguration(@PathVariable Long id) {
        tenantConfigurationService.deleteConfiguration(id);
        return ResponseEntity.noContent().build();
    }
} 