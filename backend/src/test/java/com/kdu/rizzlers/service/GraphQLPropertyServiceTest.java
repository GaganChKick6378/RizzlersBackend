package com.kdu.rizzlers.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kdu.rizzlers.dto.out.PropertyResponse;
import com.kdu.rizzlers.service.impl.GraphQLPropertyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for the GraphQLPropertyService interface to ensure proper contract behavior
 */
@ExtendWith(MockitoExtension.class)
class GraphQLPropertyServiceTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private GraphQLPropertyService graphQLPropertyService;
    private ObjectMapper objectMapper;

    private PropertyResponse property1;
    private PropertyResponse property2;
    private List<Integer> propertyIds;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        graphQLPropertyService = new GraphQLPropertyServiceImpl(webClientBuilder, objectMapper);
        
        // Set values for the properties using reflection
        ReflectionTestUtils.setField(graphQLPropertyService, "graphqlUrl", "http://example.com/graphql");
        ReflectionTestUtils.setField(graphQLPropertyService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(graphQLPropertyService, "apiKeyHeader", "x-api-key");
        ReflectionTestUtils.setField(graphQLPropertyService, "timeout", 5000);
        
        // Mock the WebClient chain - use lenient() to avoid UnnecessaryStubbingException
        lenient().when(webClientBuilder.build()).thenReturn(webClient);
        lenient().when(webClient.post()).thenReturn(requestBodyUriSpec);
        lenient().when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        lenient().when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
        lenient().when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        lenient().when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        // Setup test properties
        property1 = PropertyResponse.builder()
                .propertyId(101)
                .propertyName("Luxury Hotel")
                .propertyAddress("123 Main St, New York")
                .contactNumber("555-1234")
                .tenantId(1)
                .build();

        property2 = PropertyResponse.builder()
                .propertyId(102)
                .propertyName("Beach Resort")
                .propertyAddress("456 Ocean Dr, Miami")
                .contactNumber("555-5678")
                .tenantId(1)
                .build();

        propertyIds = Arrays.asList(101, 102);
    }

    @Test
    @DisplayName("getPropertiesByIds should return properties matching the provided IDs")
    void getPropertiesByIds_shouldReturnMatchingProperties() {
        // Arrange
        String validResponse = """
                {
                  "data": {
                    "listProperties": [
                      {
                        "property_id": 101,
                        "property_name": "Luxury Hotel",
                        "property_address": "123 Main St, New York",
                        "contact_number": "555-1234",
                        "tenant_id": 1
                      },
                      {
                        "property_id": 102,
                        "property_name": "Beach Resort",
                        "property_address": "456 Ocean Dr, Miami",
                        "contact_number": "555-5678",
                        "tenant_id": 1
                      }
                    ]
                  }
                }
                """;
        
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(validResponse));
        
        // Act
        List<PropertyResponse> result = graphQLPropertyService.getPropertiesByIds(propertyIds);
        
        // Assert
        assertEquals(2, result.size());
        assertEquals(101, result.get(0).getPropertyId());
        assertEquals("Luxury Hotel", result.get(0).getPropertyName());
        assertEquals(102, result.get(1).getPropertyId());
        assertEquals("Beach Resort", result.get(1).getPropertyName());
    }

    @Test
    @DisplayName("getPropertiesByIds should return empty list when no properties match")
    void getPropertiesByIds_withNoMatches_shouldReturnEmptyList() {
        // Arrange
        String emptyResponse = """
                {
                  "data": {
                    "listProperties": []
                  }
                }
                """;
        
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(emptyResponse));
        
        // Act
        List<PropertyResponse> result = graphQLPropertyService.getPropertiesByIds(Arrays.asList(999, 888));
        
        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getPropertiesByIds should handle null input gracefully")
    void getPropertiesByIds_withNullInput_shouldHandleGracefully() {
        // Act
        List<PropertyResponse> result = graphQLPropertyService.getPropertiesByIds(null);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        // Verify that no WebClient calls were made
        verify(webClientBuilder, never()).build();
    }

    @Test
    @DisplayName("getPropertiesByIds should return subset of properties that match")
    void getPropertiesByIds_withPartialMatches_shouldReturnMatchingSubset() {
        // Arrange
        String partialResponse = """
                {
                  "data": {
                    "listProperties": [
                      {
                        "property_id": 101,
                        "property_name": "Luxury Hotel",
                        "property_address": "123 Main St, New York",
                        "contact_number": "555-1234",
                        "tenant_id": 1
                      }
                    ]
                  }
                }
                """;
        
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(partialResponse));
        
        // Act
        List<PropertyResponse> result = graphQLPropertyService.getPropertiesByIds(Arrays.asList(101, 999));
        
        // Assert
        assertEquals(1, result.size());
        assertEquals(101, result.get(0).getPropertyId());
        assertEquals("Luxury Hotel", result.get(0).getPropertyName());
    }

    @Test
    void getPropertiesByIds_ShouldReturnPropertiesWhenFoundInResponse() {
        // Given
        String validResponse = """
                {
                  "data": {
                    "listProperties": [
                      {
                        "property_id": 1,
                        "property_name": "Hotel A",
                        "property_address": "123 Main St",
                        "contact_number": "555-1234"
                      },
                      {
                        "property_id": 2,
                        "property_name": "Hotel B",
                        "property_address": "456 Oak Ave",
                        "contact_number": "555-5678"
                      }
                    ]
                  }
                }
                """;
        
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(validResponse));
        
        List<Integer> propertyIds = Arrays.asList(1, 2);
        
        // When
        List<PropertyResponse> result = graphQLPropertyService.getPropertiesByIds(propertyIds);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getPropertyId());
        assertEquals("Hotel A", result.get(0).getPropertyName());
        assertEquals("123 Main St", result.get(0).getPropertyAddress());
        assertEquals("555-1234", result.get(0).getContactNumber());
        
        verify(webClientBuilder).build();
        verify(webClient).post();
        verify(requestBodyUriSpec).uri("http://example.com/graphql");
        verify(requestBodySpec).contentType(MediaType.APPLICATION_JSON);
        verify(requestBodySpec).header("x-api-key", "test-api-key");
        verify(requestBodySpec).bodyValue(any());
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToMono(String.class);
    }

    @Test
    void getPropertiesByIds_ShouldFilterPropertiesBasedOnIds() {
        // Given
        String validResponse = """
                {
                  "data": {
                    "listProperties": [
                      {
                        "property_id": 1,
                        "property_name": "Hotel A",
                        "property_address": "123 Main St",
                        "contact_number": "555-1234"
                      },
                      {
                        "property_id": 2,
                        "property_name": "Hotel B",
                        "property_address": "456 Oak Ave",
                        "contact_number": "555-5678"
                      },
                      {
                        "property_id": 3,
                        "property_name": "Hotel C",
                        "property_address": "789 Pine Rd",
                        "contact_number": "555-9012"
                      }
                    ]
                  }
                }
                """;
        
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(validResponse));
        
        // Only request properties 1 and 3
        List<Integer> propertyIds = Arrays.asList(1, 3);
        
        // When
        List<PropertyResponse> result = graphQLPropertyService.getPropertiesByIds(propertyIds);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        // Verify it only contains properties 1 and 3
        List<Integer> resultIds = result.stream()
                .map(PropertyResponse::getPropertyId)
                .toList();
        assertTrue(resultIds.contains(1));
        assertTrue(resultIds.contains(3));
        assertFalse(resultIds.contains(2));
    }

    @Test
    void getPropertiesByIds_ShouldReturnEmptyListWhenNoIdsProvided() {
        // Given
        List<Integer> emptyPropertyIds = Collections.emptyList();
        
        // When
        List<PropertyResponse> result = graphQLPropertyService.getPropertiesByIds(emptyPropertyIds);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        // Verify no WebClient calls were made
        verify(webClientBuilder, never()).build();
    }

    @Test
    void getPropertiesByIds_ShouldHandleExceptionsGracefully() {
        // Given
        when(responseSpec.bodyToMono(String.class)).thenThrow(new RuntimeException("Test exception"));
        
        List<Integer> propertyIds = Arrays.asList(1, 2);
        
        // When
        List<PropertyResponse> result = graphQLPropertyService.getPropertiesByIds(propertyIds);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getPropertiesByIds_ShouldHandleInvalidResponse() {
        // Given
        String invalidResponse = "{ \"data\": { \"listProperties\": \"not an array\" } }";
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(invalidResponse));
        
        List<Integer> propertyIds = Arrays.asList(1, 2);
        
        // When
        List<PropertyResponse> result = graphQLPropertyService.getPropertiesByIds(propertyIds);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getPropertiesByIds_ShouldHandleNullResponse() {
        // Given
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.empty());
        
        List<Integer> propertyIds = Arrays.asList(1, 2);
        
        // When
        List<PropertyResponse> result = graphQLPropertyService.getPropertiesByIds(propertyIds);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
} 