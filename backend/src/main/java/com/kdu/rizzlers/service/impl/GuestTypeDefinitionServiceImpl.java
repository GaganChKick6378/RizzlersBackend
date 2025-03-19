package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.dto.in.GuestTypeDefinitionRequest;
import com.kdu.rizzlers.dto.out.GuestTypeDefinitionResponse;
import com.kdu.rizzlers.entity.GuestTypeDefinition;
import com.kdu.rizzlers.exception.ResourceNotFoundException;
import com.kdu.rizzlers.repository.GuestTypeDefinitionRepository;
import com.kdu.rizzlers.service.GuestTypeDefinitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GuestTypeDefinitionServiceImpl implements GuestTypeDefinitionService {

    private final GuestTypeDefinitionRepository guestTypeDefinitionRepository;

    @Override
    @Transactional
    public GuestTypeDefinitionResponse createGuestTypeDefinition(GuestTypeDefinitionRequest request) {
        GuestTypeDefinition guestTypeDefinition = GuestTypeDefinition.builder()
                .tenantId(request.getTenantId())
                .guestType(request.getGuestType())
                .minAge(request.getMinAge())
                .maxAge(request.getMaxAge())
                .description(request.getDescription())
                .isActive(request.getIsActive())
                .maxCount(request.getMaxCount())
                .build();
        
        GuestTypeDefinition savedGuestTypeDefinition = guestTypeDefinitionRepository.save(guestTypeDefinition);
        return mapToResponse(savedGuestTypeDefinition);
    }

    @Override
    public GuestTypeDefinitionResponse getGuestTypeDefinitionById(Long id) {
        GuestTypeDefinition guestTypeDefinition = guestTypeDefinitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GuestTypeDefinition", "id", id));
        return mapToResponse(guestTypeDefinition);
    }

    @Override
    public List<GuestTypeDefinitionResponse> getAllGuestTypeDefinitions() {
        return guestTypeDefinitionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<GuestTypeDefinitionResponse> getGuestTypeDefinitionsByTenantIdAndIsActive(Integer tenantId, Boolean isActive) {
        return guestTypeDefinitionRepository.findByTenantIdAndIsActive(tenantId, isActive).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public GuestTypeDefinitionResponse getGuestTypeDefinitionByTenantIdAndGuestTypeAndIsActive(
            Integer tenantId, String guestType, Boolean isActive) {
        GuestTypeDefinition guestTypeDefinition = guestTypeDefinitionRepository
                .findByTenantIdAndGuestTypeAndIsActive(tenantId, guestType, isActive)
                .orElseThrow(() -> new ResourceNotFoundException("GuestTypeDefinition", 
                        "tenantId, guestType, isActive", tenantId + ", " + guestType + ", " + isActive));
        return mapToResponse(guestTypeDefinition);
    }

    @Override
    public List<GuestTypeDefinitionResponse> getGuestTypeDefinitionsByTenantId(Integer tenantId) {
        return guestTypeDefinitionRepository.findByTenantId(tenantId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public GuestTypeDefinitionResponse updateGuestTypeDefinition(Long id, GuestTypeDefinitionRequest request) {
        GuestTypeDefinition guestTypeDefinition = guestTypeDefinitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GuestTypeDefinition", "id", id));
        
        guestTypeDefinition.setTenantId(request.getTenantId());
        guestTypeDefinition.setGuestType(request.getGuestType());
        guestTypeDefinition.setMinAge(request.getMinAge());
        guestTypeDefinition.setMaxAge(request.getMaxAge());
        guestTypeDefinition.setDescription(request.getDescription());
        guestTypeDefinition.setIsActive(request.getIsActive());
        guestTypeDefinition.setMaxCount(request.getMaxCount());
        
        GuestTypeDefinition updatedGuestTypeDefinition = guestTypeDefinitionRepository.save(guestTypeDefinition);
        return mapToResponse(updatedGuestTypeDefinition);
    }

    @Override
    @Transactional
    public void deleteGuestTypeDefinition(Long id) {
        GuestTypeDefinition guestTypeDefinition = guestTypeDefinitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GuestTypeDefinition", "id", id));
        
        // Soft delete: update isActive to false
        guestTypeDefinition.setIsActive(false);
        guestTypeDefinitionRepository.save(guestTypeDefinition);
    }

    private GuestTypeDefinitionResponse mapToResponse(GuestTypeDefinition guestTypeDefinition) {
        return GuestTypeDefinitionResponse.builder()
                .id(guestTypeDefinition.getId())
                .tenantId(guestTypeDefinition.getTenantId())
                .guestType(guestTypeDefinition.getGuestType())
                .minAge(guestTypeDefinition.getMinAge())
                .maxAge(guestTypeDefinition.getMaxAge())
                .description(guestTypeDefinition.getDescription())
                .isActive(guestTypeDefinition.getIsActive())
                .maxCount(guestTypeDefinition.getMaxCount())
                .createdAt(guestTypeDefinition.getCreatedAt())
                .updatedAt(guestTypeDefinition.getUpdatedAt())
                .build();
    }
} 