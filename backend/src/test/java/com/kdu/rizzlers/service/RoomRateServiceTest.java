package com.kdu.rizzlers.service;

import com.kdu.rizzlers.config.CustomTestConfiguration;
import com.kdu.rizzlers.dto.out.DailyRoomRateDTO;
import com.kdu.rizzlers.entity.PropertyPromotionSchedule;
import com.kdu.rizzlers.repository.PropertyPromotionScheduleRepository;
import com.kdu.rizzlers.service.impl.RoomRateServiceImpl;
import org.junit.jupiter.api.BeforeEach;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // Set lenient for the entire test class
public class RoomRateServiceTest {

    @Mock
    private PropertyPromotionScheduleRepository propertyPromotionScheduleRepository;

    @Mock
    private WebClient.Builder webClientBuilder;

    @InjectMocks
    private RoomRateServiceImpl roomRateService;

    private List<PropertyPromotionSchedule> mockPromotions;
    private Integer propertyId;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    public void setup() {
        // Create a partial mock to avoid GraphQL operations
        roomRateService = Mockito.spy(new RoomRateServiceImpl(webClientBuilder, propertyPromotionScheduleRepository));
        
        // Mock the GraphQL-dependent method - with lenient mode already applied at class level
        doReturn(Collections.emptyList()).when((RoomRateServiceImpl)roomRateService)
            .getDailyRatesWithPromotions(any(Integer.class), any(Integer.class));

        propertyId = 100;
        startDate = LocalDate.of(2023, 6, 1);
        endDate = LocalDate.of(2023, 6, 30);

        // Create mock promotions
        mockPromotions = new ArrayList<>();
        
        // Promotion 1: Summer discount (June 1-15)
        PropertyPromotionSchedule summerPromotion = PropertyPromotionSchedule.builder()
                .id(1L)
                .propertyId(propertyId)
                .promotionId(1)
                .startDate(LocalDate.of(2023, 6, 1))
                .endDate(LocalDate.of(2023, 6, 15))
                .priceFactor(BigDecimal.valueOf(0.8)) // 20% discount
                .isActive(true)
                .build();
        
        // Promotion 2: Weekend special (June 16-18)
        PropertyPromotionSchedule weekendPromotion = PropertyPromotionSchedule.builder()
                .id(2L)
                .propertyId(propertyId)
                .promotionId(2)
                .startDate(LocalDate.of(2023, 6, 16))
                .endDate(LocalDate.of(2023, 6, 18))
                .priceFactor(BigDecimal.valueOf(0.85)) // 15% discount
                .isActive(true)
                .build();
        
        // Promotion 3: Last minute (June 25-30)
        PropertyPromotionSchedule lastMinutePromotion = PropertyPromotionSchedule.builder()
                .id(3L)
                .propertyId(propertyId)
                .promotionId(3)
                .startDate(LocalDate.of(2023, 6, 25))
                .endDate(LocalDate.of(2023, 6, 30))
                .priceFactor(BigDecimal.valueOf(0.75)) // 25% discount
                .isActive(true)
                .build();
        
        // Promotion 4: Inactive promotion
        PropertyPromotionSchedule inactivePromotion = PropertyPromotionSchedule.builder()
                .id(4L)
                .propertyId(propertyId)
                .promotionId(4)
                .startDate(LocalDate.of(2023, 6, 10))
                .endDate(LocalDate.of(2023, 6, 20))
                .priceFactor(BigDecimal.valueOf(0.9)) // 10% discount
                .isActive(false)
                .build();
        
        mockPromotions.add(summerPromotion);
        mockPromotions.add(weekendPromotion);
        mockPromotions.add(lastMinutePromotion);
        mockPromotions.add(inactivePromotion);
    }

    @Test
    public void testGetAllPromotions() {
        // Given
        when(propertyPromotionScheduleRepository.findAllByPropertyId(propertyId)).thenReturn(mockPromotions);
        
        // When
        List<PropertyPromotionSchedule> result = roomRateService.getAllPromotions(propertyId);
        
        // Then
        assertNotNull(result);
        assertEquals(4, result.size());
        verify(propertyPromotionScheduleRepository).findAllByPropertyId(propertyId);
    }

    @Test
    public void testGetActivePromotions() {
        // Given
        when(propertyPromotionScheduleRepository.findActivePromotionsForPropertyInPeriod(
                eq(propertyId), eq(startDate), eq(endDate)))
                .thenReturn(mockPromotions.stream()
                        .filter(PropertyPromotionSchedule::getIsActive)
                        .toList());
        
        // When
        List<PropertyPromotionSchedule> result = roomRateService.getActivePromotions(propertyId, startDate, endDate);
        
        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(propertyPromotionScheduleRepository).findActivePromotionsForPropertyInPeriod(
                propertyId, startDate, endDate);
    }

    @Test
    public void testGetActivePromotions_NoPromotionsFound() {
        // Given
        when(propertyPromotionScheduleRepository.findActivePromotionsForPropertyInPeriod(
                eq(propertyId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        
        // When
        List<PropertyPromotionSchedule> result = roomRateService.getActivePromotions(propertyId, startDate, endDate);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(propertyPromotionScheduleRepository).findActivePromotionsForPropertyInPeriod(
                propertyId, startDate, endDate);
    }

    @Test
    void getAllPromotions_ShouldReturnEmptyListWhenNoPromotionsExist() {
        // Given
        when(propertyPromotionScheduleRepository.findAllByPropertyId(anyInt())).thenReturn(new ArrayList<>());

        // When
        List<PropertyPromotionSchedule> result = roomRateService.getAllPromotions(999);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(propertyPromotionScheduleRepository).findAllByPropertyId(999);
    }

    @Test
    void getActivePromotions_ShouldReturnEmptyListWhenNoActivePromotions() {
        // Given
        when(propertyPromotionScheduleRepository.findActivePromotionsForPropertyInPeriod(
                anyInt(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new ArrayList<>());

        // When
        List<PropertyPromotionSchedule> result = roomRateService.getActivePromotions(999, startDate, endDate);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(propertyPromotionScheduleRepository).findActivePromotionsForPropertyInPeriod(999, startDate, endDate);
    }

    @Test
    void getActivePromotions_ShouldHandleNullDates() {
        // Given
        LocalDate nullStartDate = null;
        LocalDate nullEndDate = null;
        
        when(propertyPromotionScheduleRepository.findActivePromotionsForPropertyInPeriod(
                eq(propertyId), eq(nullStartDate), eq(nullEndDate)))
                .thenReturn(new ArrayList<>());

        // When
        List<PropertyPromotionSchedule> result = roomRateService.getActivePromotions(propertyId, nullStartDate, nullEndDate);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(propertyPromotionScheduleRepository).findActivePromotionsForPropertyInPeriod(propertyId, nullStartDate, nullEndDate);
    }

    // Helper method to create PropertyPromotionSchedule objects
    private PropertyPromotionSchedule createPromotion(Long id, Integer propertyId, Integer promotionId, 
                                                     double priceFactor, LocalDate startDate, 
                                                     LocalDate endDate, boolean isActive, LocalDateTime createdAt) {
        PropertyPromotionSchedule promotion = new PropertyPromotionSchedule();
        promotion.setId(id);
        promotion.setPropertyId(propertyId);
        promotion.setPromotionId(promotionId);
        promotion.setPriceFactor(BigDecimal.valueOf(priceFactor));
        promotion.setStartDate(startDate);
        promotion.setEndDate(endDate);
        promotion.setIsActive(isActive);
        promotion.setCreatedAt(createdAt);
        return promotion;
    }
} 