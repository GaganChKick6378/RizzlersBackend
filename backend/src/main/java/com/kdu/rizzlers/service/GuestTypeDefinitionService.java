package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.in.GuestTypeDefinitionRequest;
import com.kdu.rizzlers.dto.out.GuestTypeDefinitionResponse;

import java.util.List;

public interface GuestTypeDefinitionService {
    GuestTypeDefinitionResponse createGuestTypeDefinition(GuestTypeDefinitionRequest request);
    GuestTypeDefinitionResponse getGuestTypeDefinitionById(Long id);
    List<GuestTypeDefinitionResponse> getAllGuestTypeDefinitions();
    List<GuestTypeDefinitionResponse> getGuestTypeDefinitionsByTenantIdAndIsActive(Integer tenantId, Boolean isActive);
    GuestTypeDefinitionResponse getGuestTypeDefinitionByTenantIdAndGuestTypeAndIsActive(
            Integer tenantId, String guestType, Boolean isActive);
    List<GuestTypeDefinitionResponse> getGuestTypeDefinitionsByTenantId(Integer tenantId);
    GuestTypeDefinitionResponse updateGuestTypeDefinition(Long id, GuestTypeDefinitionRequest request);
    void deleteGuestTypeDefinition(Long id);
} 