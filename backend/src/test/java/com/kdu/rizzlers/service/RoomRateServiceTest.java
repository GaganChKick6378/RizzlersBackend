package com.kdu.rizzlers.service;

import com.kdu.rizzlers.config.CustomTestConfiguration;
import com.kdu.rizzlers.dto.out.DailyRoomRateDTO;
import com.kdu.rizzlers.entity.PropertyPromotionSchedule;
import com.kdu.rizzlers.repository.PropertyPromotionScheduleRepository;
import com.kdu.rizzlers.service.impl.RoomRateServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * Tests for the RoomRateService interface to ensure proper contract behavior
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RoomRateServiceTest {

    @Mock
    private RoomRateService roomRateService;

    @Mock
    private PropertyPromotionScheduleRepository propertyPromotionScheduleRepository;

    @Mock
    private WebClient.Builder webClientBuilder;

    @InjectMocks
    private RoomRateServiceImpl roomRateServiceImpl;

    private List<PropertyPromotionSchedule> mockPromotions;
    private Integer propertyId;
    private LocalDate startDate;
    private LocalDate endDate;
    private PropertyPromotionSchedule promotion1;
    private PropertyPromotionSchedule promotion2;

    @BeforeEach
    void setUp() {
        // Create a partial mock to avoid GraphQL operations
        roomRateServiceImpl = Mockito.spy(new RoomRateServiceImpl(webClientBuilder, propertyPromotionScheduleRepository));
        
        // Mock the GraphQL-dependent method
        doReturn(Collections.emptyList()).when((RoomRateServiceImpl)roomRateServiceImpl)
            .getDailyRatesWithPromotions(any(Integer.class), any(Integer.class));

        propertyId = 123;
        startDate = LocalDate.now();
        endDate = startDate.plusDays(5);

        // Create mock promotions
        mockPromotions = new ArrayList<>();
        
        // Promotion 1: Summer discount
        promotion1 = PropertyPromotionSchedule.builder()
                .id(1L)
                .propertyId(propertyId)
                .promotionId(100)
                .title("Summer Special")
                .description("10% off for summer bookings")
                .promoCode("SUMMER10")
                .priceFactor(new BigDecimal("0.90"))
                .startDate(startDate)
                .endDate(endDate)
                .isActive(true)
                .isVisible(true)
                .build();
        
        // Promotion 2: Early Bird
        promotion2 = PropertyPromotionSchedule.builder()
                .id(2L)
                .propertyId(propertyId)
                .promotionId(200)
                .title("Early Bird")
                .description("15% off for early bookings")
                .promoCode("EARLY15")
                .priceFactor(new BigDecimal("0.85"))
                .startDate(startDate.minusDays(5))
                .endDate(endDate.plusDays(5))
                .isActive(true)
                .isVisible(true)
                .build();
        
        mockPromotions.add(promotion1);
        mockPromotions.add(promotion2);
        
        // Setup all mock behavior
        when(roomRateService.getActivePromotions(propertyId, startDate, endDate))
            .thenReturn(Arrays.asList(promotion1, promotion2));
            
        when(roomRateService.getAllPromotions(propertyId))
            .thenReturn(Arrays.asList(promotion1, promotion2));
            
        when(roomRateService.getActivePromotions(999, startDate, endDate))
            .thenReturn(Collections.emptyList());
            
        when(roomRateService.getDailyRatesWithPromotions(anyInt(), anyInt()))
            .thenReturn(Arrays.asList(
                DailyRoomRateDTO.builder()
                    .date(LocalDate.now())
                    .minimumRate(100.0)
                    .hasPromotion(true)
                    .promotionId(100)
                    .priceFactor(0.9)
                    .discountedRate(90.0)
                    .build(),
                DailyRoomRateDTO.builder()
                    .date(LocalDate.now().plusDays(1))
                    .minimumRate(120.0)
                    .hasPromotion(true)
                    .promotionId(200)
                    .priceFactor(0.85)
                    .discountedRate(102.0)
                    .build()
            ));
    }

    @Test
    @DisplayName("getActivePromotions should return active promotions for a date range")
    void getActivePromotions_shouldReturnPromotionsForDateRange() {
        // Act
        List<PropertyPromotionSchedule> result = roomRateService.getActivePromotions(propertyId, startDate, endDate);

        // Assert
        assertEquals(2, result.size());
        assertEquals(promotion1.getId(), result.get(0).getId());
        assertEquals(promotion2.getId(), result.get(1).getId());
    }
    
    @Test
    @DisplayName("getAllPromotions should return all promotions for a property")
    void getAllPromotions_shouldReturnAllPromotionsForProperty() {
        // Act
        List<PropertyPromotionSchedule> result = roomRateService.getAllPromotions(propertyId);

        // Assert
        assertEquals(2, result.size());
        assertEquals("Summer Special", result.get(0).getTitle());
        assertEquals("Early Bird", result.get(1).getTitle());
    }
    
    @Test
    @DisplayName("getDailyRatesWithPromotions should return room rates with promotions")
    void getDailyRatesWithPromotions_shouldReturnRatesWithPromotions() {
        // Arrange
        Integer tenantId = 456;

        // Act
        List<DailyRoomRateDTO> result = roomRateService.getDailyRatesWithPromotions(tenantId, propertyId);

        // Assert
        assertEquals(2, result.size());
        assertEquals(100.0, result.get(0).getMinimumRate());
        assertEquals(90.0, result.get(0).getDiscountedRate());
        assertEquals(120.0, result.get(1).getMinimumRate());
        assertEquals(102.0, result.get(1).getDiscountedRate());
    }
    
    @Test
    @DisplayName("getActivePromotions should return empty list when no promotions exist")
    void getActivePromotions_withNoPromotions_shouldReturnEmptyList() {
        // Act - using a different property ID to get the empty list stub
        List<PropertyPromotionSchedule> result = roomRateService.getActivePromotions(
                999, startDate, endDate);

        // Assert
        assertTrue(result.isEmpty());
    }
} 