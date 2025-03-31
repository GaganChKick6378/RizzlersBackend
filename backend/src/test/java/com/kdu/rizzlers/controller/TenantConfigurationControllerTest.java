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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
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

    private TenantConfigurationRequest validRequest;
    private TenantConfigurationResponse configResponse1;
    private TenantConfigurationResponse configResponse2;
    private List<TenantConfigurationResponse> configResponseList;
    private LandingPageConfigResponse landingPageConfig;
    private ResultsPageConfigResponse resultsPageConfig;

    @BeforeEach
    void setUp() {
        // Create a valid request
        validRequest = TenantConfigurationRequest.builder()
                .tenantId(1)
                .page("landing")
                .field("searchBar")
                .value("{\"enabled\":true,\"placeholderText\":\"Search for rooms...\",\"buttonText\":\"Search\"}")
                .isActive(true)
                .build();

        LocalDateTime now = LocalDateTime.now();

        // Create response objects
        configResponse1 = TenantConfigurationResponse.builder()
                .id(1L)
                .tenantId(1)
                .page("landing")
                .field("searchBar")
                .value("{\"enabled\":true,\"placeholderText\":\"Search for rooms...\",\"buttonText\":\"Search\"}")
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        configResponse2 = TenantConfigurationResponse.builder()
                .id(2L)
                .tenantId(1)
                .page("results")
                .field("filters")
                .value("{\"enabled\":true,\"show\":true,\"position\":\"left\"}")
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        configResponseList = Arrays.asList(configResponse1, configResponse2);

        // Create landing page config
        Map<String, Object> searchBar = new HashMap<>();
        searchBar.put("enabled", true);
        searchBar.put("placeholderText", "Search for rooms...");
        searchBar.put("buttonText", "Search");

        Map<String, Object> featuredRooms = new HashMap<>();
        featuredRooms.put("enabled", true);
        featuredRooms.put("title", "Featured Rooms");
        featuredRooms.put("displayCount", 3);

        Map<String, Object> headerLogo = new HashMap<>();
        headerLogo.put("url", "logo.jpg");

        Map<String, Object> pageTitle = new HashMap<>();
        pageTitle.put("text", "Grand Hotel");

        Map<String, Object> bannerImage = new HashMap<>();
        bannerImage.put("url", "hotel.jpg");

        landingPageConfig = LandingPageConfigResponse.builder()
                .tenantId(1)
                .page("landing")
                .headerLogo(headerLogo)
                .pageTitle(pageTitle)
                .bannerImage(bannerImage)
                .build();

        // Create results page config
        Map<String, Object> filters = new HashMap<>();
        filters.put("enabled", true);
        filters.put("show", true);
        filters.put("position", "left");

        Map<String, Object> sorting = new HashMap<>();
        sorting.put("enabled", true);
        sorting.put("options", Arrays.asList("price-low-high", "price-high-low", "rating"));

        Map<String, Object> pagination = new HashMap<>();
        pagination.put("enabled", true);
        pagination.put("itemsPerPage", 10);

        Map<String, Object> displayOptions = new HashMap<>();
        displayOptions.put("view", "grid");
        displayOptions.put("showPrices", true);

        Map<String, Object> propertyImage = new HashMap<>();
        propertyImage.put("url", "hotel.jpg");

        resultsPageConfig = ResultsPageConfigResponse.builder()
                .tenantId(1)
                .page("results")
                .filters(filters)
                .sorting(sorting)
                .pagination(pagination)
                .displayOptions(displayOptions)
                .propertyImage(propertyImage)
                .build();
    }

    @Test
    @DisplayName("Create configuration with valid request should return created status")
    void createConfiguration_withValidRequest_shouldReturnCreatedStatus() throws Exception {
        when(tenantConfigurationService.createConfiguration(any(TenantConfigurationRequest.class)))
                .thenReturn(configResponse1);

        mockMvc.perform(post("/tenant-configurations")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.tenantId", is(1)))
                .andExpect(jsonPath("$.page", is("landing")))
                .andExpect(jsonPath("$.field", is("searchBar")))
                .andExpect(jsonPath("$.isActive", is(true)));
    }

    @Test
    @DisplayName("Get configuration by ID should return configuration")
    void getConfigurationById_shouldReturnConfiguration() throws Exception {
        when(tenantConfigurationService.getConfigurationById(1L))
                .thenReturn(configResponse1);

        mockMvc.perform(get("/tenant-configurations/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.tenantId", is(1)))
                .andExpect(jsonPath("$.page", is("landing")))
                .andExpect(jsonPath("$.field", is("searchBar")));
    }

    @Test
    @DisplayName("Get all configurations should return list of configurations")
    void getAllConfigurations_shouldReturnListOfConfigurations() throws Exception {
        when(tenantConfigurationService.getAllConfigurations())
                .thenReturn(configResponseList);

        mockMvc.perform(get("/tenant-configurations")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].page", is("landing")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].page", is("results")));
    }

    @Test
    @DisplayName("Get configurations by tenant ID should return tenant's configurations")
    void getConfigurationsByTenantId_shouldReturnTenantConfigurations() throws Exception {
        when(tenantConfigurationService.getConfigurationsByTenantId(1))
                .thenReturn(configResponseList);

        mockMvc.perform(get("/tenant-configurations/tenant/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].tenantId", is(1)))
                .andExpect(jsonPath("$[1].tenantId", is(1)));
    }

    @Test
    @DisplayName("Get configurations by tenant ID and page should return specific configurations")
    void getConfigurationsByTenantIdAndPage_shouldReturnSpecificConfigurations() throws Exception {
        when(tenantConfigurationService.getConfigurationsByTenantIdAndPage(1, "landing"))
                .thenReturn(Arrays.asList(configResponse1));

        mockMvc.perform(get("/tenant-configurations/tenant/1/page/landing")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tenantId", is(1)))
                .andExpect(jsonPath("$[0].page", is("landing")));
    }

    @Test
    @DisplayName("Get configuration by tenant ID, page, and field should return specific configuration")
    void getConfigurationByTenantIdAndPageAndField_shouldReturnSpecificConfiguration() throws Exception {
        when(tenantConfigurationService.getConfigurationByTenantIdAndPageAndField(1, "landing", "searchBar"))
                .thenReturn(configResponse1);

        mockMvc.perform(get("/tenant-configurations/tenant/1/page/landing/field/searchBar")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId", is(1)))
                .andExpect(jsonPath("$.page", is("landing")))
                .andExpect(jsonPath("$.field", is("searchBar")));
    }

    @Test
    @DisplayName("Get landing page configuration should return structured config")
    void getLandingPageConfiguration_shouldReturnStructuredConfig() throws Exception {
        when(tenantConfigurationService.getLandingPageConfiguration(1))
                .thenReturn(landingPageConfig);

        mockMvc.perform(get("/tenant-configurations/tenant/1/landing")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId", is(1)))
                .andExpect(jsonPath("$.page", is("landing")))
                .andExpect(jsonPath("$.header_logo.url", is("logo.jpg")))
                .andExpect(jsonPath("$.page_title.text", is("Grand Hotel")));
    }

    @Test
    @DisplayName("Get basic landing page configuration should return structured config")
    void getBasicLandingPageConfiguration_shouldReturnStructuredConfig() throws Exception {
        when(tenantConfigurationService.getLandingPageConfiguration(eq(1), eq(true)))
                .thenReturn(landingPageConfig);

        mockMvc.perform(get("/tenant-configurations/tenant/1/landing/basic")
                .param("fetch_property_details", "true")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenantId", is(1)))
                .andExpect(jsonPath("$.page", is("landing")))
                .andExpect(jsonPath("$.header_logo.url", is("logo.jpg")));
    }

    @Test
    @DisplayName("Get results page configuration should return structured config")
    void getResultsPageConfiguration_shouldReturnStructuredConfig() throws Exception {
        when(tenantConfigurationService.getResultsPageConfiguration(1))
                .thenReturn(resultsPageConfig);

        mockMvc.perform(get("/tenant-configurations/tenant/1/results")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filters.enabled", is(true)))
                .andExpect(jsonPath("$.filters.show", is(true)))
                .andExpect(jsonPath("$.sorting.enabled", is(true)))
                .andExpect(jsonPath("$.pagination.enabled", is(true)));
    }

    @Test
    @DisplayName("Get basic results page configuration should return structured config")
    void getBasicResultsPageConfiguration_shouldReturnStructuredConfig() throws Exception {
        when(tenantConfigurationService.getResultsPageConfiguration(eq(1), eq(true)))
                .thenReturn(resultsPageConfig);

        mockMvc.perform(get("/tenant-configurations/tenant/1/results/basic")
                .param("fetch_property_details", "true")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filters.enabled", is(true)))
                .andExpect(jsonPath("$.propertyImage.url", is("hotel.jpg")));
    }

    @Test
    @DisplayName("Update configuration should return updated configuration")
    void updateConfiguration_shouldReturnUpdatedConfiguration() throws Exception {
        TenantConfigurationResponse updatedResponse = TenantConfigurationResponse.builder()
                .id(1L)
                .tenantId(1)
                .page("landing")
                .field("searchBar")
                .value("{\"enabled\":false,\"placeholderText\":\"Find rooms...\",\"buttonText\":\"Find\"}")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(tenantConfigurationService.updateConfiguration(eq(1L), any(TenantConfigurationRequest.class)))
                .thenReturn(updatedResponse);

        // Modified request with different values
        TenantConfigurationRequest updateRequest = TenantConfigurationRequest.builder()
                .tenantId(1)
                .page("landing")
                .field("searchBar")
                .value("{\"enabled\":false,\"placeholderText\":\"Find rooms...\",\"buttonText\":\"Find\"}")
                .isActive(true)
                .build();

        mockMvc.perform(put("/tenant-configurations/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.value", is("{\"enabled\":false,\"placeholderText\":\"Find rooms...\",\"buttonText\":\"Find\"}")));
    }

    @Test
    @DisplayName("Delete configuration should return no content")
    void deleteConfiguration_shouldReturnNoContent() throws Exception {
        doNothing().when(tenantConfigurationService).deleteConfiguration(1L);

        mockMvc.perform(delete("/tenant-configurations/1")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }
} 