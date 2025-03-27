package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.dto.out.DailyRoomRateDTO;
import com.kdu.rizzlers.entity.PropertyPromotionSchedule;
import com.kdu.rizzlers.service.RoomRateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class RoomRateControllerTest {

    @Mock
    private RoomRateService roomRateService;
    
    @InjectMocks
    private RoomRateController roomRateController;
    
    private MockMvc mockMvc;
    
    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(roomRateController).build();
    }
    
    @Test
    public void testGetActivePromotions() throws Exception {
        // Given
        Integer propertyId = 1;
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(5);
        String formattedStartDate = startDate.format(DateTimeFormatter.ISO_DATE);
        String formattedEndDate = endDate.format(DateTimeFormatter.ISO_DATE);
        
        PropertyPromotionSchedule promotion = new PropertyPromotionSchedule();
        promotion.setId(1L);
        promotion.setPropertyId(propertyId);
        promotion.setPromotionId(101);
        promotion.setPriceFactor(BigDecimal.valueOf(0.8));
        promotion.setStartDate(startDate.minusDays(2));
        promotion.setEndDate(endDate.plusDays(2));
        promotion.setIsActive(true);
        
        when(roomRateService.getActivePromotions(eq(propertyId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(promotion));
        
        // When & Then
        mockMvc.perform(get("/room-rates/promotions")
                .param("propertyId", propertyId.toString())
                .param("startDate", formattedStartDate)
                .param("endDate", formattedEndDate)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].propertyId", is(propertyId)))
                .andExpect(jsonPath("$[0].promotionId", is(101)))
                .andExpect(jsonPath("$[0].priceFactor", is(0.8)));
    }
    
    @Test
    public void testGetAllPromotions() throws Exception {
        // Given
        Integer propertyId = 1;
        
        PropertyPromotionSchedule promotion1 = new PropertyPromotionSchedule();
        promotion1.setId(1L);
        promotion1.setPropertyId(propertyId);
        promotion1.setPromotionId(101);
        promotion1.setPriceFactor(BigDecimal.valueOf(0.8));
        promotion1.setStartDate(LocalDate.now().minusDays(5));
        promotion1.setEndDate(LocalDate.now().plusDays(5));
        promotion1.setIsActive(true);
        
        PropertyPromotionSchedule promotion2 = new PropertyPromotionSchedule();
        promotion2.setId(2L);
        promotion2.setPropertyId(propertyId);
        promotion2.setPromotionId(102);
        promotion2.setPriceFactor(BigDecimal.valueOf(0.9));
        promotion2.setStartDate(LocalDate.now().plusDays(10));
        promotion2.setEndDate(LocalDate.now().plusDays(20));
        promotion2.setIsActive(true);
        
        when(roomRateService.getAllPromotions(propertyId))
                .thenReturn(Arrays.asList(promotion1, promotion2));
        
        // When & Then
        mockMvc.perform(get("/room-rates/all-promotions")
                .param("propertyId", propertyId.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].propertyId", is(propertyId)))
                .andExpect(jsonPath("$[0].promotionId", is(101)))
                .andExpect(jsonPath("$[1].propertyId", is(propertyId)))
                .andExpect(jsonPath("$[1].promotionId", is(102)));
    }
    
    @Test
    public void testGetDailyRatesWithPromotions() throws Exception {
        // Given
        Integer tenantId = 1;
        Integer propertyId = 1;
        
        DailyRoomRateDTO dto1 = DailyRoomRateDTO.builder()
                .date(LocalDate.now())
                .minimumRate(100.0)
                .hasPromotion(true)
                .promotionId(101)
                .priceFactor(0.8)
                .discountedRate(80.0)
                .build();
        
        DailyRoomRateDTO dto2 = DailyRoomRateDTO.builder()
                .date(LocalDate.now().plusDays(1))
                .minimumRate(110.0)
                .hasPromotion(false)
                .priceFactor(1.0)
                .discountedRate(110.0)
                .build();
        
        when(roomRateService.getDailyRatesWithPromotions(tenantId, propertyId))
                .thenReturn(Arrays.asList(dto1, dto2));
        
        // When & Then
        mockMvc.perform(get("/room-rates/daily-rates")
                .param("tenantId", tenantId.toString())
                .param("propertyId", propertyId.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].minimum_rate", is(100.0)))
                .andExpect(jsonPath("$[0].has_promotion", is(true)))
                .andExpect(jsonPath("$[0].promotion_id", is(101)))
                .andExpect(jsonPath("$[0].price_factor", is(0.8)))
                .andExpect(jsonPath("$[0].discounted_rate", is(80.0)))
                .andExpect(jsonPath("$[1].minimum_rate", is(110.0)))
                .andExpect(jsonPath("$[1].has_promotion", is(false)))
                .andExpect(jsonPath("$[1].price_factor", is(1.0)))
                .andExpect(jsonPath("$[1].discounted_rate", is(110.0)));
    }
} 