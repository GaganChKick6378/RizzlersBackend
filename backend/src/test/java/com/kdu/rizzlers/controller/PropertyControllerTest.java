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
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Mono;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(PropertyController.class)
@AutoConfigureMockMvc(addFilters = false)
class PropertyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PropertyGraphQLService propertyGraphQLService;

    private Property validProperty;

    @BeforeEach
    void setUp() {
        // Create a valid property
        validProperty = Property.builder()
                .property_id(101)
                .property_name("Grand Hotel")
                .property_address("123 Main Street, City")
                .contact_number("555-1234")
                .tenant_id(1)
                .build();
    }

    @Test
    @DisplayName("Get property by name with existing name should return property")
    void getPropertyByName_withExistingName_shouldReturnProperty() throws Exception {
        String propertyName = "Grand Hotel";
        
        when(propertyGraphQLService.getPropertyByName(propertyName))
                .thenReturn(Mono.just(validProperty));

        mockMvc.perform(get("/properties/graphql/name/{propertyName}", propertyName)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.property_id", is(101)))
                .andExpect(jsonPath("$.property_name", is("Grand Hotel")))
                .andExpect(jsonPath("$.property_address", is("123 Main Street, City")))
                .andExpect(jsonPath("$.contact_number", is("555-1234")))
                .andExpect(jsonPath("$.tenant_id", is(1)));
    }

    @Test
    @DisplayName("Get property by name with non-existing name should return not found")
    void getPropertyByName_withNonExistingName_shouldReturnNotFound() throws Exception {
        String propertyName = "Non Existing Hotel";
        
        when(propertyGraphQLService.getPropertyByName(propertyName))
                .thenReturn(Mono.empty());

        mockMvc.perform(get("/properties/graphql/name/{propertyName}", propertyName)
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Get property by name with service error should return not found")
    void getPropertyByName_withServiceError_shouldReturnNotFound() throws Exception {
        String propertyName = "Error Hotel";
        
        when(propertyGraphQLService.getPropertyByName(propertyName))
                .thenReturn(Mono.empty());

        mockMvc.perform(get("/properties/graphql/name/{propertyName}", propertyName)
                .with(csrf()))
                .andExpect(status().isNotFound());
    }
} 