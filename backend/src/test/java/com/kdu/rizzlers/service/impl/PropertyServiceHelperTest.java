package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.dto.out.PropertyResponse;
import com.kdu.rizzlers.dto.out.TenantPropertyAssignmentResponse;
import com.kdu.rizzlers.entity.TenantPropertyAssignment;
import com.kdu.rizzlers.repository.TenantPropertyAssignmentRepository;
import com.kdu.rizzlers.service.GraphQLPropertyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PropertyServiceHelperTest {

    @Mock
    private TenantPropertyAssignmentRepository tenantPropertyAssignmentRepository;

    @Mock
    private GraphQLPropertyService graphQLPropertyService;

    @InjectMocks
    private PropertyServiceHelper propertyServiceHelper;

    private TenantPropertyAssignment assignment1;
    private TenantPropertyAssignment assignment2;
    private PropertyResponse property1;
    private PropertyResponse property2;
    private List<TenantPropertyAssignment> assignments;
    private List<PropertyResponse> properties;
    private final Integer TENANT_ID = 1;
    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        // Setup test data
        assignment1 = TenantPropertyAssignment.builder()
                .id(1L)
                .tenantId(TENANT_ID)
                .propertyId(101)
                .isAssigned(true)
                .build();
        
        assignment2 = TenantPropertyAssignment.builder()
                .id(2L)
                .tenantId(TENANT_ID)
                .propertyId(102)
                .isAssigned(false)
                .build();
        
        assignments = Arrays.asList(assignment1, assignment2);
        
        property1 = PropertyResponse.builder()
                .propertyId(101)
                .propertyName("Hotel A")
                .propertyAddress("123 Main St")
                .contactNumber("555-1234")
                .tenantId(TENANT_ID)
                .build();
        
        property2 = PropertyResponse.builder()
                .propertyId(102)
                .propertyName("Hotel B")
                .propertyAddress("456 Second Ave")
                .contactNumber("555-5678")
                .tenantId(TENANT_ID)
                .build();
        
        properties = Arrays.asList(property1, property2);
    }

    @Test
    @DisplayName("Should return basic property assignments when fetchPropertyDetails is false")
    void getPropertyAssignments_withoutPropertyDetails_shouldReturnBasicAssignments() {
        // Arrange
        when(tenantPropertyAssignmentRepository.findByTenantId(TENANT_ID)).thenReturn(assignments);
        
        // Act
        List<TenantPropertyAssignmentResponse> result = propertyServiceHelper.getPropertyAssignments(TENANT_ID, false);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
        assertEquals(101, result.get(0).getPropertyId());
        assertEquals(102, result.get(1).getPropertyId());
        assertTrue(result.get(0).getIsAssigned());
        assertFalse(result.get(1).getIsAssigned());
        assertNull(result.get(0).getPropertyName());
        assertNull(result.get(1).getPropertyName());
        
        // Verify
        verify(tenantPropertyAssignmentRepository).findByTenantId(TENANT_ID);
        verifyNoInteractions(graphQLPropertyService);
    }

    @Test
    @DisplayName("Should return enriched property assignments when fetchPropertyDetails is true")
    void getPropertyAssignments_withPropertyDetails_shouldReturnEnrichedAssignments() {
        // Arrange
        when(tenantPropertyAssignmentRepository.findByTenantId(TENANT_ID)).thenReturn(assignments);
        when(graphQLPropertyService.getPropertiesByIds(anyList())).thenReturn(properties);
        
        // Act
        List<TenantPropertyAssignmentResponse> result = propertyServiceHelper.getPropertyAssignments(TENANT_ID, true);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // First assignment
        assertEquals(1L, result.get(0).getId());
        assertEquals(101, result.get(0).getPropertyId());
        assertEquals("Hotel A", result.get(0).getPropertyName());
        assertEquals("123 Main St", result.get(0).getPropertyAddress());
        assertEquals("555-1234", result.get(0).getContactNumber());
        
        // Second assignment
        assertEquals(2L, result.get(1).getId());
        assertEquals(102, result.get(1).getPropertyId());
        assertEquals("Hotel B", result.get(1).getPropertyName());
        assertEquals("456 Second Ave", result.get(1).getPropertyAddress());
        assertEquals("555-5678", result.get(1).getContactNumber());
        
        // Verify
        verify(tenantPropertyAssignmentRepository).findByTenantId(TENANT_ID);
        verify(graphQLPropertyService).getPropertiesByIds(Arrays.asList(101, 102));
    }

    @Test
    @DisplayName("Should handle empty property assignments")
    void getPropertyAssignments_withEmptyAssignments_shouldReturnEmptyList() {
        // Arrange
        when(tenantPropertyAssignmentRepository.findByTenantId(TENANT_ID)).thenReturn(Collections.emptyList());
        
        // Act
        List<TenantPropertyAssignmentResponse> result = propertyServiceHelper.getPropertyAssignments(TENANT_ID, true);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        // Verify
        verify(tenantPropertyAssignmentRepository).findByTenantId(TENANT_ID);
        verifyNoInteractions(graphQLPropertyService);
    }

    @Test
    @DisplayName("Should handle null property details response")
    void getPropertyAssignments_withNullGraphQLResponse_shouldReturnBasicAssignments() {
        // Arrange
        when(tenantPropertyAssignmentRepository.findByTenantId(TENANT_ID)).thenReturn(assignments);
        when(graphQLPropertyService.getPropertiesByIds(anyList())).thenReturn(null);
        
        // Act
        List<TenantPropertyAssignmentResponse> result = propertyServiceHelper.getPropertyAssignments(TENANT_ID, true);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(101, result.get(0).getPropertyId());
        // Property details should not be set
        assertNull(result.get(0).getPropertyName());
        assertNull(result.get(0).getPropertyAddress());
        
        // Verify
        verify(tenantPropertyAssignmentRepository).findByTenantId(TENANT_ID);
        verify(graphQLPropertyService).getPropertiesByIds(anyList());
    }

    @Test
    @DisplayName("Should handle empty property details response")
    void getPropertyAssignments_withEmptyGraphQLResponse_shouldReturnBasicAssignments() {
        // Arrange
        when(tenantPropertyAssignmentRepository.findByTenantId(TENANT_ID)).thenReturn(assignments);
        when(graphQLPropertyService.getPropertiesByIds(anyList())).thenReturn(Collections.emptyList());
        
        // Act
        List<TenantPropertyAssignmentResponse> result = propertyServiceHelper.getPropertyAssignments(TENANT_ID, true);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(101, result.get(0).getPropertyId());
        // Property details should not be set
        assertNull(result.get(0).getPropertyName());
        assertNull(result.get(0).getPropertyAddress());
        
        // Verify
        verify(tenantPropertyAssignmentRepository).findByTenantId(TENANT_ID);
        verify(graphQLPropertyService).getPropertiesByIds(anyList());
    }

    @Test
    @DisplayName("Should handle exception when fetching property details")
    void getPropertyAssignments_withGraphQLException_shouldReturnBasicAssignments() {
        // Arrange
        when(tenantPropertyAssignmentRepository.findByTenantId(TENANT_ID)).thenReturn(assignments);
        when(graphQLPropertyService.getPropertiesByIds(anyList())).thenThrow(new RuntimeException("GraphQL error"));
        
        // Act
        List<TenantPropertyAssignmentResponse> result = propertyServiceHelper.getPropertyAssignments(TENANT_ID, true);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(101, result.get(0).getPropertyId());
        // Property details should not be set
        assertNull(result.get(0).getPropertyName());
        assertNull(result.get(0).getPropertyAddress());
        
        // Verify
        verify(tenantPropertyAssignmentRepository).findByTenantId(TENANT_ID);
        verify(graphQLPropertyService).getPropertiesByIds(anyList());
    }

    @Test
    @DisplayName("Should handle property ID not found in GraphQL response")
    void getPropertyAssignments_withMissingProperty_shouldHandleGracefully() {
        // Arrange
        when(tenantPropertyAssignmentRepository.findByTenantId(TENANT_ID)).thenReturn(assignments);
        // Only return one property, not both
        when(graphQLPropertyService.getPropertiesByIds(anyList())).thenReturn(Collections.singletonList(property1));
        
        // Act
        List<TenantPropertyAssignmentResponse> result = propertyServiceHelper.getPropertyAssignments(TENANT_ID, true);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // First assignment should have property details
        assertEquals(1L, result.get(0).getId());
        assertEquals(101, result.get(0).getPropertyId());
        assertEquals("Hotel A", result.get(0).getPropertyName());
        assertEquals("123 Main St", result.get(0).getPropertyAddress());
        
        // Second assignment should not have property details
        assertEquals(2L, result.get(1).getId());
        assertEquals(102, result.get(1).getPropertyId());
        assertNull(result.get(1).getPropertyName());
        assertNull(result.get(1).getPropertyAddress());
        
        // Verify
        verify(tenantPropertyAssignmentRepository).findByTenantId(TENANT_ID);
        verify(graphQLPropertyService).getPropertiesByIds(anyList());
    }
} 