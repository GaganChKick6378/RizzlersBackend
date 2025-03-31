package com.kdu.rizzlers.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kdu.rizzlers.dto.in.PromoCodeValidateRequest;
import com.kdu.rizzlers.dto.out.PromoCodeResponse;
import com.kdu.rizzlers.dto.out.PropertyPromotionScheduleResponse;
import com.kdu.rizzlers.service.PromoCodeService;
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
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(PromoCodeController.class)
@AutoConfigureMockMvc(addFilters = false)
class PromoCodeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PromoCodeService promoCodeService;

    private PropertyPromotionScheduleResponse validPromotion1;
    private PropertyPromotionScheduleResponse validPromotion2;
    private List<PropertyPromotionScheduleResponse> visiblePromotions;
    private PromoCodeValidateRequest validRequest;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(30);

        // Create valid promotions using builder pattern
        validPromotion1 = PropertyPromotionScheduleResponse.builder()
                .id(1L)
                .propertyId(101)
                .promotionId(201)
                .title("Summer Discount")
                .description("Get 15% off on summer bookings")
                .promoCode("SUMMER15")
                .priceFactor(new BigDecimal("0.85"))
                .startDate(startDate)
                .endDate(endDate)
                .isActive(true)
                .isVisible(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        validPromotion2 = PropertyPromotionScheduleResponse.builder()
                .id(2L)
                .propertyId(102)
                .promotionId(202)
                .title("Weekend Special")
                .description("10% off on weekend stays")
                .promoCode("WEEKEND10")
                .priceFactor(new BigDecimal("0.90"))
                .startDate(startDate)
                .endDate(endDate)
                .isActive(true)
                .isVisible(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        visiblePromotions = Arrays.asList(validPromotion1, validPromotion2);

        // Create valid promo code validation request
        validRequest = PromoCodeValidateRequest.builder()
                .promoCode("SUMMER15")
                .build();
    }

    @Test
    @DisplayName("Get visible promotions should return list of promotions")
    void getVisiblePromotions_shouldReturnPromotionsList() throws Exception {
        when(promoCodeService.getAllVisiblePromotions())
                .thenReturn(visiblePromotions);

        mockMvc.perform(get("/promo-codes/visible")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].promotionId", is(201)))
                .andExpect(jsonPath("$[0].title", is("Summer Discount")))
                .andExpect(jsonPath("$[0].priceFactor", is(0.85)))
                .andExpect(jsonPath("$[1].promotionId", is(202)))
                .andExpect(jsonPath("$[1].title", is("Weekend Special")))
                .andExpect(jsonPath("$[1].priceFactor", is(0.90)));
    }

    @Test
    @DisplayName("Validate promo code by path with valid code should return promotion details")
    void validatePromoCodeByPath_withValidCode_shouldReturnPromotionDetails() throws Exception {
        when(promoCodeService.validatePromoCode("SUMMER15"))
                .thenReturn(Optional.of(validPromotion1));

        mockMvc.perform(get("/promo-codes/validate/SUMMER15")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.promotionId", is(201)))
                .andExpect(jsonPath("$.title", is("Summer Discount")))
                .andExpect(jsonPath("$.description", is("Get 15% off on summer bookings")))
                .andExpect(jsonPath("$.priceFactor", is(0.85)));
    }

    @Test
    @DisplayName("Validate promo code by path with invalid code should return error")
    void validatePromoCodeByPath_withInvalidCode_shouldReturnError() throws Exception {
        when(promoCodeService.validatePromoCode("INVALID"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/promo-codes/validate/INVALID")
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Invalid promo code")))
                .andExpect(jsonPath("$.message", is("The promo code is invalid, expired, or not available")));
    }

    @Test
    @DisplayName("Validate promo code with POST with valid code should return promotion details")
    void validatePromoCodePost_withValidCode_shouldReturnPromotionDetails() throws Exception {
        when(promoCodeService.validatePromoCode(anyString()))
                .thenReturn(Optional.of(validPromotion1));

        mockMvc.perform(post("/promo-codes/validate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.promotionId", is(201)))
                .andExpect(jsonPath("$.title", is("Summer Discount")))
                .andExpect(jsonPath("$.description", is("Get 15% off on summer bookings")))
                .andExpect(jsonPath("$.priceFactor", is(0.85)));
    }

    @Test
    @DisplayName("Validate promo code with POST with invalid code should return error")
    void validatePromoCodePost_withInvalidCode_shouldReturnError() throws Exception {
        PromoCodeValidateRequest invalidRequest = PromoCodeValidateRequest.builder()
                .promoCode("INVALID")
                .build();

        when(promoCodeService.validatePromoCode("INVALID"))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/promo-codes/validate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Invalid promo code")))
                .andExpect(jsonPath("$.message", is("The promo code is invalid, expired, or not available")));
    }

    @Test
    @DisplayName("Validate promo code with POST with missing code should return validation error")
    void validatePromoCodePost_withMissingCode_shouldReturnValidationError() throws Exception {
        PromoCodeValidateRequest emptyRequest = PromoCodeValidateRequest.builder()
                .promoCode("")
                .build();

        mockMvc.perform(post("/promo-codes/validate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyRequest)))
                .andExpect(status().isBadRequest());
    }
} 