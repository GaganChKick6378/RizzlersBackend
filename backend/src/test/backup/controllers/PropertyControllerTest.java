package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.entity.Property;
import com.kdu.rizzlers.service.PropertyGraphQLService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(PropertyController.class)
@AutoConfigureMockMvc(addFilters = false)
class PropertyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PropertyGraphQLService propertyGraphQLService;

    private Property property;
    private String propertyName;

    @BeforeEach
    void setUp() {
        propertyName = "Grand Hotel";
        
        // Create a sample property
        property = new Property();
        property.setPropertyId(1);
        property.setPropertyName(propertyName);
        property.setPropertyAddress("123 Main St");
    }

    @Test
    @DisplayName("Should get property by name when property exists")
    void getPropertyByName_whenPropertyExists_shouldReturnProperty() throws Exception {
        // Arrange
        when(propertyGraphQLService.getPropertyByName(propertyName))
                .thenReturn(Mono.just(property));

        // Act & Assert
        mockMvc.perform(get("/properties/graphql/name/{propertyName}", propertyName)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.propertyId").value(1))
                .andExpect(jsonPath("$.propertyName").value(propertyName))
                .andExpect(jsonPath("$.propertyAddress").value("123 Main St"));
                
        verify(propertyGraphQLService, times(1)).getPropertyByName(propertyName);
    }

    @Test
    @DisplayName("Should return 404 when property does not exist")
    void getPropertyByName_whenPropertyDoesNotExist_shouldReturn404() throws Exception {
        // Arrange
        when(propertyGraphQLService.getPropertyByName(propertyName))
                .thenReturn(Mono.empty());

        // Act & Assert
        mockMvc.perform(get("/properties/graphql/name/{propertyName}", propertyName)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
                
        verify(propertyGraphQLService, times(1)).getPropertyByName(propertyName);
    }
} 