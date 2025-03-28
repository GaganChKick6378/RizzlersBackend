package com.kdu.rizzlers.service;

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
import com.kdu.rizzlers.service.impl.ConfigurationDefaultProvider;
import com.kdu.rizzlers.service.impl.ConfigurationValidator;
import com.kdu.rizzlers.service.impl.PropertyServiceHelper;
import com.kdu.rizzlers.service.impl.TenantConfigurationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TenantConfigurationServiceTest {

    @Mock
    private TenantConfigurationRepository tenantConfigurationRepository;

    @Mock
    private TenantPropertyAssignmentRepository tenantPropertyAssignmentRepository;

    @Mock
    private GuestTypeDefinitionService guestTypeDefinitionService;

    @Mock
    private GraphQLPropertyService graphQLPropertyService;
    
    @Mock
    private PropertyServiceHelper propertyServiceHelper;
    
    @Mock
    private ConfigurationValidator configValidator;
    
    @Mock
    private ConfigurationDefaultProvider defaultProvider;

    @InjectMocks
    private TenantConfigurationServiceImpl tenantConfigurationService;

    private TenantConfiguration testConfiguration;
    private TenantConfigurationRequest testRequest;
    private TenantPropertyAssignment testAssignment;
    private List<GuestTypeDefinitionResponse> testGuestTypes;
    private List<PropertyResponse> testProperties;
    private List<TenantPropertyAssignmentResponse> testPropertyAssignmentResponses;

    @BeforeEach
    void setUp() {
        // Setup test configuration
        testConfiguration = TenantConfiguration.builder()
                .id(1L)
                .tenantId(100)
                .page("landing")
                .field("header_logo")
                .value("{\"url\":\"https://example.com/logo.png\",\"alt\":\"Logo\"}")
                .isActive(true)
                .build();
        testConfiguration.setCreatedAt(LocalDateTime.now());
        testConfiguration.setUpdatedAt(LocalDateTime.now());

        // Setup test request
        testRequest = TenantConfigurationRequest.builder()
                .tenantId(100)
                .page("landing")
                .field("header_logo")
                .value("{\"url\":\"https://example.com/new-logo.png\",\"alt\":\"New Logo\"}")
                .isActive(true)
                .build();

        // Setup test property assignment
        testAssignment = TenantPropertyAssignment.builder()
                .id(1L)
                .tenantId(100)
                .propertyId(200)
                .isAssigned(true)
                .build();
        testAssignment.setCreatedAt(LocalDateTime.now());
        testAssignment.setUpdatedAt(LocalDateTime.now());

        // Setup test guest types
        testGuestTypes = Collections.singletonList(
                GuestTypeDefinitionResponse.builder()
                        .id(1L)
                        .tenantId(100)
                        .guestType("ADULT")
                        .minAge(18)
                        .maxAge(120)
                        .isActive(true)
                        .maxCount(1)
                        .build()
        );

        // Setup test properties
        testProperties = Collections.singletonList(
                PropertyResponse.builder()
                        .propertyId(200)
                        .propertyName("Test Hotel")
                        .propertyAddress("123 Test St")
                        .contactNumber("123-456-7890")
                        .tenantId(100)
                        .build()
        );
        
        // Setup test property assignment responses
        testPropertyAssignmentResponses = Collections.singletonList(
                TenantPropertyAssignmentResponse.builder()
                        .id(1L)
                        .tenantId(100)
                        .propertyId(200)
                        .propertyName("Test Hotel")
                        .propertyAddress("123 Test St")
                        .contactNumber("123-456-7890")
                        .isAssigned(true)
                        .build()
        );
    }

    @Test
    void createConfiguration_ShouldSaveAndReturnConfiguration() {
        // Given
        when(tenantConfigurationRepository.save(any(TenantConfiguration.class))).thenReturn(testConfiguration);

        // When
        TenantConfigurationResponse response = tenantConfigurationService.createConfiguration(testRequest);

        // Then
        assertNotNull(response);
        assertEquals(testConfiguration.getId(), response.getId());
        assertEquals(testConfiguration.getTenantId(), response.getTenantId());
        assertEquals(testConfiguration.getPage(), response.getPage());
        assertEquals(testConfiguration.getField(), response.getField());
        assertEquals(testConfiguration.getValue(), response.getValue());
        assertEquals(testConfiguration.getIsActive(), response.getIsActive());
        verify(tenantConfigurationRepository).save(any(TenantConfiguration.class));
    }

    @Test
    void getConfigurationById_ShouldReturnConfigurationWhenExists() {
        // Given
        Long id = 1L;
        when(tenantConfigurationRepository.findById(id)).thenReturn(Optional.of(testConfiguration));

        // When
        TenantConfigurationResponse response = tenantConfigurationService.getConfigurationById(id);

        // Then
        assertNotNull(response);
        assertEquals(id, response.getId());
        verify(tenantConfigurationRepository).findById(id);
    }

    @Test
    void getConfigurationById_ShouldThrowExceptionWhenNotFound() {
        // Given
        Long id = 999L;
        when(tenantConfigurationRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> tenantConfigurationService.getConfigurationById(id));
        verify(tenantConfigurationRepository).findById(id);
    }

    @Test
    void getAllConfigurations_ShouldReturnAllConfigurations() {
        // Given
        List<TenantConfiguration> configurations = Arrays.asList(testConfiguration);
        when(tenantConfigurationRepository.findAll()).thenReturn(configurations);

        // When
        List<TenantConfigurationResponse> responses = tenantConfigurationService.getAllConfigurations();

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(testConfiguration.getId(), responses.get(0).getId());
        verify(tenantConfigurationRepository).findAll();
    }

    @Test
    void getConfigurationsByTenantId_ShouldReturnConfigurationsForTenant() {
        // Given
        Integer tenantId = 100;
        List<TenantConfiguration> configurations = Arrays.asList(testConfiguration);
        when(tenantConfigurationRepository.findByTenantIdAndIsActive(tenantId, true)).thenReturn(configurations);

        // When
        List<TenantConfigurationResponse> responses = tenantConfigurationService.getConfigurationsByTenantId(tenantId);

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(tenantId, responses.get(0).getTenantId());
        verify(tenantConfigurationRepository).findByTenantIdAndIsActive(tenantId, true);
    }

    @Test
    void getConfigurationsByTenantIdAndPage_ShouldReturnConfigurationsForTenantAndPage() {
        // Given
        Integer tenantId = 100;
        String page = "landing";
        List<TenantConfiguration> configurations = Arrays.asList(testConfiguration);
        when(tenantConfigurationRepository.findByTenantIdAndPageAndIsActive(tenantId, page, true)).thenReturn(configurations);

        // When
        List<TenantConfigurationResponse> responses = tenantConfigurationService.getConfigurationsByTenantIdAndPage(tenantId, page);

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(tenantId, responses.get(0).getTenantId());
        assertEquals(page, responses.get(0).getPage());
        verify(tenantConfigurationRepository).findByTenantIdAndPageAndIsActive(tenantId, page, true);
    }

    @Test
    void getConfigurationByTenantIdAndPageAndField_ShouldReturnConfigurationWhenFound() {
        // Given
        Integer tenantId = 100;
        String page = "landing";
        String field = "header_logo";
        when(tenantConfigurationRepository.findByTenantIdAndPageAndFieldAndIsActive(tenantId, page, field, true))
                .thenReturn(Optional.of(testConfiguration));

        // When
        TenantConfigurationResponse response = tenantConfigurationService.getConfigurationByTenantIdAndPageAndField(tenantId, page, field);

        // Then
        assertNotNull(response);
        assertEquals(tenantId, response.getTenantId());
        assertEquals(page, response.getPage());
        assertEquals(field, response.getField());
        verify(tenantConfigurationRepository).findByTenantIdAndPageAndFieldAndIsActive(tenantId, page, field, true);
    }

    @Test
    void updateConfiguration_ShouldUpdateAndReturnConfiguration() {
        // Given
        Long id = 1L;
        when(tenantConfigurationRepository.findById(id)).thenReturn(Optional.of(testConfiguration));
        when(tenantConfigurationRepository.save(any(TenantConfiguration.class))).thenReturn(testConfiguration);

        // When
        TenantConfigurationResponse response = tenantConfigurationService.updateConfiguration(id, testRequest);

        // Then
        assertNotNull(response);
        assertEquals(id, response.getId());
        assertEquals(testRequest.getTenantId(), response.getTenantId());
        assertEquals(testRequest.getPage(), response.getPage());
        assertEquals(testRequest.getField(), response.getField());
        verify(tenantConfigurationRepository).findById(id);
        verify(tenantConfigurationRepository).save(any(TenantConfiguration.class));
    }

    @Test
    void deleteConfiguration_ShouldSoftDeleteConfiguration() {
        // Given
        Long id = 1L;
        when(tenantConfigurationRepository.findById(id)).thenReturn(Optional.of(testConfiguration));

        // When
        tenantConfigurationService.deleteConfiguration(id);

        // Then
        verify(tenantConfigurationRepository).findById(id);
        verify(tenantConfigurationRepository).save(testConfiguration);
        
        // Verify the configuration was soft deleted (isActive = false)
        assertFalse(testConfiguration.getIsActive());
    }

    @Test
    void getLandingPageConfiguration_WithPropertyDetails_ShouldReturnFullConfiguration() {
        // Given
        Integer tenantId = 100;
        List<TenantConfiguration> configurations = createTestLandingPageConfigurations(tenantId);
        
        when(tenantConfigurationRepository.findByTenantIdAndPageAndIsActive(tenantId, "landing", true))
                .thenReturn(configurations);
        when(guestTypeDefinitionService.getGuestTypeDefinitionsByTenantId(tenantId))
                .thenReturn(testGuestTypes);
        when(propertyServiceHelper.getPropertyAssignments(tenantId, true))
                .thenReturn(testPropertyAssignmentResponses);
        
        // Handle validations to return true for any input
        doAnswer(invocation -> {
            // Mock the behavior of setDefaultConfigValues to not do anything
            return null;
        }).when(defaultProvider).setDefaultConfigValues(any());
        
        // Mock all validator methods to return true
        when(configValidator.validateHeaderLogo(any())).thenReturn(true);
        when(configValidator.validatePageTitle(any())).thenReturn(true);
        when(configValidator.validateBannerImage(any())).thenReturn(true);
        when(configValidator.validateFooter(any())).thenReturn(true);
        when(configValidator.validateLanguages(any())).thenReturn(true);
        when(configValidator.validateCurrencies(any())).thenReturn(true);

        // When
        LandingPageConfigResponse response = tenantConfigurationService.getLandingPageConfiguration(tenantId, true);

        // Then
        assertNotNull(response);
        assertEquals(tenantId, response.getTenantId());
        assertEquals("landing", response.getPage());
        assertNotNull(response.getGuestTypes());
        assertEquals(1, response.getGuestTypes().size());
        assertNotNull(response.getProperties());
        assertEquals(1, response.getProperties().size());
        
        verify(tenantConfigurationRepository).findByTenantIdAndPageAndIsActive(tenantId, "landing", true);
        verify(guestTypeDefinitionService).getGuestTypeDefinitionsByTenantId(tenantId);
        verify(propertyServiceHelper).getPropertyAssignments(tenantId, true);
        verify(defaultProvider).setDefaultConfigValues(any());
    }

    @Test
    void getLandingPageConfiguration_WithoutPropertyDetails_ShouldReturnBasicConfiguration() {
        // Given
        Integer tenantId = 100;
        List<TenantConfiguration> configurations = createTestLandingPageConfigurations(tenantId);
        
        when(tenantConfigurationRepository.findByTenantIdAndPageAndIsActive(tenantId, "landing", true))
                .thenReturn(configurations);
        when(guestTypeDefinitionService.getGuestTypeDefinitionsByTenantId(tenantId))
                .thenReturn(testGuestTypes);
        when(propertyServiceHelper.getPropertyAssignments(tenantId, false))
                .thenReturn(testPropertyAssignmentResponses);
                
        // Handle validations to return true for any input
        doAnswer(invocation -> {
            // Mock the behavior of setDefaultConfigValues to not do anything
            return null;
        }).when(defaultProvider).setDefaultConfigValues(any());
        
        // Mock all validator methods to return true
        when(configValidator.validateHeaderLogo(any())).thenReturn(true);
        when(configValidator.validatePageTitle(any())).thenReturn(true);
        when(configValidator.validateBannerImage(any())).thenReturn(true);
        when(configValidator.validateFooter(any())).thenReturn(true);
        when(configValidator.validateLanguages(any())).thenReturn(true);
        when(configValidator.validateCurrencies(any())).thenReturn(true);

        // When
        LandingPageConfigResponse response = tenantConfigurationService.getLandingPageConfiguration(tenantId, false);

        // Then
        assertNotNull(response);
        assertEquals(tenantId, response.getTenantId());
        assertEquals("landing", response.getPage());
        assertNotNull(response.getGuestTypes());
        assertEquals(1, response.getGuestTypes().size());
        assertNotNull(response.getProperties());
        assertEquals(1, response.getProperties().size());
        
        verify(tenantConfigurationRepository).findByTenantIdAndPageAndIsActive(tenantId, "landing", true);
        verify(guestTypeDefinitionService).getGuestTypeDefinitionsByTenantId(tenantId);
        verify(propertyServiceHelper).getPropertyAssignments(tenantId, false);
        verify(defaultProvider).setDefaultConfigValues(any());
    }

    private List<TenantConfiguration> createTestLandingPageConfigurations(Integer tenantId) {
        List<TenantConfiguration> configurations = new ArrayList<>();
        
        // Header Logo
        configurations.add(TenantConfiguration.builder()
                .id(1L)
                .tenantId(tenantId)
                .page("landing")
                .field("header_logo")
                .value("{\"url\":\"https://example.com/logo.png\",\"alt\":\"Logo\"}")
                .isActive(true)
                .build());
        
        // Page Title
        configurations.add(TenantConfiguration.builder()
                .id(2L)
                .tenantId(tenantId)
                .page("landing")
                .field("page_title")
                .value("{\"text\":\"Welcome to Our Hotel\",\"color\":\"#333333\"}")
                .isActive(true)
                .build());
        
        // Banner Image
        configurations.add(TenantConfiguration.builder()
                .id(3L)
                .tenantId(tenantId)
                .page("landing")
                .field("banner_image")
                .value("{\"url\":\"https://example.com/banner.jpg\",\"alt\":\"Banner\"}")
                .isActive(true)
                .build());
        
        // Footer
        configurations.add(TenantConfiguration.builder()
                .id(4L)
                .tenantId(tenantId)
                .page("landing")
                .field("footer")
                .value("{\"image\":{\"url\":\"https://example.com/footer-logo.png\",\"alt\":\"Footer Logo\"},\"desc\":\"Your trusted travel partner since 1995\",\"copyright\":\"© 2023 Company Name\"}")
                .isActive(true)
                .build());
        
        // Languages
        configurations.add(TenantConfiguration.builder()
                .id(5L)
                .tenantId(tenantId)
                .page("landing")
                .field("languages")
                .value("{\"options\":[{\"code\":\"EN\",\"name\":\"English\",\"active\":true},{\"code\":\"ES\",\"name\":\"Español\",\"active\":true},{\"code\":\"FR\",\"name\":\"Français\",\"active\":true},{\"code\":\"DE\",\"name\":\"Deutsch\",\"active\":true},{\"code\":\"IT\",\"name\":\"Italiano\",\"active\":true}],\"default\":\"EN\"}")
                .isActive(true)
                .build());
        
        // Currencies
        configurations.add(TenantConfiguration.builder()
                .id(6L)
                .tenantId(tenantId)
                .page("landing")
                .field("currencies")
                .value("{\"options\":[{\"code\":\"USD\",\"symbol\":\"$\",\"name\":\"US Dollar\",\"active\":true},{\"code\":\"EUR\",\"symbol\":\"€\",\"name\":\"Euro\",\"active\":true},{\"code\":\"GBP\",\"symbol\":\"£\",\"name\":\"British Pound\",\"active\":true},{\"code\":\"INR\",\"symbol\":\"₹\",\"name\":\"Indian Rupee\",\"active\":true}],\"default\":\"USD\"}")
                .isActive(true)
                .build());
        
        return configurations;
    }
} 