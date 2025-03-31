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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
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

    private List<PromoCodeResponse> promoCodeResponses;
    private PromoCodeResponse validPromoCodeResponse;
    private PromoCodeValidateRequest validateRequest;
    private PropertyPromotionScheduleResponse promotionScheduleResponse;
    private String promoCode;

    @BeforeEach
    void setUp() {
        promoCode = "SUMMER2023";
        
        // Create sample promo code responses
        validPromoCodeResponse = PromoCodeResponse.builder()
                .promotionId(1)
                .title("Summer Special")
                .description("Get 20% off your summer booking")
                .priceFactor(0.8)
                .promoCode(promoCode)
                .build();

        PromoCodeResponse promoCode2 = PromoCodeResponse.builder()
                .promotionId(2)
                .title("Weekend Getaway")
                .description("Save 15% on weekend stays")
                .priceFactor(0.85)
                .promoCode("WEEKEND15")
                .build();

        promoCodeResponses = Arrays.asList(validPromoCodeResponse, promoCode2);

        // Create validate request
        validateRequest = new PromoCodeValidateRequest();
        validateRequest.setPromoCode(promoCode);
        validateRequest.setPropertyId(1);
        validateRequest.setStartDate(LocalDate.now().plusDays(1));
        validateRequest.setEndDate(LocalDate.now().plusDays(3));
        
        // Create promotion schedule response
        promotionScheduleResponse = new PropertyPromotionScheduleResponse();
        promotionScheduleResponse.setPropertyId(1);
        promotionScheduleResponse.setPropertyName("Test Hotel");
        promotionScheduleResponse.setPromotionId(1);
        promotionScheduleResponse.setTitle("Summer Special");
        promotionScheduleResponse.setDescription("Get 20% off your summer booking");
        promotionScheduleResponse.setPriceFactor(0.8);
        promotionScheduleResponse.setStartDate(LocalDate.now().minusDays(10));
        promotionScheduleResponse.setEndDate(LocalDate.now().plusDays(20));
    }

    @Test
    @DisplayName("Should return visible promotions")
    void getVisiblePromotions_shouldReturnVisiblePromotions() throws Exception {
        // Arrange
        when(promoCodeService.getAllVisiblePromotions()).thenReturn(promoCodeResponses);

        // Act & Assert
        mockMvc.perform(get("/promo-codes/visible")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].promotionId", is(1)))
                .andExpect(jsonPath("$[0].title", is("Summer Special")))
                .andExpect(jsonPath("$[0].priceFactor", is(0.8)))
                .andExpect(jsonPath("$[1].promotionId", is(2)))
                .andExpect(jsonPath("$[1].title", is("Weekend Getaway")));
    }

    @Test
    @DisplayName("Should validate promo code using path parameter")
    void validatePromoCodePath_shouldValidatePromoCode() throws Exception {
        // Arrange
        when(promoCodeService.validatePromoCode(anyString())).thenReturn(validPromoCodeResponse);

        // Act & Assert
        mockMvc.perform(get("/promo-codes/validate/{promoCode}", promoCode)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.promotionId", is(1)))
                .andExpect(jsonPath("$.title", is("Summer Special")))
                .andExpect(jsonPath("$.priceFactor", is(0.8)))
                .andExpect(jsonPath("$.promoCode", is(promoCode)));
    }

    @Test
    @DisplayName("Should validate promo code using request body")
    void validatePromoCode_shouldValidatePromoCode() throws Exception {
        // Arrange
        when(promoCodeService.validatePromoCode(any(PromoCodeValidateRequest.class)))
                .thenReturn(validPromoCodeResponse);

        // Act & Assert
        mockMvc.perform(post("/promo-codes/validate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.promotionId", is(1)))
                .andExpect(jsonPath("$.title", is("Summer Special")))
                .andExpect(jsonPath("$.priceFactor", is(0.8)))
                .andExpect(jsonPath("$.promoCode", is(promoCode)));
    }

    @Test
    @DisplayName("Should get promotion schedule for property")
    void getPromotionScheduleForProperty_shouldReturnSchedule() throws Exception {
        // Arrange
        when(promoCodeService.getPromotionScheduleForProperty(1)).thenReturn(List.of(promotionScheduleResponse));

        // Act & Assert
        mockMvc.perform(get("/promo-codes/property/{propertyId}/schedule", 1)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].propertyId", is(1)))
                .andExpect(jsonPath("$[0].propertyName", is("Test Hotel")))
                .andExpect(jsonPath("$[0].promotionId", is(1)))
                .andExpect(jsonPath("$[0].title", is("Summer Special")))
                .andExpect(jsonPath("$[0].priceFactor", is(0.8)));
    }

    @Test
    @DisplayName("Should return empty list when no visible promotions")
    void getVisiblePromotions_whenNoPromotions_shouldReturnEmptyList() throws Exception {
        // Arrange
        when(promoCodeService.getAllVisiblePromotions()).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/promo-codes/visible")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should handle invalid promo code")
    void validatePromoCode_withInvalidCode_shouldReturnBadRequest() throws Exception {
        // Arrange
        when(promoCodeService.validatePromoCode(any(PromoCodeValidateRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid promo code"));

        // Act & Assert
        mockMvc.perform(post("/promo-codes/validate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Failed to process request")));
    }
} 