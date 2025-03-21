package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.dto.in.TenantConfigurationRequest;
import com.kdu.rizzlers.dto.out.GuestTypeDefinitionResponse;
import com.kdu.rizzlers.dto.out.LandingPageConfigResponse;
import com.kdu.rizzlers.dto.out.PropertyResponse;
import com.kdu.rizzlers.dto.out.TenantConfigurationResponse;
import com.kdu.rizzlers.dto.out.TenantPropertyAssignmentResponse;
import com.kdu.rizzlers.entity.TenantConfiguration;
import com.kdu.rizzlers.entity.TenantPropertyAssignment;
import com.kdu.rizzlers.exception.ResourceNotFoundException;
import com.kdu.rizzlers.repository.TenantConfigurationRepository;
import com.kdu.rizzlers.repository.TenantPropertyAssignmentRepository;
import com.kdu.rizzlers.service.GraphQLPropertyService;
import com.kdu.rizzlers.service.GuestTypeDefinitionService;
import com.kdu.rizzlers.service.TenantConfigurationService;
import com.kdu.rizzlers.util.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantConfigurationServiceImpl implements TenantConfigurationService {

    private final TenantConfigurationRepository tenantConfigurationRepository;
    private final TenantPropertyAssignmentRepository tenantPropertyAssignmentRepository;
    private final GuestTypeDefinitionService guestTypeDefinitionService;
    private final GraphQLPropertyService graphQLPropertyService;

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
    public LandingPageConfigResponse getLandingPageConfiguration(Integer tenantId, boolean fetchPropertyDetails) {
        // Get all landing page configurations for the tenant
        List<TenantConfiguration> configurations = tenantConfigurationRepository.findByTenantIdAndPageAndIsActive(
                tenantId, "landing", true);
        
        // Get all guest type definitions for the tenant
        List<GuestTypeDefinitionResponse> guestTypes = guestTypeDefinitionService.getGuestTypeDefinitionsByTenantIdAndIsActive(
                tenantId, true);
        
        // Create response builder (without property details initially)
        LandingPageConfigResponse.LandingPageConfigResponseBuilder builder = LandingPageConfigResponse.builder()
                .tenantId(tenantId)
                .page("landing")
                .guestTypes(guestTypes);
        
        // If property details are requested, fetch them
        if (fetchPropertyDetails) {
            // Get all property assignments for the tenant
            List<TenantPropertyAssignment> propertyAssignments = tenantPropertyAssignmentRepository.findByTenantIdAndIsAssigned(
                    tenantId, true);
            
            // Convert property assignments to response DTOs with additional property details
            List<TenantPropertyAssignmentResponse> propertyResponses = fetchPropertyDetailsWithGraphQL(propertyAssignments);
            builder.properties(propertyResponses);
        } else {
            // Just get the basic property assignments without enrichment from GraphQL
            List<TenantPropertyAssignment> propertyAssignments = tenantPropertyAssignmentRepository.findByTenantIdAndIsAssigned(
                    tenantId, true);
            
            List<TenantPropertyAssignmentResponse> basicPropertyResponses = propertyAssignments.stream()
                .map(assignment -> TenantPropertyAssignmentResponse.builder()
                    .id(assignment.getId())
                    .tenantId(assignment.getTenantId())
                    .propertyId(assignment.getPropertyId())
                    .isAssigned(assignment.getIsAssigned())
                    .createdAt(assignment.getCreatedAt())
                    .updatedAt(assignment.getUpdatedAt())
                    .build())
                .collect(Collectors.toList());
            
            builder.properties(basicPropertyResponses);
        }
        
        // Map each configuration to the corresponding field in the response
        for (TenantConfiguration config : configurations) {
            String field = config.getField();
            Map<String, Object> valueMap = JsonUtil.jsonToMap(config.getValue());
            
            switch (field) {
                case "header_logo":
                    builder.headerLogo(valueMap);
                    break;
                case "page_title":
                    builder.pageTitle(valueMap);
                    break;
                case "banner_image":
                    builder.bannerImage(valueMap);
                    break;
                case "length_of_stay":
                    builder.lengthOfStay(valueMap);
                    break;
                case "guest_options":
                    builder.guestOptions(valueMap);
                    break;
                case "room_options":
                    builder.roomOptions(valueMap);
                    break;
                case "accessibility_options":
                    builder.accessibilityOptions(valueMap);
                    break;
                case "number_of_rooms":
                    builder.numberOfRooms(valueMap);
                    break;
                default:
                    // Ignore other fields
                    break;
            }
        }
        
        return builder.build();
    }
    
    @Override
    public LandingPageConfigResponse getLandingPageConfiguration(Integer tenantId) {
        return getLandingPageConfiguration(tenantId, true);
    }
    
    private List<TenantPropertyAssignmentResponse> fetchPropertyDetailsWithGraphQL(List<TenantPropertyAssignment> propertyAssignments) {
        if (propertyAssignments.isEmpty()) {
            log.debug("No property assignments to process");
            return new ArrayList<>();
        }
        
        // Extract property IDs to fetch from GraphQL
        List<Integer> propertyIds = propertyAssignments.stream()
                .map(TenantPropertyAssignment::getPropertyId)
                .collect(Collectors.toList());
        
        log.info("Fetching property details for {} properties with IDs: {}", 
                propertyIds.size(), propertyIds);
        
        // Fetch property details from GraphQL service
        List<PropertyResponse> propertyDetails = new ArrayList<>();
        try {
            propertyDetails = graphQLPropertyService.getPropertiesByIds(propertyIds);
            log.info("Retrieved {} properties from GraphQL", propertyDetails.size());
        } catch (Exception e) {
            log.error("Error calling GraphQL service: {}", e.getMessage(), e);
        }
        
        // Create a map for easy property lookup
        Map<Integer, PropertyResponse> propertyMap = propertyDetails.stream()
                .collect(Collectors.toMap(
                    PropertyResponse::getPropertyId, 
                    property -> property, 
                    (existing, replacement) -> existing
                ));
        
        // Map property assignments to response objects
        return propertyAssignments.stream().map(assignment -> {
            Integer propertyId = assignment.getPropertyId();
            
            // Start building the response with assignment data
            TenantPropertyAssignmentResponse.TenantPropertyAssignmentResponseBuilder builder = 
                TenantPropertyAssignmentResponse.builder()
                    .id(assignment.getId())
                    .tenantId(assignment.getTenantId())
                    .propertyId(propertyId)
                    .isAssigned(assignment.getIsAssigned())
                    .createdAt(assignment.getCreatedAt())
                    .updatedAt(assignment.getUpdatedAt());
            
            // Enrich with property details if available
            PropertyResponse property = propertyMap.get(propertyId);
            if (property != null) {
                builder.propertyName(property.getPropertyName())
                       .propertyAddress(property.getPropertyAddress())
                       .contactNumber(property.getContactNumber());
                
                log.debug("Found property details for ID {}: {}", 
                        propertyId, property.getPropertyName());
            } else {
                log.debug("No property details found for ID {}", propertyId);
            }
            
            return builder.build();
        }).collect(Collectors.toList());
    }

    private void appendPropertyDetails(List<TenantPropertyAssignmentResponse> propertyAssignments) {
        if (propertyAssignments == null || propertyAssignments.isEmpty()) {
            log.debug("No property assignments to append details to");
            return;
        }
        
        List<Integer> propertyIds = propertyAssignments.stream()
                .map(TenantPropertyAssignmentResponse::getPropertyId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        if (propertyIds.isEmpty()) {
            log.debug("No valid property IDs found in assignments");
            return;
        }
        
        try {
            log.debug("Fetching property details for property IDs: {}", propertyIds);
            List<PropertyResponse> properties = graphQLPropertyService.getPropertiesByIds(propertyIds);
            
            if (properties == null || properties.isEmpty()) {
                log.warn("No property details returned from GraphQL for property IDs: {}", propertyIds);
                return;
            }
            
            // Create a map for easy lookup
            Map<Integer, PropertyResponse> propertyMap = properties.stream()
                    .collect(Collectors.toMap(
                            PropertyResponse::getPropertyId,
                            Function.identity(),
                            (existing, replacement) -> existing));
            
            // Append property details to each assignment
            for (TenantPropertyAssignmentResponse assignment : propertyAssignments) {
                Integer propertyId = assignment.getPropertyId();
                if (propertyId != null) {
                    PropertyResponse property = propertyMap.get(propertyId);
                    if (property != null) {
                        // Set each property field individually
                        assignment.setPropertyName(property.getPropertyName());
                        assignment.setPropertyAddress(property.getPropertyAddress());
                        assignment.setContactNumber(property.getContactNumber());
                        
                        log.debug("Set property details for property ID: {}", propertyId);
                    } else {
                        // Property not found in GraphQL response
                        log.debug("Property ID {} not found in GraphQL response", propertyId);
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("Error fetching property details: {}", e.getMessage(), e);
        }
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