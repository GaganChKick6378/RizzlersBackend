package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.in.TenantPropertyAssignmentRequest;
import com.kdu.rizzlers.dto.out.TenantPropertyAssignmentResponse;

import java.util.List;

public interface TenantPropertyAssignmentService {
    TenantPropertyAssignmentResponse createAssignment(TenantPropertyAssignmentRequest request);
    TenantPropertyAssignmentResponse getAssignmentById(Long id);
    List<TenantPropertyAssignmentResponse> getAllAssignments();
    List<TenantPropertyAssignmentResponse> getAssignmentsByTenantId(Integer tenantId);
    List<TenantPropertyAssignmentResponse> getAssignedPropertiesByTenantId(Integer tenantId);
    List<TenantPropertyAssignmentResponse> getAssignmentsByPropertyId(Integer propertyId);
    TenantPropertyAssignmentResponse getAssignmentByTenantIdAndPropertyId(Integer tenantId, Integer propertyId);
    TenantPropertyAssignmentResponse updateAssignment(Long id, TenantPropertyAssignmentRequest request);
    void deleteAssignment(Long id);
} 