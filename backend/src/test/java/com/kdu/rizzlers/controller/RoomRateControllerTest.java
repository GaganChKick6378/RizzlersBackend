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
    private List<DailyRoomRateDTO> dailyRates;

    @BeforeEach
    void setup() {
        // Setup test promotions
        promotion1 = PropertyPromotionSchedule.builder()
                .id(1L)
                .propertyId(100)
                .promotionId(200)
                .title("Summer Special")
                .description("20% off for summer bookings")
                .promoCode("SUMMER20")
                .priceFactor(new BigDecimal("0.80"))
                .startDate(LocalDate.now().minusDays(10))
                .endDate(LocalDate.now().plusDays(30))
                .isActive(true)
                .isVisible(true)
                .build();

        promotion2 = PropertyPromotionSchedule.builder()
                .id(2L)
                .propertyId(100)
                .promotionId(201)
                .title("Early Bird")
                .description("15% off for early bookings")
                .promoCode("EARLY15")
                .priceFactor(new BigDecimal("0.85"))
                .startDate(LocalDate.now().minusDays(5))
                .endDate(LocalDate.now().plusDays(20))
                .isActive(true)
                .isVisible(true)
                .build();

        // Setup daily rates
        dailyRates = Arrays.asList(
            DailyRoomRateDTO.builder()
                .date(LocalDate.now())
                .minimumRate(100.0)
                .hasPromotion(true)
                .promotionId(200)
                .priceFactor(0.8)
                .discountedRate(80.0)
                .build(),
            DailyRoomRateDTO.builder()
                .date(LocalDate.now().plusDays(1))
                .minimumRate(120.0)
                .hasPromotion(true)
                .promotionId(201)
                .priceFactor(0.85)
                .discountedRate(102.0)
                .build()
        );
    }

    @Test
    @DisplayName("Should return active promotions for a date range")
    void getActivePromotions_shouldReturnPromotionsForDateRange() throws Exception {
        // Arrange
        when(roomRateService.getActivePromotions(anyInt(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList(promotion1, promotion2));

        // Act & Assert
        mockMvc.perform(get("/room-rates/promotions")
                .with(csrf())
                .param("propertyId", "100")
                .param("startDate", LocalDate.now().toString())
                .param("endDate", LocalDate.now().plusDays(10).toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].propertyId", is(100)))
                .andExpect(jsonPath("$[0].title", is("Summer Special")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].title", is("Early Bird")));
    }

    @Test
    @DisplayName("Should return empty list when no active promotions exist")
    void getActivePromotions_withNoActivePromotions_shouldReturnEmptyList() throws Exception {
        // Arrange
        when(roomRateService.getActivePromotions(anyInt(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/room-rates/promotions")
                .with(csrf())
                .param("propertyId", "100")
                .param("startDate", LocalDate.now().toString())
                .param("endDate", LocalDate.now().plusDays(10).toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should return all promotions for a property")
    void getAllPromotions_shouldReturnAllPromotions() throws Exception {
        // Arrange
        when(roomRateService.getAllPromotions(anyInt()))
                .thenReturn(Arrays.asList(promotion1, promotion2));

        // Act & Assert
        mockMvc.perform(get("/room-rates/all-promotions")
                .with(csrf())
                .param("propertyId", "100")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].propertyId", is(100)))
                .andExpect(jsonPath("$[0].title", is("Summer Special")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].title", is("Early Bird")));
    }

    @Test
    @DisplayName("Should return empty list when no promotions exist")
    void getAllPromotions_withNoPromotions_shouldReturnEmptyList() throws Exception {
        // Arrange
        when(roomRateService.getAllPromotions(anyInt()))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/room-rates/all-promotions")
                .with(csrf())
                .param("propertyId", "100")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should return daily rates with promotions")
    void getDailyRatesWithPromotions_shouldReturnDailyRates() throws Exception {
        // Arrange
        when(roomRateService.getDailyRatesWithPromotions(anyInt(), anyInt()))
                .thenReturn(dailyRates);

        // Act & Assert
        mockMvc.perform(get("/room-rates/daily-rates")
                .with(csrf())
                .param("tenantId", "1")
                .param("propertyId", "100")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].minimum_rate", is(100.0)))
                .andExpect(jsonPath("$[0].has_promotion", is(true)))
                .andExpect(jsonPath("$[0].promotion_id", is(200)))
                .andExpect(jsonPath("$[0].price_factor", is(0.8)))
                .andExpect(jsonPath("$[0].discounted_rate", is(80.0)))
                .andExpect(jsonPath("$[1].minimum_rate", is(120.0)))
                .andExpect(jsonPath("$[1].promotion_id", is(201)));
    }

    @Test
    @DisplayName("Should return empty list when no daily rates exist")
    void getDailyRatesWithPromotions_withNoRates_shouldReturnEmptyList() throws Exception {
        // Arrange
        when(roomRateService.getDailyRatesWithPromotions(anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/room-rates/daily-rates")
                .with(csrf())
                .param("tenantId", "1")
                .param("propertyId", "100")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
} 