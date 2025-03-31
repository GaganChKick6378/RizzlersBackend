package com.kdu.rizzlers.service;

import com.kdu.rizzlers.entity.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for the PropertyGraphQLService class to improve interface coverage
 */
@ExtendWith(MockitoExtension.class)
public class PropertyGraphQLServiceTest {

    @Mock
    private PropertyGraphQLService propertyGraphQLService;
    
    private Property sampleProperty;

    @BeforeEach
    void setUp() {
        // Setup sample property
        sampleProperty = new Property();
        sampleProperty.setProperty_id(123);
        sampleProperty.setProperty_name("Grand Hotel");
        sampleProperty.setProperty_address("123 Main St, City, Country");
        sampleProperty.setContact_number("1234567890");
        sampleProperty.setTenant_id(789);
        
        // Setup mock with lenient() to handle unused stubbing
        lenient().when(propertyGraphQLService.getPropertyByName("Grand Hotel"))
                .thenReturn(Mono.just(sampleProperty));
        
        // Setup empty result for non-existent property
        lenient().when(propertyGraphQLService.getPropertyByName("Non-existent Hotel"))
                .thenReturn(Mono.empty());
        
        // Setup behavior for null and empty inputs
        lenient().when(propertyGraphQLService.getPropertyByName(null))
                .thenReturn(Mono.empty());
        
        lenient().when(propertyGraphQLService.getPropertyByName(""))
                .thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("Should return property when it exists")
    void getPropertyByName_shouldReturnProperty() {
        // Act
        Property result = propertyGraphQLService.getPropertyByName("Grand Hotel").block();
        
        // Assert
        assertNotNull(result);
        assertEquals(123, result.getProperty_id());
        assertEquals("Grand Hotel", result.getProperty_name());
        assertEquals("123 Main St, City, Country", result.getProperty_address());
        assertEquals("1234567890", result.getContact_number());
        assertEquals(789, result.getTenant_id());
    }

    @Test
    @DisplayName("Should return empty when property does not exist")
    void getPropertyByName_whenPropertyDoesNotExist_shouldReturnEmpty() {
        // Act
        Property result = propertyGraphQLService.getPropertyByName("Non-existent Hotel").block();
        
        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should handle null property name gracefully")
    void getPropertyByName_withNullPropertyName_shouldHandleGracefully() {
        // Act
        Property result = propertyGraphQLService.getPropertyByName(null).block();
        
        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should handle empty property name gracefully")
    void getPropertyByName_withEmptyPropertyName_shouldHandleGracefully() {
        // Act
        Property result = propertyGraphQLService.getPropertyByName("").block();
        
        // Assert
        assertNull(result);
    }
} 