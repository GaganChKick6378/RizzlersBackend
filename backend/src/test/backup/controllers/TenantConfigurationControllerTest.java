package com.kdu.rizzlers.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kdu.rizzlers.dto.in.TenantConfigurationRequest;
import com.kdu.rizzlers.dto.out.LandingPageConfigResponse;
import com.kdu.rizzlers.dto.out.ResultsPageConfigResponse;
import com.kdu.rizzlers.dto.out.TenantConfigurationResponse;
import com.kdu.rizzlers.service.TenantConfigurationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(TenantConfigurationController.class)
@AutoConfigureMockMvc(addFilters = false)
class TenantConfigurationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TenantConfigurationService tenantConfigurationService;

    private TenantConfigurationRequest configRequest;
    private TenantConfigurationResponse configResponse;
    private LandingPageConfigResponse landingPageResponse;
    private ResultsPageConfigResponse resultsPageResponse;
    private List<TenantConfigurationResponse> configResponses;
    private Long configId;
    private Integer tenantId;

    @BeforeEach
    void setUp() {
        configId = 1L;
        tenantId = 100;

        // Create request object
        configRequest = new TenantConfigurationRequest();
        configRequest.setTenantId(tenantId);
        configRequest.setPage("landing");
        configRequest.setField("header");
        configRequest.setValue("Welcome to our hotel");

        // Create response object
        configResponse = new TenantConfigurationResponse();
        configResponse.setId(configId);
        configResponse.setTenantId(tenantId);
        configResponse.setPage("landing");
        configResponse.setField("header");
        configResponse.setValue("Welcome to our hotel");
        configResponse.setCreatedAt(new Date());
        configResponse.setUpdatedAt(new Date());

        // Create list of responses
        configResponses = Arrays.asList(configResponse);

        // Setup landing page response
        landingPageResponse = new LandingPageConfigResponse();
        landingPageResponse.setHeroSection(new HashMap<>());
        landingPageResponse.getHeroSection().put("title", "Welcome to our hotel");
        landingPageResponse.getHeroSection().put("subtitle", "Book your stay with us");
        
        // Setup results page response
        resultsPageResponse = new ResultsPageConfigResponse();
        resultsPageResponse.setFilters(new HashMap<>());
        resultsPageResponse.getFilters().put("enabled", true);
        resultsPageResponse.getFilters().put("show", true);
        resultsPageResponse.setSorting(new HashMap<>());
        resultsPageResponse.getSorting().put("enabled", true);
        resultsPageResponse.setPagination(new HashMap<>());
        resultsPageResponse.getPagination().put("enabled", true);
        resultsPageResponse.setDisplayOptions(new HashMap<>());
        resultsPageResponse.setPropertyImage("http://example.com/image.jpg");
    }

    @Test
    @DisplayName("Should create a new tenant configuration")
    void createConfiguration_shouldCreateConfiguration() throws Exception {
        // Arrange
        when(tenantConfigurationService.createConfiguration(any(TenantConfigurationRequest.class)))
                .thenReturn(configResponse);

        // Act & Assert
        mockMvc.perform(post("/tenant-configurations")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(configRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(configId.intValue())))
                .andExpect(jsonPath("$.tenantId", is(tenantId)))
                .andExpect(jsonPath("$.page", is("landing")))
                .andExpect(jsonPath("$.field", is("header")))
                .andExpect(jsonPath("$.value", is("Welcome to our hotel")));
    }

    @Test
    @DisplayName("Should get configuration by ID")
    void getConfigurationById_shouldReturnConfiguration() throws Exception {
        // Arrange
        when(tenantConfigurationService.getConfigurationById(configId))
                .thenReturn(configResponse);

        // Act & Assert
        mockMvc.perform(get("/tenant-configurations/{id}", configId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(configId.intValue())))
                .andExpect(jsonPath("$.tenantId", is(tenantId)))
                .andExpect(jsonPath("$.page", is("landing")))
                .andExpect(jsonPath("$.field", is("header")))
                .andExpect(jsonPath("$.value", is("Welcome to our hotel")));
    }

    @Test
    @DisplayName("Should get all configurations")
    void getAllConfigurations_shouldReturnAllConfigurations() throws Exception {
        // Arrange
        when(tenantConfigurationService.getAllConfigurations())
                .thenReturn(configResponses);

        // Act & Assert
        mockMvc.perform(get("/tenant-configurations")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(configId.intValue())))
                .andExpect(jsonPath("$[0].tenantId", is(tenantId)))
                .andExpect(jsonPath("$[0].page", is("landing")));
    }

    @Test
    @DisplayName("Should get configurations by tenant ID")
    void getConfigurationsByTenantId_shouldReturnTenantConfigurations() throws Exception {
        // Arrange
        when(tenantConfigurationService.getConfigurationsByTenantId(tenantId))
                .thenReturn(configResponses);

        // Act & Assert
        mockMvc.perform(get("/tenant-configurations/tenant/{tenantId}", tenantId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tenantId", is(tenantId)));
    }

    @Test
    @DisplayName("Should get configurations by tenant ID and page")
    void getConfigurationsByTenantIdAndPage_shouldReturnTenantPageConfigurations() throws Exception {
        // Arrange
        when(tenantConfigurationService.getConfigurationsByTenantIdAndPage(eq(tenantId), eq("landing")))
                .thenReturn(configResponses);

        // Act & Assert
        mockMvc.perform(get("/tenant-configurations/tenant/{tenantId}/page/{page}", tenantId, "landing")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tenantId", is(tenantId)))
                .andExpect(jsonPath("$[0].page", is("landing")));
    }

    @Test
    @DisplayName("Should get configuration by tenant ID, page and field")
    void getConfigurationByTenantIdAndPageAndField_shouldReturnSpecificConfiguration() throws Exception {
        // Arrange
        when(tenantConfigurationService.getConfigurationByTenantIdAndPageAndField(
                eq(tenantId), eq("landing"), eq("header")))
                .thenReturn(configResponse);

        // Act & Assert
        mockMvc.perform(get("/tenant-configurations/tenant/{tenantId}/page/{page}/field/{field}",
                tenantId, "landing", "header")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId", is(tenantId)))
                .andExpect(jsonPath("$.page", is("landing")))
                .andExpect(jsonPath("$.field", is("header")));
    }

    @Test
    @DisplayName("Should get landing page configuration")
    void getLandingPageConfiguration_shouldReturnLandingPageConfig() throws Exception {
        // Arrange
        when(tenantConfigurationService.getLandingPageConfiguration(tenantId))
                .thenReturn(landingPageResponse);

        // Act & Assert
        mockMvc.perform(get("/tenant-configurations/tenant/{tenantId}/landing", tenantId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.heroSection.title", is("Welcome to our hotel")))
                .andExpect(jsonPath("$.heroSection.subtitle", is("Book your stay with us")));
    }

    @Test
    @DisplayName("Should get basic landing page configuration")
    void getBasicLandingPageConfiguration_shouldReturnLandingPageConfig() throws Exception {
        // Arrange
        when(tenantConfigurationService.getLandingPageConfiguration(eq(tenantId), anyBoolean()))
                .thenReturn(landingPageResponse);

        // Act & Assert
        mockMvc.perform(get("/tenant-configurations/tenant/{tenantId}/landing/basic", tenantId)
                .with(csrf())
                .param("fetch_property_details", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.heroSection.title", is("Welcome to our hotel")));
    }

    @Test
    @DisplayName("Should get results page configuration")
    void getResultsPageConfiguration_shouldReturnResultsPageConfig() throws Exception {
        // Arrange
        when(tenantConfigurationService.getResultsPageConfiguration(tenantId))
                .thenReturn(resultsPageResponse);

        // Act & Assert
        mockMvc.perform(get("/tenant-configurations/tenant/{tenantId}/results", tenantId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filters.enabled", is(true)))
                .andExpect(jsonPath("$.sorting.enabled", is(true)))
                .andExpect(jsonPath("$.pagination.enabled", is(true)))
                .andExpect(jsonPath("$.propertyImage", is("http://example.com/image.jpg")));
    }

    @Test
    @DisplayName("Should get basic results page configuration")
    void getBasicResultsPageConfiguration_shouldReturnResultsPageConfig() throws Exception {
        // Arrange
        when(tenantConfigurationService.getResultsPageConfiguration(eq(tenantId), anyBoolean()))
                .thenReturn(resultsPageResponse);

        // Act & Assert
        mockMvc.perform(get("/tenant-configurations/tenant/{tenantId}/results/basic", tenantId)
                .with(csrf())
                .param("fetch_property_details", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filters.enabled", is(true)))
                .andExpect(jsonPath("$.propertyImage", is("http://example.com/image.jpg")));
    }

    @Test
    @DisplayName("Should update configuration")
    void updateConfiguration_shouldUpdateConfiguration() throws Exception {
        // Arrange
        when(tenantConfigurationService.updateConfiguration(eq(configId), any(TenantConfigurationRequest.class)))
                .thenReturn(configResponse);

        // Act & Assert
        mockMvc.perform(put("/tenant-configurations/{id}", configId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(configRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(configId.intValue())))
                .andExpect(jsonPath("$.tenantId", is(tenantId)))
                .andExpect(jsonPath("$.value", is("Welcome to our hotel")));
    }

    @Test
    @DisplayName("Should delete configuration")
    void deleteConfiguration_shouldDeleteConfiguration() throws Exception {
        // Arrange
        doNothing().when(tenantConfigurationService).deleteConfiguration(configId);

        // Act & Assert
        mockMvc.perform(delete("/tenant-configurations/{id}", configId)
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(tenantConfigurationService, times(1)).deleteConfiguration(configId);
    }
} 