package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.dto.out.PropertyResponse;
import com.kdu.rizzlers.dto.out.TenantPropertyAssignmentResponse;
import com.kdu.rizzlers.entity.TenantPropertyAssignment;
import com.kdu.rizzlers.repository.TenantPropertyAssignmentRepository;
import com.kdu.rizzlers.service.GraphQLPropertyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Helper for property-related operations.
 * Separates property fetching and processing logic from the main service.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PropertyServiceHelper {

    private final TenantPropertyAssignmentRepository tenantPropertyAssignmentRepository;
    private final GraphQLPropertyService graphQLPropertyService;

    /**
     * Gets property assignments for a tenant with optional GraphQL property details
     *
     * @param tenantId The ID of the tenant
     * @param fetchPropertyDetails Whether to fetch property details from GraphQL
     * @return List of property assignment responses
     */
    public List<TenantPropertyAssignmentResponse> getPropertyAssignments(Integer tenantId, boolean fetchPropertyDetails) {
        // Get all property assignments for the tenant - including both assigned and unassigned properties
        List<TenantPropertyAssignment> propertyAssignments = tenantPropertyAssignmentRepository.findByTenantId(tenantId);
        
        if (fetchPropertyDetails) {
            // Convert property assignments to response DTOs with additional property details
            return fetchPropertyDetailsWithGraphQL(propertyAssignments);
        } else {
            // Just get the basic property assignments without enrichment from GraphQL
            return propertyAssignments.stream()
                .map(assignment -> TenantPropertyAssignmentResponse.builder()
                    .id(assignment.getId())
                    .tenantId(assignment.getTenantId())
                    .propertyId(assignment.getPropertyId())
                    .isAssigned(assignment.getIsAssigned())
                    .createdAt(assignment.getCreatedAt())
                    .updatedAt(assignment.getUpdatedAt())
                    .build())
                .collect(Collectors.toList());
        }
    }

    /**
     * Fetches property details from GraphQL and enriches the property assignments
     *
     * @param propertyAssignments The property assignments to enrich
     * @return List of enriched property assignment responses
     */
    private List<TenantPropertyAssignmentResponse> fetchPropertyDetailsWithGraphQL(List<TenantPropertyAssignment> propertyAssignments) {
        if (propertyAssignments.isEmpty()) {
            log.debug("No property assignments to process");
            return new ArrayList<>();
        }
        
        // Extract property IDs to fetch from GraphQL
        List<Integer> propertyIds = propertyAssignments.stream()
                .map(TenantPropertyAssignment::getPropertyId)
                .collect(Collectors.toList());
        
        // Create the basic responses first (without property details)
        List<TenantPropertyAssignmentResponse> responses = propertyAssignments.stream()
                .map(assignment -> TenantPropertyAssignmentResponse.builder()
                        .id(assignment.getId())
                        .tenantId(assignment.getTenantId())
                        .propertyId(assignment.getPropertyId())
                        .isAssigned(assignment.getIsAssigned())
                        .createdAt(assignment.getCreatedAt())
                        .updatedAt(assignment.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
        
        try {
            log.debug("Fetching property details for property IDs: {}", propertyIds);
            List<PropertyResponse> properties = graphQLPropertyService.getPropertiesByIds(propertyIds);
            
            if (properties == null || properties.isEmpty()) {
                log.warn("No property details returned from GraphQL for property IDs: {}", propertyIds);
                return responses;
            }
            
            // Create a map for easy lookup
            Map<Integer, PropertyResponse> propertyMap = properties.stream()
                    .collect(Collectors.toMap(
                            PropertyResponse::getPropertyId,
                            Function.identity(),
                            (existing, replacement) -> existing));
            
            // Append property details to each assignment
            for (TenantPropertyAssignmentResponse assignment : responses) {
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
        
        return responses;
    }
} 