package com.kdu.rizzlers.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.kdu.rizzlers.dto.out.PropertyResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GraphQLPropertyServiceImplTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private GraphQLPropertyServiceImpl graphQLPropertyService;

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

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(graphQLPropertyService, "graphqlUrl", "http://test.com/graphql");
        ReflectionTestUtils.setField(graphQLPropertyService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(graphQLPropertyService, "apiKeyHeader", "x-api-key");
        ReflectionTestUtils.setField(graphQLPropertyService, "timeout", 5000);
    }

    @Test
    void getPropertiesByIds_withEmptyIdList_shouldReturnEmptyList() {
        // Act - without any mocking setup needed
        List<PropertyResponse> result = graphQLPropertyService.getPropertiesByIds(Collections.emptyList());

        // Assert
        assertTrue(result.isEmpty());
        // No need to verify webClientBuilder as it shouldn't be called
    }

    @Test
    void getPropertiesByIds_withNullIdList_shouldReturnEmptyList() {
        // Act - without any mocking setup needed
        List<PropertyResponse> result = graphQLPropertyService.getPropertiesByIds(null);

        // Assert
        assertTrue(result.isEmpty());
        // No need to verify webClientBuilder as it shouldn't be called
    }

    @Test
    void getPropertiesByIds_whenGraphQLReturnsError_shouldHandleAndReturnEmptyList() {
        // Arrange
        List<Integer> propertyIds = Arrays.asList(1, 2);
        
        // Setup basic WebClient chain
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        
        // Simulate error
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.error(new RuntimeException("GraphQL error")));

        // Act
        List<PropertyResponse> result = graphQLPropertyService.getPropertiesByIds(propertyIds);

        // Assert
        assertTrue(result.isEmpty());
        verify(webClientBuilder).build();
    }
} 