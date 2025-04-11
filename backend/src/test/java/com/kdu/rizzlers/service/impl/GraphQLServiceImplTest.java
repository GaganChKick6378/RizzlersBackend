package com.kdu.rizzlers.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GraphQLServiceImplTest {

    @Mock
    private WebClient.Builder webClientBuilder;
    
    @Mock
    private WebClient webClient;
    
    private GraphQLServiceImpl graphQLService;
    private GraphQLServiceImpl spyGraphQLService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String graphqlEndpoint = "https://api.example.com/graphql";
    private final String apiKey = "test-api-key";
    private final String apiKeyHeader = "X-Api-Key";

    @BeforeEach
    void setUp() {
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.defaultHeader(anyString(), anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        
        graphQLService = new GraphQLServiceImpl(
                webClientBuilder,
                objectMapper,
                graphqlEndpoint,
                apiKey,
                apiKeyHeader
        );
        
        spyGraphQLService = spy(graphQLService);
    }

    @Test
    void constructor_ShouldConfigureWebClient() {
        // Verify that the WebClient is built with the correct configuration
        verify(webClientBuilder).baseUrl(graphqlEndpoint);
        verify(webClientBuilder).defaultHeader(apiKeyHeader, apiKey);
        verify(webClientBuilder).build();
    }

    @Test
    void executeQuery_WithoutVariables_ShouldCallOverloadedMethod() {
        // Arrange
        String query = "query { test }";
        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put("data", Collections.singletonMap("test", "value"));
        
        doReturn(expectedResult).when(spyGraphQLService).executeQuery(eq(query), any());
        
        // Act
        Map<String, Object> result = spyGraphQLService.executeQuery(query);
        
        // Assert
        assertEquals(expectedResult, result);
        verify(spyGraphQLService).executeQuery(eq(query), eq(Collections.emptyMap()));
    }

    @Test
    void executeQuery_WithVariables_ShouldReturnEmptyMapOnException() {
        // Arrange
        String query = "query ($id: ID!) { item(id: $id) { name } }";
        Map<String, Object> variables = Collections.singletonMap("id", "123");
        
        // We'll make the actual WebClient unavailable to cause an exception
        doReturn(null).when(webClientBuilder).build();
        
        // Act
        Map<String, Object> result = graphQLService.executeQuery(query, variables);
        
        // Assert
        assertTrue(result.isEmpty());
    }
    
    @Test
    void executeQuery_WithVariables_ShouldReturnResponseWithParameterizedTypeReference() {
        // Arrange
        String query = "query ($id: ID!) { item(id: $id) { name } }";
        Map<String, Object> variables = Collections.singletonMap("id", "123");
        Map<String, Object> expectedResponse = new HashMap<>();
        expectedResponse.put("data", Collections.singletonMap("item", Collections.singletonMap("name", "Test Item")));
        
        // Test using a simplified approach with a spy
        // We'll mock the complete chain using doReturn which works better with complex types
        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);
        
        doReturn(requestBodyUriSpec).when(webClient).post();
        doReturn(requestBodySpec).when(requestBodyUriSpec).contentType(any(MediaType.class));
        doReturn(requestBodySpec).when(requestBodySpec).bodyValue(any());
        doReturn(responseSpec).when(requestBodySpec).retrieve();
        doReturn(Mono.just(expectedResponse)).when(responseSpec).bodyToMono(any(org.springframework.core.ParameterizedTypeReference.class));
        
        // Act
        Map<String, Object> result = graphQLService.executeQuery(query, variables);
        
        // Assert
        assertEquals(expectedResponse, result);
        
        // Verify the complete chain was called with the expected parameters
        verify(webClient).post();
        verify(requestBodyUriSpec).contentType(MediaType.APPLICATION_JSON);
        verify(requestBodySpec).bodyValue(argThat(body -> {
            if (!(body instanceof Map)) return false;
            Map<String, Object> requestBody = (Map<String, Object>) body;
            return query.equals(requestBody.get("query")) && variables.equals(requestBody.get("variables"));
        }));
        verify(requestBodySpec).retrieve();
        verify(responseSpec).bodyToMono(any(org.springframework.core.ParameterizedTypeReference.class));
    }
} 