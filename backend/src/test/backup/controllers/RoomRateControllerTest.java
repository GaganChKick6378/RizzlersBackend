package com.kdu.rizzlers.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kdu.rizzlers.dto.out.DailyRoomRateDTO;
import com.kdu.rizzlers.entity.PropertyPromotionSchedule;
import com.kdu.rizzlers.service.RoomRateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(RoomRateController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoomRateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RoomRateService roomRateService;

    private PropertyPromotionSchedule promotion1;
    private PropertyPromotionSchedule promotion2;
    private List<PropertyPromotionSchedule> promotions;
    private List<DailyRoomRateDTO> dailyRates;
    private Integer propertyId;
    private Integer tenantId;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        propertyId = 1;
        tenantId = 100;
        startDate = LocalDate.now();
        endDate = LocalDate.now().plusDays(3);

        // Create sample promotions
        promotion1 = new PropertyPromotionSchedule();
        promotion1.setId(1L);
        promotion1.setPropertyId(propertyId);
        promotion1.setPromotionId(10);
        promotion1.setTitle("Weekend Special");
        promotion1.setDescription("10% off on weekends");
        promotion1.setPriceFactor(0.9);
        promotion1.setStartDate(LocalDate.now().minusDays(5));
        promotion1.setEndDate(LocalDate.now().plusDays(30));

        promotion2 = new PropertyPromotionSchedule();
        promotion2.setId(2L);
        promotion2.setPropertyId(propertyId);
        promotion2.setPromotionId(20);
        promotion2.setTitle("Summer Discount");
        promotion2.setDescription("15% off for summer bookings");
        promotion2.setPriceFactor(0.85);
        promotion2.setStartDate(LocalDate.now().minusDays(10));
        promotion2.setEndDate(LocalDate.now().plusDays(60));

        promotions = Arrays.asList(promotion1, promotion2);

        // Create sample daily rates
        DailyRoomRateDTO rate1 = new DailyRoomRateDTO();
        rate1.setPropertyId(propertyId);
        rate1.setRoomTypeId(101);
        rate1.setRoomTypeName("Deluxe Room");
        rate1.setRateDate(LocalDate.now());
        rate1.setBaseRate(150.0);
        rate1.setFinalRate(135.0);
        rate1.setAppliedPromotionId(10);
        rate1.setAppliedPromotionTitle("Weekend Special");
        rate1.setDiscountPercentage(10.0);

        DailyRoomRateDTO rate2 = new DailyRoomRateDTO();
        rate2.setPropertyId(propertyId);
        rate2.setRoomTypeId(102);
        rate2.setRoomTypeName("Suite");
        rate2.setRateDate(LocalDate.now().plusDays(1));
        rate2.setBaseRate(250.0);
        rate2.setFinalRate(212.5);
        rate2.setAppliedPromotionId(20);
        rate2.setAppliedPromotionTitle("Summer Discount");
        rate2.setDiscountPercentage(15.0);

        dailyRates = Arrays.asList(rate1, rate2);
    }

    @Test
    @DisplayName("Should get active promotions")
    void getActivePromotions_shouldReturnActivePromotions() throws Exception {
        // Arrange
        when(roomRateService.getActivePromotions(anyInt(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(promotions);

        // Act & Assert
        mockMvc.perform(get("/room-rates/promotions")
                .with(csrf())
                .param("propertyId", propertyId.toString())
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].propertyId", is(propertyId)))
                .andExpect(jsonPath("$[0].promotionId", is(10)))
                .andExpect(jsonPath("$[0].title", is("Weekend Special")))
                .andExpect(jsonPath("$[0].priceFactor", is(0.9)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].propertyId", is(propertyId)))
                .andExpect(jsonPath("$[1].title", is("Summer Discount")));
    }

    @Test
    @DisplayName("Should get all promotions for a property")
    void getAllPromotions_shouldReturnAllPromotions() throws Exception {
        // Arrange
        when(roomRateService.getAllPromotions(anyInt())).thenReturn(promotions);

        // Act & Assert
        mockMvc.perform(get("/room-rates/all-promotions")
                .with(csrf())
                .param("propertyId", propertyId.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].propertyId", is(propertyId)))
                .andExpect(jsonPath("$[0].title", is("Weekend Special")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].propertyId", is(propertyId)))
                .andExpect(jsonPath("$[1].title", is("Summer Discount")));
    }

    @Test
    @DisplayName("Should get daily rates with promotions")
    void getDailyRatesWithPromotions_shouldReturnDailyRates() throws Exception {
        // Arrange
        when(roomRateService.getDailyRatesWithPromotions(anyInt(), anyInt())).thenReturn(dailyRates);

        // Act & Assert
        mockMvc.perform(get("/room-rates/daily-rates")
                .with(csrf())
                .param("tenantId", tenantId.toString())
                .param("propertyId", propertyId.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].propertyId", is(propertyId)))
                .andExpect(jsonPath("$[0].roomTypeId", is(101)))
                .andExpect(jsonPath("$[0].roomTypeName", is("Deluxe Room")))
                .andExpect(jsonPath("$[0].baseRate", is(150.0)))
                .andExpect(jsonPath("$[0].finalRate", is(135.0)))
                .andExpect(jsonPath("$[0].appliedPromotionId", is(10)))
                .andExpect(jsonPath("$[0].appliedPromotionTitle", is("Weekend Special")))
                .andExpect(jsonPath("$[0].discountPercentage", is(10.0)))
                .andExpect(jsonPath("$[1].propertyId", is(propertyId)))
                .andExpect(jsonPath("$[1].roomTypeId", is(102)))
                .andExpect(jsonPath("$[1].roomTypeName", is("Suite")));
    }

    @Test
    @DisplayName("Should handle empty promotions list")
    void getActivePromotions_whenNoPromotions_shouldReturnEmptyList() throws Exception {
        // Arrange
        when(roomRateService.getActivePromotions(anyInt(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/room-rates/promotions")
                .with(csrf())
                .param("propertyId", propertyId.toString())
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should handle empty daily rates list")
    void getDailyRatesWithPromotions_whenNoRates_shouldReturnEmptyList() throws Exception {
        // Arrange
        when(roomRateService.getDailyRatesWithPromotions(anyInt(), anyInt()))
                .thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/room-rates/daily-rates")
                .with(csrf())
                .param("tenantId", tenantId.toString())
                .param("propertyId", propertyId.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
} 