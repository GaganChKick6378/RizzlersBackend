package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.cache.CacheService;
import com.kdu.rizzlers.dto.PropertyConfigurationDTO;
import com.kdu.rizzlers.service.PropertyConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/property-configurations")
@Tag(name = "Property Configuration", description = "APIs for property configuration management")
public class PropertyConfigurationController {

    private final PropertyConfigurationService propertyConfigurationService;
    @Autowired
    private CacheService cacheService;

    @GetMapping("/{id}")
    @Operation(summary = "Get property configuration by ID", 
              description = "Retrieves property configuration by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Configuration found",
                   content = @Content(schema = @Schema(implementation = PropertyConfigurationDTO.Response.class))),
        @ApiResponse(responseCode = "404", description = "Configuration not found")
    })
    public ResponseEntity<PropertyConfigurationDTO.Response> getPropertyConfigurationById(
            @Parameter(description = "Configuration ID", required = true) 
            @PathVariable Long id) {
        log.info("REST request to get property configuration with ID: {}", id);
        String cacheKey = "property-config:id:" + id;
        PropertyConfigurationDTO.Response cached = cacheService.get(cacheKey, PropertyConfigurationDTO.Response.class);
        if (cached != null) {
            return ResponseEntity.ok(cached);
        }
        PropertyConfigurationDTO.Response response = propertyConfigurationService.getPropertyConfigurationById(id);
        cacheService.set(cacheKey, response, 10, TimeUnit.MINUTES);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/property/{propertyId}")
    @Operation(summary = "Get property configuration by property ID", 
              description = "Retrieves property configuration by property ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Configuration found",
                   content = @Content(schema = @Schema(implementation = PropertyConfigurationDTO.Response.class))),
        @ApiResponse(responseCode = "404", description = "Configuration not found")
    })
    public ResponseEntity<PropertyConfigurationDTO.Response> getPropertyConfigurationByPropertyId(
            @Parameter(description = "Property ID", required = true)
            @PathVariable Integer propertyId) {
        log.info("REST request to get property configuration for property ID: {}", propertyId);
        String cacheKey = "property-config:propertyId:" + propertyId;
        PropertyConfigurationDTO.Response cached = cacheService.get(cacheKey, PropertyConfigurationDTO.Response.class);
        if (cached != null) {
            return ResponseEntity.ok(cached);
        }
        PropertyConfigurationDTO.Response response = propertyConfigurationService.getPropertyConfigurationByPropertyId(propertyId);
        cacheService.set(cacheKey, response, 10, TimeUnit.MINUTES);
        return ResponseEntity.ok(response);
    }
} 