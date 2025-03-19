package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.dto.out.TenantConfigurationResponse;
import com.kdu.rizzlers.service.TenantConfigurationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class TenantConfigurationControllerTest {

    @Mock
    private TenantConfigurationService tenantConfigurationService;

    @InjectMocks
    private TenantConfigurationController tenantConfigurationController;

    private TenantConfigurationResponse mockConfigResponse;
    private List<TenantConfigurationResponse> mockConfigResponseList;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize mock response
        mockConfigResponse = TenantConfigurationResponse.builder()
                .id(1L)
                .tenantId(1)
                .page("landing")
                .field("header_logo")
                .value("{\"url\": \"https://example.com/logo.png\", \"alt\": \"Logo\"}")
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        mockConfigResponseList = Arrays.asList(mockConfigResponse);
    }

    @Test
    void getConfigurationById_ShouldReturnConfiguration_WhenValidIdProvided() {
        // Arrange
        Long configId = 1L;
        when(tenantConfigurationService.getConfigurationById(configId)).thenReturn(mockConfigResponse);

        // Act
        ResponseEntity<TenantConfigurationResponse> response = tenantConfigurationController.getConfigurationById(configId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        TenantConfigurationResponse configResponse = response.getBody();
        assertNotNull(configResponse);
        assertEquals(configId, configResponse.getId());
        assertEquals(1, configResponse.getTenantId());
        assertEquals("landing", configResponse.getPage());
        assertEquals("header_logo", configResponse.getField());
        assertEquals("{\"url\": \"https://example.com/logo.png\", \"alt\": \"Logo\"}", configResponse.getValue());
        assertEquals(true, configResponse.getIsActive());
    }
    
    @Test
    void getConfigurationsByTenantId_ShouldReturnConfigurations_WhenValidTenantIdProvided() {
        // Arrange
        Integer tenantId = 1;
        when(tenantConfigurationService.getConfigurationsByTenantId(tenantId)).thenReturn(mockConfigResponseList);

        // Act
        ResponseEntity<List<TenantConfigurationResponse>> response = tenantConfigurationController.getConfigurationsByTenantId(tenantId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        List<TenantConfigurationResponse> configResponses = response.getBody();
        assertNotNull(configResponses);
        assertEquals(1, configResponses.size());
        assertEquals(tenantId, configResponses.get(0).getTenantId());
    }
} 