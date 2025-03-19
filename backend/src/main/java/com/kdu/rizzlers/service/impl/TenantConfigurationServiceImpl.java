package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.dto.in.TenantConfigurationRequest;
import com.kdu.rizzlers.dto.out.GuestTypeDefinitionResponse;
import com.kdu.rizzlers.dto.out.LandingPageConfigResponse;
import com.kdu.rizzlers.dto.out.TenantConfigurationResponse;
import com.kdu.rizzlers.entity.TenantConfiguration;
import com.kdu.rizzlers.exception.ResourceNotFoundException;
import com.kdu.rizzlers.repository.TenantConfigurationRepository;
import com.kdu.rizzlers.service.GuestTypeDefinitionService;
import com.kdu.rizzlers.service.TenantConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TenantConfigurationServiceImpl implements TenantConfigurationService {

    private final TenantConfigurationRepository tenantConfigurationRepository;
    private final GuestTypeDefinitionService guestTypeDefinitionService;

    @Override
    @Transactional
    public TenantConfigurationResponse createConfiguration(TenantConfigurationRequest request) {
        TenantConfiguration configuration = TenantConfiguration.builder()
                .tenantId(request.getTenantId())
                .page(request.getPage())
                .field(request.getField())
                .value(request.getValue())
                .isActive(request.getIsActive())
                .build();
        
        TenantConfiguration savedConfiguration = tenantConfigurationRepository.save(configuration);
        return mapToResponse(savedConfiguration);
    }

    @Override
    public TenantConfigurationResponse getConfigurationById(Long id) {
        TenantConfiguration configuration = tenantConfigurationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TenantConfiguration", "id", id));
        return mapToResponse(configuration);
    }

    @Override
    public List<TenantConfigurationResponse> getAllConfigurations() {
        return tenantConfigurationRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TenantConfigurationResponse> getConfigurationsByTenantId(Integer tenantId) {
        return tenantConfigurationRepository.findByTenantIdAndIsActive(tenantId, true).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<TenantConfigurationResponse> getConfigurationsByTenantIdAndPage(Integer tenantId, String page) {
        return tenantConfigurationRepository.findByTenantIdAndPageAndIsActive(tenantId, page, true).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TenantConfigurationResponse getConfigurationByTenantIdAndPageAndField(Integer tenantId, String page, String field) {
        TenantConfiguration configuration = tenantConfigurationRepository.findByTenantIdAndPageAndFieldAndIsActive(tenantId, page, field, true)
                .orElseThrow(() -> new ResourceNotFoundException("TenantConfiguration", "tenantId, page, field", tenantId + ", " + page + ", " + field));
        return mapToResponse(configuration);
    }

    @Override
    @Transactional
    public TenantConfigurationResponse updateConfiguration(Long id, TenantConfigurationRequest request) {
        TenantConfiguration configuration = tenantConfigurationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TenantConfiguration", "id", id));
        
        configuration.setTenantId(request.getTenantId());
        configuration.setPage(request.getPage());
        configuration.setField(request.getField());
        configuration.setValue(request.getValue());
        configuration.setIsActive(request.getIsActive());
        
        TenantConfiguration updatedConfiguration = tenantConfigurationRepository.save(configuration);
        return mapToResponse(updatedConfiguration);
    }

    @Override
    @Transactional
    public void deleteConfiguration(Long id) {
        TenantConfiguration configuration = tenantConfigurationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TenantConfiguration", "id", id));
        
        // Soft delete: update isActive to false
        configuration.setIsActive(false);
        tenantConfigurationRepository.save(configuration);
    }
    
    @Override
    public LandingPageConfigResponse getLandingPageConfiguration(Integer tenantId) {
        // Get all landing page configurations for the tenant
        List<TenantConfiguration> configurations = tenantConfigurationRepository.findByTenantIdAndPageAndIsActive(
                tenantId, "landing", true);
        
        // Get all guest type definitions for the tenant
        List<GuestTypeDefinitionResponse> guestTypes = guestTypeDefinitionService.getGuestTypeDefinitionsByTenantIdAndIsActive(
                tenantId, true);
        
        // Create response builder
        LandingPageConfigResponse.LandingPageConfigResponseBuilder builder = LandingPageConfigResponse.builder()
                .tenantId(tenantId)
                .page("landing")
                .guestTypes(guestTypes);
        
        // Map each configuration to the corresponding field in the response
        for (TenantConfiguration config : configurations) {
            String field = config.getField();
            Map<String, Object> value = config.getValue();
            
            switch (field) {
                case "header_logo":
                    builder.headerLogo(value);
                    break;
                case "page_title":
                    builder.pageTitle(value);
                    break;
                case "banner_image":
                    builder.bannerImage(value);
                    break;
                case "length_of_stay":
                    builder.lengthOfStay(value);
                    break;
                case "guest_options":
                    builder.guestOptions(value);
                    break;
                case "room_options":
                    builder.roomOptions(value);
                    break;
                case "accessibility_options":
                    builder.accessibilityOptions(value);
                    break;
                case "number_of_rooms":
                    builder.numberOfRooms(value);
                    break;
                default:
                    // Ignore other fields
                    break;
            }
        }
        
        return builder.build();
    }

    private TenantConfigurationResponse mapToResponse(TenantConfiguration configuration) {
        return TenantConfigurationResponse.builder()
                .id(configuration.getId())
                .tenantId(configuration.getTenantId())
                .page(configuration.getPage())
                .field(configuration.getField())
                .value(configuration.getValue())
                .isActive(configuration.getIsActive())
                .createdAt(configuration.getCreatedAt())
                .updatedAt(configuration.getUpdatedAt())
                .build();
    }
} 