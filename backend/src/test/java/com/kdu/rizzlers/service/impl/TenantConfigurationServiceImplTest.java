package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.dto.in.TenantConfigurationRequest;
import com.kdu.rizzlers.dto.out.GuestTypeDefinitionResponse;
import com.kdu.rizzlers.dto.out.LandingPageConfigResponse;
import com.kdu.rizzlers.dto.out.ResultsPageConfigResponse;
import com.kdu.rizzlers.dto.out.TenantConfigurationResponse;
import com.kdu.rizzlers.dto.out.TenantPropertyAssignmentResponse;
import com.kdu.rizzlers.entity.TenantConfiguration;
import com.kdu.rizzlers.exception.ResourceNotFoundException;
import com.kdu.rizzlers.repository.TenantConfigurationRepository;
import com.kdu.rizzlers.service.FilterOptionsService;
import com.kdu.rizzlers.service.GuestTypeDefinitionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantConfigurationServiceImplTest {

    @Mock
    private TenantConfigurationRepository tenantConfigurationRepository;

    @Mock
    private GuestTypeDefinitionService guestTypeDefinitionService;

    @Mock
    private PropertyServiceHelper propertyServiceHelper;

    @Mock
    private ConfigurationValidator configValidator;

    @Mock
    private ConfigurationDefaultProvider defaultProvider;

    @Mock
    private FilterOptionsService filterOptionsService;

    @InjectMocks
    private TenantConfigurationServiceImpl tenantConfigurationService;

    private TenantConfiguration mockConfig;
    private TenantConfigurationRequest mockRequest;

    @BeforeEach
    void setUp() {
        // Setup mock config
        mockConfig = TenantConfiguration.builder()
                .id(1L)
                .tenantId(100)
                .page("landing")
                .field("logo")
                .value("{\"url\":\"https://example.com/logo.png\",\"alt\":\"Logo\"}")
                .isActive(true)
                .build();

        // Setup mock request
        mockRequest = new TenantConfigurationRequest();
        mockRequest.setTenantId(100);
        mockRequest.setPage("landing");
        mockRequest.setField("logo");
        mockRequest.setValue("{\"url\":\"https://example.com/logo.png\",\"alt\":\"Logo\"}");
        mockRequest.setIsActive(true);
    }

    @Test
    void createConfiguration_shouldReturnSavedConfiguration() {
        // Arrange
        when(tenantConfigurationRepository.save(any(TenantConfiguration.class))).thenReturn(mockConfig);

        // Act
        TenantConfigurationResponse response = tenantConfigurationService.createConfiguration(mockRequest);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(100, response.getTenantId());
        assertEquals("landing", response.getPage());
        assertEquals("logo", response.getField());
        verify(tenantConfigurationRepository).save(any(TenantConfiguration.class));
    }

    @Test
    void getConfigurationById_whenExists_shouldReturnConfiguration() {
        // Arrange
        when(tenantConfigurationRepository.findById(1L)).thenReturn(Optional.of(mockConfig));

        // Act
        TenantConfigurationResponse response = tenantConfigurationService.getConfigurationById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(100, response.getTenantId());
        assertEquals("landing", response.getPage());
        verify(tenantConfigurationRepository).findById(1L);
    }

    @Test
    void getConfigurationById_whenNotExists_shouldThrowResourceNotFoundException() {
        // Arrange
        when(tenantConfigurationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            tenantConfigurationService.getConfigurationById(999L);
        });
        verify(tenantConfigurationRepository).findById(999L);
    }

    @Test
    void getAllConfigurations_shouldReturnAllConfigurations() {
        // Arrange
        TenantConfiguration mockConfig2 = TenantConfiguration.builder()
                .id(2L)
                .tenantId(101)
                .page("results")
                .field("header")
                .value("{\"title\":\"Search Results\"}")
                .isActive(true)
                .build();
        
        when(tenantConfigurationRepository.findAll()).thenReturn(Arrays.asList(mockConfig, mockConfig2));

        // Act
        List<TenantConfigurationResponse> responses = tenantConfigurationService.getAllConfigurations();

        // Assert
        assertEquals(2, responses.size());
        assertEquals(1L, responses.get(0).getId());
        assertEquals(2L, responses.get(1).getId());
        verify(tenantConfigurationRepository).findAll();
    }

    @Test
    void getConfigurationsByTenantId_shouldReturnConfigurationsForTenant() {
        // Arrange
        when(tenantConfigurationRepository.findByTenantIdAndIsActive(100, true))
                .thenReturn(Collections.singletonList(mockConfig));

        // Act
        List<TenantConfigurationResponse> responses = tenantConfigurationService.getConfigurationsByTenantId(100);

        // Assert
        assertEquals(1, responses.size());
        assertEquals(100, responses.get(0).getTenantId());
        verify(tenantConfigurationRepository).findByTenantIdAndIsActive(100, true);
    }

    @Test
    void getConfigurationsByTenantIdAndPage_shouldReturnConfigurationsForTenantAndPage() {
        // Arrange
        when(tenantConfigurationRepository.findByTenantIdAndPageAndIsActive(100, "landing", true))
                .thenReturn(Collections.singletonList(mockConfig));

        // Act
        List<TenantConfigurationResponse> responses = tenantConfigurationService.getConfigurationsByTenantIdAndPage(100, "landing");

        // Assert
        assertEquals(1, responses.size());
        assertEquals("landing", responses.get(0).getPage());
        verify(tenantConfigurationRepository).findByTenantIdAndPageAndIsActive(100, "landing", true);
    }

    @Test
    void getConfigurationByTenantIdAndPageAndField_whenExists_shouldReturnConfiguration() {
        // Arrange
        when(tenantConfigurationRepository.findByTenantIdAndPageAndFieldAndIsActive(100, "landing", "logo", true))
                .thenReturn(Optional.of(mockConfig));

        // Act
        TenantConfigurationResponse response = tenantConfigurationService.getConfigurationByTenantIdAndPageAndField(
                100, "landing", "logo");

        // Assert
        assertNotNull(response);
        assertEquals("logo", response.getField());
        verify(tenantConfigurationRepository).findByTenantIdAndPageAndFieldAndIsActive(100, "landing", "logo", true);
    }

    @Test
    void getConfigurationByTenantIdAndPageAndField_whenNotExists_shouldThrowResourceNotFoundException() {
        // Arrange
        when(tenantConfigurationRepository.findByTenantIdAndPageAndFieldAndIsActive(100, "landing", "header", true))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            tenantConfigurationService.getConfigurationByTenantIdAndPageAndField(100, "landing", "header");
        });
        verify(tenantConfigurationRepository).findByTenantIdAndPageAndFieldAndIsActive(100, "landing", "header", true);
    }

    @Test
    void updateConfiguration_whenExists_shouldReturnUpdatedConfiguration() {
        // Arrange
        mockRequest.setValue("{\"url\":\"https://example.com/updated-logo.png\",\"alt\":\"Updated Logo\"}");
        
        when(tenantConfigurationRepository.findById(1L)).thenReturn(Optional.of(mockConfig));
        when(tenantConfigurationRepository.save(any(TenantConfiguration.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        TenantConfigurationResponse response = tenantConfigurationService.updateConfiguration(1L, mockRequest);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(mockRequest.getValue(), response.getValue());
        verify(tenantConfigurationRepository).findById(1L);
        verify(tenantConfigurationRepository).save(any(TenantConfiguration.class));
    }

    @Test
    void updateConfiguration_whenNotExists_shouldThrowResourceNotFoundException() {
        // Arrange
        when(tenantConfigurationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            tenantConfigurationService.updateConfiguration(999L, mockRequest);
        });
        verify(tenantConfigurationRepository).findById(999L);
        verify(tenantConfigurationRepository, never()).save(any(TenantConfiguration.class));
    }

    @Test
    void deleteConfiguration_whenExists_shouldSoftDeleteConfiguration() {
        // Arrange
        when(tenantConfigurationRepository.findById(1L)).thenReturn(Optional.of(mockConfig));
        when(tenantConfigurationRepository.save(any(TenantConfiguration.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        tenantConfigurationService.deleteConfiguration(1L);

        // Assert
        verify(tenantConfigurationRepository).findById(1L);
        verify(tenantConfigurationRepository).save(mockConfig);
        assertFalse(mockConfig.getIsActive());
    }

    @Test
    void deleteConfiguration_whenNotExists_shouldThrowResourceNotFoundException() {
        // Arrange
        when(tenantConfigurationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            tenantConfigurationService.deleteConfiguration(999L);
        });
        verify(tenantConfigurationRepository).findById(999L);
        verify(tenantConfigurationRepository, never()).save(any(TenantConfiguration.class));
    }

    @Test
    void getLandingPageConfiguration_shouldReturnConfigurationWithDefaults() {
        // Given
        Integer tenantId = 1;
        when(tenantConfigurationRepository.findByTenantIdAndPageAndIsActive(
                eq(tenantId), eq("landing"), eq(true)))
                .thenReturn(Collections.emptyList());
        
        // Set up mock behavior for defaultProvider
        doAnswer(invocation -> {
            LandingPageConfigResponse.LandingPageConfigResponseBuilder builder = invocation.getArgument(0);
            return null; // void method
        }).when(defaultProvider).setDefaultConfigValues(any(LandingPageConfigResponse.LandingPageConfigResponseBuilder.class));
        
        // Mock propertyServiceHelper to return empty list
        when(propertyServiceHelper.getPropertyAssignments(eq(tenantId), eq(true)))
                .thenReturn(Collections.emptyList());
                
        // Mock guestTypeDefinitionService to return empty list
        when(guestTypeDefinitionService.getGuestTypeDefinitionsByTenantId(tenantId))
                .thenReturn(Collections.emptyList());
        
        // When
        LandingPageConfigResponse response = tenantConfigurationService.getLandingPageConfiguration(tenantId);
        
        // Then
        assertNotNull(response);
        verify(defaultProvider).setDefaultConfigValues(any(LandingPageConfigResponse.LandingPageConfigResponseBuilder.class));
    }
    
    @Test
    void getResultsPageConfiguration_shouldReturnConfigurationWithDefaults() {
        // Given
        Integer tenantId = 1;
        when(tenantConfigurationRepository.findByTenantIdAndPageAndIsActive(
                eq(tenantId), eq("results"), eq(true)))
                .thenReturn(Collections.emptyList());
                
        // Set up mock behavior for defaultProvider
        doAnswer(invocation -> {
            ResultsPageConfigResponse.ResultsPageConfigResponseBuilder builder = invocation.getArgument(0);
            return null; // void method
        }).when(defaultProvider).setResultsPageDefaultValues(any(ResultsPageConfigResponse.ResultsPageConfigResponseBuilder.class));
        
        // Mock propertyServiceHelper to return empty list
        when(propertyServiceHelper.getPropertyAssignments(eq(tenantId), eq(true)))
                .thenReturn(Collections.emptyList());
        
        // When
        ResultsPageConfigResponse response = tenantConfigurationService.getResultsPageConfiguration(tenantId);
        
        // Then
        assertNotNull(response);
        verify(defaultProvider).setResultsPageDefaultValues(any(ResultsPageConfigResponse.ResultsPageConfigResponseBuilder.class));
    }
} 