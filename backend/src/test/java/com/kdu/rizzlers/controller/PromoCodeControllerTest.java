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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
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

    private PropertyPromotionScheduleResponse validPromotion;
    private PropertyPromotionScheduleResponse secondPromotion;
    private PromoCodeValidateRequest validRequest;

    @BeforeEach
    void setup() {
        validPromotion = PropertyPromotionScheduleResponse.builder()
                .id(1L)
                .propertyId(100)
                .promotionId(200)
                .title("Summer Special")
                .description("20% off for summer bookings")
                .promoCode("SUMMER20")
                .priceFactor(new BigDecimal("0.80")) // 20% off = 0.80 factor
                .startDate(LocalDate.now().minusDays(10))
                .endDate(LocalDate.now().plusDays(30))
                .isActive(true)
                .isVisible(true)
                .createdAt(LocalDateTime.now().minusDays(15))
                .updatedAt(LocalDateTime.now().minusDays(15))
                .build();

        secondPromotion = PropertyPromotionScheduleResponse.builder()
                .id(2L)
                .propertyId(100)
                .promotionId(201)
                .title("Early Bird")
                .description("15% off for early bookings")
                .promoCode("EARLY15")
                .priceFactor(new BigDecimal("0.85")) // 15% off = 0.85 factor
                .startDate(LocalDate.now().minusDays(5))
                .endDate(LocalDate.now().plusDays(20))
                .isActive(true)
                .isVisible(true)
                .createdAt(LocalDateTime.now().minusDays(10))
                .updatedAt(LocalDateTime.now().minusDays(10))
                .build();

        validRequest = PromoCodeValidateRequest.builder()
                .promoCode("SUMMER20")
                .build();
    }

    @Test
    @DisplayName("Should return all visible promotions")
    void getVisiblePromotions_shouldReturnAllVisiblePromotions() throws Exception {
        // Arrange
        List<PropertyPromotionScheduleResponse> promotions = Arrays.asList(validPromotion, secondPromotion);
        when(promoCodeService.getAllVisiblePromotions()).thenReturn(promotions);

        // Act & Assert
        mockMvc.perform(get("/promo-codes/visible")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].promotionId", is(validPromotion.getPromotionId())))
                .andExpect(jsonPath("$[0].title", is(validPromotion.getTitle())))
                .andExpect(jsonPath("$[0].description", is(validPromotion.getDescription())))
                .andExpect(jsonPath("$[0].priceFactor", is(validPromotion.getPriceFactor().doubleValue())))
                .andExpect(jsonPath("$[1].promotionId", is(secondPromotion.getPromotionId())))
                .andExpect(jsonPath("$[1].title", is(secondPromotion.getTitle())))
                .andExpect(jsonPath("$[1].description", is(secondPromotion.getDescription())))
                .andExpect(jsonPath("$[1].priceFactor", is(secondPromotion.getPriceFactor().doubleValue())));
    }

    @Test
    @DisplayName("Should validate promo code using path parameter (GET)")
    void validatePromoCodeByPath_withValidPromoCode_shouldReturnPromotionDetails() throws Exception {
        // Arrange
        when(promoCodeService.validatePromoCode("SUMMER20")).thenReturn(Optional.of(validPromotion));

        // Act & Assert
        mockMvc.perform(get("/promo-codes/validate/SUMMER20")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(validPromotion.getId().intValue())))
                .andExpect(jsonPath("$.propertyId", is(validPromotion.getPropertyId())))
                .andExpect(jsonPath("$.promotionId", is(validPromotion.getPromotionId())))
                .andExpect(jsonPath("$.title", is(validPromotion.getTitle())))
                .andExpect(jsonPath("$.description", is(validPromotion.getDescription())))
                .andExpect(jsonPath("$.promoCode", is(validPromotion.getPromoCode())))
                .andExpect(jsonPath("$.priceFactor", is(validPromotion.getPriceFactor().doubleValue())))
                .andExpect(jsonPath("$.isActive", is(validPromotion.getIsActive())))
                .andExpect(jsonPath("$.isVisible", is(validPromotion.getIsVisible())));
    }

    @Test
    @DisplayName("Should return error for invalid promo code (GET)")
    void validatePromoCodeByPath_withInvalidPromoCode_shouldReturnError() throws Exception {
        // Arrange
        when(promoCodeService.validatePromoCode("INVALID")).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/promo-codes/validate/INVALID")
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Invalid promo code")))
                .andExpect(jsonPath("$.message", is("The promo code is invalid, expired, or not available")));
    }

    @Test
    @DisplayName("Should validate promo code using POST request")
    void validatePromoCodePost_withValidPromoCode_shouldReturnSimplifiedResponse() throws Exception {
        // Arrange
        when(promoCodeService.validatePromoCode(anyString())).thenReturn(Optional.of(validPromotion));

        // Act & Assert
        mockMvc.perform(post("/promo-codes/validate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.promotionId", is(validPromotion.getPromotionId())))
                .andExpect(jsonPath("$.title", is(validPromotion.getTitle())))
                .andExpect(jsonPath("$.description", is(validPromotion.getDescription())))
                .andExpect(jsonPath("$.priceFactor", is(validPromotion.getPriceFactor().doubleValue())))
                // Following fields should not be in the response
                .andExpect(jsonPath("$.id").doesNotExist())
                .andExpect(jsonPath("$.propertyId").doesNotExist())
                .andExpect(jsonPath("$.promoCode").doesNotExist())
                .andExpect(jsonPath("$.startDate").doesNotExist())
                .andExpect(jsonPath("$.endDate").doesNotExist())
                .andExpect(jsonPath("$.isActive").doesNotExist())
                .andExpect(jsonPath("$.isVisible").doesNotExist())
                .andExpect(jsonPath("$.createdAt").doesNotExist())
                .andExpect(jsonPath("$.updatedAt").doesNotExist());
    }

    @Test
    @DisplayName("Should return error for invalid promo code (POST)")
    void validatePromoCodePost_withInvalidPromoCode_shouldReturnError() throws Exception {
        // Arrange
        when(promoCodeService.validatePromoCode(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(post("/promo-codes/validate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Invalid promo code")))
                .andExpect(jsonPath("$.message", is("The promo code is invalid, expired, or not available")));
    }

    @Test
    @DisplayName("Should return bad request for invalid request body")
    void validatePromoCodePost_withInvalidRequest_shouldReturnBadRequest() throws Exception {
        // Arrange
        PromoCodeValidateRequest invalidRequest = PromoCodeValidateRequest.builder().build(); // No promo code

        // Act & Assert
        mockMvc.perform(post("/promo-codes/validate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
} 