package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.dto.in.TenantConfigurationRequest;
import com.kdu.rizzlers.dto.out.TenantConfigurationResponse;
import com.kdu.rizzlers.service.TenantConfigurationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tenant-configurations")
public class TenantConfigurationController {

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