package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.out.PromotionDTO;
import com.kdu.rizzlers.service.impl.PromotionGraphQLServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromotionGraphQLServiceTest {

    @Mock
    private WebClient webClient;
    
    @Mock
    private HttpGraphQlClient graphQlClient;

    @InjectMocks
    private PromotionGraphQLServiceImpl promotionGraphQLService;

    @BeforeEach
    void setUp() {
        // Set up required fields using ReflectionTestUtils
        ReflectionTestUtils.setField(promotionGraphQLService, "graphqlEndpoint", "http://test.com/graphql");
        ReflectionTestUtils.setField(promotionGraphQLService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(promotionGraphQLService, "apiKeyHeader", "x-api-key");
        ReflectionTestUtils.setField(promotionGraphQLService, "webClient", webClient);
        ReflectionTestUtils.setField(promotionGraphQLService, "graphQlClient", graphQlClient);
    }

    @Test
    @DisplayName("PromotionGraphQLService interface is correctly implemented")
    void testInterface() {
        // This test verifies that PromotionGraphQLServiceImpl implements PromotionGraphQLService
        PromotionGraphQLService service = promotionGraphQLService;
        assertNotNull(service);
    }
    
    @Test
    @DisplayName("fetchAllPromotions should return empty list when GraphQL client throws an exception")
    void fetchAllPromotions_whenGraphQLClientThrowsException_shouldReturnEmptyList() {
        // Set up mock to throw an exception
        when(graphQlClient.document(anyString())).thenThrow(new RuntimeException("Test exception"));
        
        // Execute method under test
        List<PromotionDTO> result = promotionGraphQLService.fetchAllPromotions();
        
        // Verify result
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(graphQlClient).document(anyString());
    }
} 