package com.kdu.rizzlers.controller;

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
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    @MockBean
    private RoomRateService roomRateService;

    private PropertyPromotionSchedule promotion1;
    private PropertyPromotionSchedule promotion2;
    private List<PropertyPromotionSchedule> promotionList;
    private List<DailyRoomRateDTO> dailyRates;

    @BeforeEach
    void setUp() {
        LocalDate now = LocalDate.now();
        LocalDate startDate = now;
        LocalDate endDate = now.plusDays(30);

        // Create test promotions
        promotion1 = PropertyPromotionSchedule.builder()
                .id(1L)
                .propertyId(101)
                .promotionId(201)
                .title("Summer Special")
                .description("15% off summer bookings")
                .promoCode("SUMMER15")
                .priceFactor(new BigDecimal("0.85"))
                .startDate(startDate)
                .endDate(endDate)
                .isActive(true)
                .isVisible(true)
                .build();

        promotion2 = PropertyPromotionSchedule.builder()
                .id(2L)
                .propertyId(101)
                .promotionId(202)
                .title("Weekend Deal")
                .description("10% off weekend stays")
                .promoCode("WEEKEND10")
                .priceFactor(new BigDecimal("0.90"))
                .startDate(startDate)
                .endDate(endDate)
                .isActive(true)
                .isVisible(true)
                .build();

        promotionList = Arrays.asList(promotion1, promotion2);

        // Create test daily rates
        DailyRoomRateDTO rate1 = DailyRoomRateDTO.builder()
                .date(now)
                .minimumRate(100.0)
                .hasPromotion(true)
                .promotionId(201)
                .priceFactor(0.85)
                .discountedRate(85.0)
                .build();

        DailyRoomRateDTO rate2 = DailyRoomRateDTO.builder()
                .date(now.plusDays(1))
                .minimumRate(120.0)
                .hasPromotion(true)
                .promotionId(202)
                .priceFactor(0.90)
                .discountedRate(108.0)
                .build();

        dailyRates = Arrays.asList(rate1, rate2);
    }

    @Test
    @DisplayName("Get active promotions should return list of promotions")
    void getActivePromotions_shouldReturnListOfPromotions() throws Exception {
        Integer propertyId = 101;
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(7);

        when(roomRateService.getActivePromotions(propertyId, startDate, endDate))
                .thenReturn(promotionList);

        mockMvc.perform(get("/room-rates/promotions")
                .param("propertyId", propertyId.toString())
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].propertyId", is(101)))
                .andExpect(jsonPath("$[0].title", is("Summer Special")))
                .andExpect(jsonPath("$[0].priceFactor", is(0.85)))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].propertyId", is(101)))
                .andExpect(jsonPath("$[1].title", is("Weekend Deal")))
                .andExpect(jsonPath("$[1].priceFactor", is(0.9)));
    }

    @Test
    @DisplayName("Get active promotions with no promotions should return empty list")
    void getActivePromotions_withNoPromotions_shouldReturnEmptyList() throws Exception {
        Integer propertyId = 102;
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(7);

        when(roomRateService.getActivePromotions(propertyId, startDate, endDate))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/room-rates/promotions")
                .param("propertyId", propertyId.toString())
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Get all promotions should return list of all promotions")
    void getAllPromotions_shouldReturnListOfAllPromotions() throws Exception {
        Integer propertyId = 101;

        when(roomRateService.getAllPromotions(propertyId))
                .thenReturn(promotionList);

        mockMvc.perform(get("/room-rates/all-promotions")
                .param("propertyId", propertyId.toString())
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Summer Special")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].title", is("Weekend Deal")));
    }

    @Test
    @DisplayName("Get daily rates with promotions should return list of daily rates")
    void getDailyRatesWithPromotions_shouldReturnListOfDailyRates() throws Exception {
        Integer tenantId = 1;
        Integer propertyId = 101;

        when(roomRateService.getDailyRatesWithPromotions(tenantId, propertyId))
                .thenReturn(dailyRates);

        mockMvc.perform(get("/room-rates/daily-rates")
                .param("tenantId", tenantId.toString())
                .param("propertyId", propertyId.toString())
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].minimum_rate", is(100.0)))
                .andExpect(jsonPath("$[0].has_promotion", is(true)))
                .andExpect(jsonPath("$[0].promotion_id", is(201)))
                .andExpect(jsonPath("$[0].price_factor", is(0.85)))
                .andExpect(jsonPath("$[0].discounted_rate", is(85.0)))
                .andExpect(jsonPath("$[1].minimum_rate", is(120.0)))
                .andExpect(jsonPath("$[1].has_promotion", is(true)))
                .andExpect(jsonPath("$[1].promotion_id", is(202)));
    }

    @Test
    @DisplayName("Get daily rates with no data should return empty list")
    void getDailyRatesWithPromotions_withNoData_shouldReturnEmptyList() throws Exception {
        Integer tenantId = 1;
        Integer propertyId = 102;

        when(roomRateService.getDailyRatesWithPromotions(tenantId, propertyId))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/room-rates/daily-rates")
                .param("tenantId", tenantId.toString())
                .param("propertyId", propertyId.toString())
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
} 