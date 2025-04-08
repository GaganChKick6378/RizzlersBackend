package com.kdu.rizzlers.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromotionGraphQLServiceImplTest {

    private WebClient webClientMock;
    private HttpGraphQlClient graphQlClientMock;
    private PromotionGraphQLServiceImpl promotionGraphQLService;
    
    private HttpGraphQlClient.RequestSpec requestSpecMock;
    private HttpGraphQlClient.RetrieveSpec retrieveSpecMock;

    @BeforeEach
    void setUp() {
        // Create mocks
        webClientMock = mock(WebClient.class);
        graphQlClientMock = mock(HttpGraphQlClient.class);
        requestSpecMock = mock(HttpGraphQlClient.RequestSpec.class);
        retrieveSpecMock = mock(HttpGraphQlClient.RetrieveSpec.class);
        
        // Create service with constructor parameters
        promotionGraphQLService = new PromotionGraphQLServiceImpl(
                "https://api.example.com/graphql",
                "test-api-key",
                "X-Api-Key");
        
        // Inject mocks
        ReflectionTestUtils.setField(promotionGraphQLService, "webClient", webClientMock);
        ReflectionTestUtils.setField(promotionGraphQLService, "graphQlClient", graphQlClientMock);
    }

    @Test
    void fetchPromotion_ShouldUseParameterizedTypeReference() {
        // Arrange
        Integer promotionId = 123;
        
        // Create mock response data
        Map<String, Object> promotionData = new HashMap<>();
        promotionData.put("price_factor", 0.9);
        promotionData.put("promotion_title", "Summer Special");
        promotionData.put("promotion_description", "20% off summer bookings");
        
        // Setup the mock chain
        when(graphQlClientMock.document(anyString())).thenReturn(requestSpecMock);
        when(requestSpecMock.variables(any())).thenReturn(requestSpecMock);
        when(requestSpecMock.retrieve(anyString())).thenReturn(retrieveSpecMock);
        
        // Use ArgumentCaptor to verify the ParameterizedTypeReference usage
        ArgumentCaptor<ParameterizedTypeReference<?>> typeCaptor = ArgumentCaptor.forClass(ParameterizedTypeReference.class);
        
        // Use answer() to dynamically create a response for any toEntity call
        when(retrieveSpecMock.toEntity(typeCaptor.capture())).thenAnswer(inv -> Mono.just(promotionData));
        
        // Act
        Map<String, Object> result = promotionGraphQLService.fetchPromotion(promotionId);
        
        // Assert
        assertNotNull(result);
        assertEquals(promotionData, result);
        
        // Verify the document call with correct query
        verify(graphQlClientMock).document(contains("getPromotion(where: {promotion_id: $promotionId})"));
        
        // Verify variables passed correctly
        verify(requestSpecMock).variables(argThat(vars -> 
            vars instanceof Map && ((Map) vars).get("promotionId").equals(promotionId)
        ));
        
        // Verify that ParameterizedTypeReference was captured
        assertNotNull(typeCaptor.getValue());
        assertEquals("java.util.Map<java.lang.String, java.lang.Object>", typeCaptor.getValue().getType().toString());
        
        // Verify the retrieve call was made with the correct path
        verify(requestSpecMock).retrieve("getPromotion");
    }
    
    @Test
    void fetchPromotion_ShouldHandleError() {
        // Arrange
        Integer promotionId = 123;
        
        // Setup the mock chain with error scenario
        when(graphQlClientMock.document(anyString())).thenReturn(requestSpecMock);
        when(requestSpecMock.variables(any())).thenReturn(requestSpecMock);
        when(requestSpecMock.retrieve(anyString())).thenReturn(retrieveSpecMock);
        
        // Use answer() to handle the error case
        when(retrieveSpecMock.toEntity(any(ParameterizedTypeReference.class)))
            .thenAnswer(inv -> Mono.error(new RuntimeException("GraphQL error")));
        
        // Act
        Map<String, Object> result = promotionGraphQLService.fetchPromotion(promotionId);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        // Verify that ParameterizedTypeReference was used
        verify(retrieveSpecMock).toEntity(any(ParameterizedTypeReference.class));
    }
} 