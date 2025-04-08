package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.dto.out.DailyRoomRateDTO;
import com.kdu.rizzlers.entity.PropertyPromotionSchedule;
import com.kdu.rizzlers.repository.PropertyPromotionScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomRateServiceImplTest {

    @Mock
    private WebClient.Builder webClientBuilder;
    
    @Mock
    private WebClient webClient;
    
    @Mock
    private PropertyPromotionScheduleRepository promotionRepository;
    
    private RoomRateServiceImpl roomRateService;

    @BeforeEach
    void setUp() {
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.defaultHeader(anyString(), anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);
        
        roomRateService = spy(new RoomRateServiceImpl(webClientBuilder, promotionRepository));
        
        // Set private fields
        ReflectionTestUtils.setField(roomRateService, "graphqlEndpoint", "https://api.example.com/graphql");
        ReflectionTestUtils.setField(roomRateService, "apiKeyHeader", "X-Api-Key");
        ReflectionTestUtils.setField(roomRateService, "apiKey", "test-api-key");
    }

    @Test
    void getDailyRatesWithPromotions_ShouldCalculateDiscountedRates() {
        // Given
        Integer tenantId = 1;
        Integer propertyId = 100;
        
        // Create a test map of rates to be returned
        Map<LocalDate, Double> mockRates = new TreeMap<>();
        mockRates.put(LocalDate.parse("2023-04-15"), 100.0);
        
        // Avoid calling private method directly by intercepting at the method level
        // This uses PowerMockito's approach of "when a method is called, do something instead"
        doReturn(mockRates).when(roomRateService).getDailyRatesWithPromotions(anyInt(), anyInt());
        
        // Mock the actual method call to return our prepared result
        doCallRealMethod().when(roomRateService).getDailyRatesWithPromotions(eq(tenantId), eq(propertyId));
        
        // Mock promotions
        List<PropertyPromotionSchedule> promotions = new ArrayList<>();
        PropertyPromotionSchedule promotion = mock(PropertyPromotionSchedule.class);
        when(promotion.isDateInPromotionPeriod(any(LocalDate.class))).thenReturn(true);
        when(promotion.getPromotionId()).thenReturn(500);
        when(promotion.getPriceFactor()).thenReturn(new BigDecimal("0.8"));
        promotions.add(promotion);
        
        when(promotionRepository.findByPropertyIdAndIsVisibleTrue(propertyId)).thenReturn(promotions);
        
        // Act
        List<DailyRoomRateDTO> result = roomRateService.getDailyRatesWithPromotions(tenantId, propertyId);
        
        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        
        DailyRoomRateDTO dto = result.get(0);
        assertEquals(LocalDate.parse("2023-04-15"), dto.getDate());
        assertEquals(100.0, dto.getMinimumRate());
        assertTrue(dto.getHasPromotion());
        assertEquals(500, dto.getPromotionId());
        assertEquals(0.8, dto.getPriceFactor());
        assertEquals(80.0, dto.getDiscountedRate());
    }
} 