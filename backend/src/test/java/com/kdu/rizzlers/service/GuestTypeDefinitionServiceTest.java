package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.in.GuestTypeDefinitionRequest;
import com.kdu.rizzlers.dto.out.GuestTypeDefinitionResponse;
import com.kdu.rizzlers.entity.GuestTypeDefinition;
import com.kdu.rizzlers.exception.ResourceNotFoundException;
import com.kdu.rizzlers.repository.GuestTypeDefinitionRepository;
import com.kdu.rizzlers.service.impl.GuestTypeDefinitionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GuestTypeDefinitionServiceTest {

    @Mock
    private GuestTypeDefinitionRepository guestTypeDefinitionRepository;

    @InjectMocks
    private GuestTypeDefinitionServiceImpl guestTypeDefinitionService;

    private GuestTypeDefinition adultGuestType;
    private GuestTypeDefinition childGuestType;
    private GuestTypeDefinitionRequest adultGuestTypeRequest;

    @BeforeEach
    void setUp() {
        // Setup test guest type definitions
        LocalDateTime now = LocalDateTime.now();
        
        adultGuestType = GuestTypeDefinition.builder()
                .id(1L)
                .tenantId(100)
                .guestType("ADULT")
                .minAge(18)
                .maxAge(120)
                .description("Adult guest (18+ years)")
                .isActive(true)
                .maxCount(4)
                .build();
        adultGuestType.setCreatedAt(now);
        adultGuestType.setUpdatedAt(now);
        
        childGuestType = GuestTypeDefinition.builder()
                .id(2L)
                .tenantId(100)
                .guestType("CHILD")
                .minAge(2)
                .maxAge(17)
                .description("Child guest (2-17 years)")
                .isActive(true)
                .maxCount(4)
                .build();
        childGuestType.setCreatedAt(now);
        childGuestType.setUpdatedAt(now);
        
        // Setup test request
        adultGuestTypeRequest = GuestTypeDefinitionRequest.builder()
                .tenantId(100)
                .guestType("ADULT")
                .minAge(18)
                .maxAge(120)
                .description("Adult guest (18+ years)")
                .isActive(true)
                .maxCount(4)
                .build();
    }

    @Test
    void createGuestTypeDefinition_ShouldSaveAndReturnGuestTypeDefinition() {
        // Given
        when(guestTypeDefinitionRepository.save(any(GuestTypeDefinition.class))).thenReturn(adultGuestType);

        // When
        GuestTypeDefinitionResponse response = guestTypeDefinitionService.createGuestTypeDefinition(adultGuestTypeRequest);

        // Then
        assertNotNull(response);
        assertEquals(adultGuestType.getId(), response.getId());
        assertEquals(adultGuestType.getTenantId(), response.getTenantId());
        assertEquals(adultGuestType.getGuestType(), response.getGuestType());
        assertEquals(adultGuestType.getMinAge(), response.getMinAge());
        assertEquals(adultGuestType.getMaxAge(), response.getMaxAge());
        assertEquals(adultGuestType.getDescription(), response.getDescription());
        assertEquals(adultGuestType.getIsActive(), response.getIsActive());
        assertEquals(adultGuestType.getMaxCount(), response.getMaxCount());
        verify(guestTypeDefinitionRepository).save(any(GuestTypeDefinition.class));
    }

    @Test
    void getGuestTypeDefinitionById_ShouldReturnGuestTypeDefinitionWhenExists() {
        // Given
        Long id = 1L;
        when(guestTypeDefinitionRepository.findById(id)).thenReturn(Optional.of(adultGuestType));

        // When
        GuestTypeDefinitionResponse response = guestTypeDefinitionService.getGuestTypeDefinitionById(id);

        // Then
        assertNotNull(response);
        assertEquals(id, response.getId());
        assertEquals(adultGuestType.getGuestType(), response.getGuestType());
        verify(guestTypeDefinitionRepository).findById(id);
    }

    @Test
    void getGuestTypeDefinitionById_ShouldThrowExceptionWhenNotFound() {
        // Given
        Long id = 999L;
        when(guestTypeDefinitionRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> guestTypeDefinitionService.getGuestTypeDefinitionById(id));
        verify(guestTypeDefinitionRepository).findById(id);
    }

    @Test
    void getAllGuestTypeDefinitions_ShouldReturnAllGuestTypeDefinitions() {
        // Given
        List<GuestTypeDefinition> guestTypes = Arrays.asList(adultGuestType, childGuestType);
        when(guestTypeDefinitionRepository.findAll()).thenReturn(guestTypes);

        // When
        List<GuestTypeDefinitionResponse> responses = guestTypeDefinitionService.getAllGuestTypeDefinitions();

        // Then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(adultGuestType.getId(), responses.get(0).getId());
        assertEquals(childGuestType.getId(), responses.get(1).getId());
        verify(guestTypeDefinitionRepository).findAll();
    }

    @Test
    void getGuestTypeDefinitionsByTenantIdAndIsActive_ShouldReturnFilteredGuestTypeDefinitions() {
        // Given
        Integer tenantId = 100;
        Boolean isActive = true;
        List<GuestTypeDefinition> guestTypes = Arrays.asList(adultGuestType, childGuestType);
        when(guestTypeDefinitionRepository.findByTenantIdAndIsActive(tenantId, isActive)).thenReturn(guestTypes);

        // When
        List<GuestTypeDefinitionResponse> responses = guestTypeDefinitionService.getGuestTypeDefinitionsByTenantIdAndIsActive(tenantId, isActive);

        // Then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(tenantId, responses.get(0).getTenantId());
        assertEquals(isActive, responses.get(0).getIsActive());
        verify(guestTypeDefinitionRepository).findByTenantIdAndIsActive(tenantId, isActive);
    }

    @Test
    void getGuestTypeDefinitionByTenantIdAndGuestTypeAndIsActive_ShouldReturnMatchingGuestTypeDefinition() {
        // Given
        Integer tenantId = 100;
        String guestType = "ADULT";
        Boolean isActive = true;
        when(guestTypeDefinitionRepository.findByTenantIdAndGuestTypeAndIsActive(tenantId, guestType, isActive))
                .thenReturn(Optional.of(adultGuestType));

        // When
        GuestTypeDefinitionResponse response = guestTypeDefinitionService.getGuestTypeDefinitionByTenantIdAndGuestTypeAndIsActive(tenantId, guestType, isActive);

        // Then
        assertNotNull(response);
        assertEquals(tenantId, response.getTenantId());
        assertEquals(guestType, response.getGuestType());
        assertEquals(isActive, response.getIsActive());
        verify(guestTypeDefinitionRepository).findByTenantIdAndGuestTypeAndIsActive(tenantId, guestType, isActive);
    }

    @Test
    void getGuestTypeDefinitionByTenantIdAndGuestTypeAndIsActive_ShouldThrowExceptionWhenNotFound() {
        // Given
        Integer tenantId = 100;
        String guestType = "INVALID";
        Boolean isActive = true;
        when(guestTypeDefinitionRepository.findByTenantIdAndGuestTypeAndIsActive(tenantId, guestType, isActive))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> 
                guestTypeDefinitionService.getGuestTypeDefinitionByTenantIdAndGuestTypeAndIsActive(tenantId, guestType, isActive));
        verify(guestTypeDefinitionRepository).findByTenantIdAndGuestTypeAndIsActive(tenantId, guestType, isActive);
    }

    @Test
    void getGuestTypeDefinitionsByTenantId_ShouldReturnAllGuestTypeDefinitionsForTenant() {
        // Given
        Integer tenantId = 100;
        List<GuestTypeDefinition> guestTypes = Arrays.asList(adultGuestType, childGuestType);
        when(guestTypeDefinitionRepository.findByTenantId(tenantId)).thenReturn(guestTypes);

        // When
        List<GuestTypeDefinitionResponse> responses = guestTypeDefinitionService.getGuestTypeDefinitionsByTenantId(tenantId);

        // Then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals(tenantId, responses.get(0).getTenantId());
        verify(guestTypeDefinitionRepository).findByTenantId(tenantId);
    }

    @Test
    void updateGuestTypeDefinition_ShouldUpdateAndReturnGuestTypeDefinition() {
        // Given
        Long id = 1L;
        GuestTypeDefinitionRequest updatedRequest = GuestTypeDefinitionRequest.builder()
                .tenantId(100)
                .guestType("ADULT")
                .minAge(21) // Changed min age
                .maxAge(120)
                .description("Updated description") // Changed description
                .isActive(true)
                .maxCount(6) // Changed max count
                .build();
        
        GuestTypeDefinition existingGuestType = adultGuestType; // Clone to avoid modifying the original
        GuestTypeDefinition updatedGuestType = GuestTypeDefinition.builder()
                .id(existingGuestType.getId())
                .tenantId(updatedRequest.getTenantId())
                .guestType(updatedRequest.getGuestType())
                .minAge(updatedRequest.getMinAge())
                .maxAge(updatedRequest.getMaxAge())
                .description(updatedRequest.getDescription())
                .isActive(updatedRequest.getIsActive())
                .maxCount(updatedRequest.getMaxCount())
                .build();
        updatedGuestType.setCreatedAt(existingGuestType.getCreatedAt());
        updatedGuestType.setUpdatedAt(LocalDateTime.now());
        
        when(guestTypeDefinitionRepository.findById(id)).thenReturn(Optional.of(existingGuestType));
        when(guestTypeDefinitionRepository.save(any(GuestTypeDefinition.class))).thenReturn(updatedGuestType);

        // When
        GuestTypeDefinitionResponse response = guestTypeDefinitionService.updateGuestTypeDefinition(id, updatedRequest);

        // Then
        assertNotNull(response);
        assertEquals(id, response.getId());
        assertEquals(updatedRequest.getMinAge(), response.getMinAge());
        assertEquals(updatedRequest.getDescription(), response.getDescription());
        assertEquals(updatedRequest.getMaxCount(), response.getMaxCount());
        verify(guestTypeDefinitionRepository).findById(id);
        verify(guestTypeDefinitionRepository).save(any(GuestTypeDefinition.class));
    }

    @Test
    void deleteGuestTypeDefinition_ShouldSoftDeleteGuestTypeDefinition() {
        // Given
        Long id = 1L;
        GuestTypeDefinition guestTypeToDelete = adultGuestType; // Use existing object to track changes
        
        when(guestTypeDefinitionRepository.findById(id)).thenReturn(Optional.of(guestTypeToDelete));

        // When
        guestTypeDefinitionService.deleteGuestTypeDefinition(id);

        // Then
        assertFalse(guestTypeToDelete.getIsActive()); // Verify it was soft deleted
        verify(guestTypeDefinitionRepository).findById(id);
        verify(guestTypeDefinitionRepository).save(guestTypeToDelete);
    }
} 