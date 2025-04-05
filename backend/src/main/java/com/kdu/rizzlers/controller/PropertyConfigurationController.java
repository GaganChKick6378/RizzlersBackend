package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.dto.PropertyConfigurationDTO;
import com.kdu.rizzlers.service.PropertyConfigurationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/property-configurations")
public class PropertyConfigurationController {

    private final PropertyConfigurationService propertyConfigurationService;

    @GetMapping("/{id}")
    public ResponseEntity<PropertyConfigurationDTO.Response> getPropertyConfigurationById(@PathVariable Long id) {
        log.info("REST request to get property configuration with ID: {}", id);
        PropertyConfigurationDTO.Response response = propertyConfigurationService.getPropertyConfigurationById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/property/{propertyId}")
    public ResponseEntity<PropertyConfigurationDTO.Response> getPropertyConfigurationByPropertyId(
            @PathVariable Integer propertyId) {
        log.info("REST request to get property configuration for property ID: {}", propertyId);
        PropertyConfigurationDTO.Response response = propertyConfigurationService.getPropertyConfigurationByPropertyId(propertyId);
        return ResponseEntity.ok(response);
    }
} 