package com.kdu.rizzlers.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kdu.rizzlers.dto.in.CombinedPromotionRequestDTO;
import com.kdu.rizzlers.dto.out.PromotionDTO;
import com.kdu.rizzlers.service.PromotionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PromotionController.class)
@AutoConfigureMockMvc(addFilters = false)
class PromotionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PromotionService promotionService;

    private List<PromotionDTO> promotions;
    private CombinedPromotionRequestDTO validRequest;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        startDate = LocalDate.now().plusDays(1);
        endDate = startDate.plusDays(5);

        // Create sample promotions using the builder pattern
        PromotionDTO promotion1 = PromotionDTO.builder()
                .promotionId(1)
                .promotionTitle("Early Bird Special")
                .promotionDescription("Book early and save 15%")
                .priceFactor(0.85)
                .minimumDaysOfStay(2)
                .isDeactivated(false)
                .originalPrice(200.0)
                .discountedPrice(170.0)
                .build();

        PromotionDTO promotion2 = PromotionDTO.builder()
                .promotionId(2)
                .promotionTitle("Weekend Getaway")
                .promotionDescription("Special weekend rates")
                .priceFactor(0.9)
                .minimumDaysOfStay(2)
                .isDeactivated(false)
                .originalPrice(200.0)
                .discountedPrice(180.0)
                .build();

        promotions = Arrays.asList(promotion1, promotion2);

        // Setup guest count map
        Map<String, Integer> guestCount = new HashMap<>();
        guestCount.put("adult", 2);
        guestCount.put("child", 1);

        // Create a valid request using the builder pattern
        validRequest = CombinedPromotionRequestDTO.builder()
                .propertyId(1)
                .startDate(startDate)
                .endDate(endDate)
                .guests(3)
                .guestCount(guestCount)
                .adults(2)
                .kids(1)
                .seniorCitizens(0)
                .isMilitaryPersonnel(false)
                .isKduMember(false)
                .isUpfrontPayment(true)
                .build();
    }

    @Test
    @DisplayName("Get all promotions should return a list of promotions")
    void getAllPromotions_shouldReturnListOfPromotions() throws Exception {
        when(promotionService.getAllPromotions()).thenReturn(promotions);

        mockMvc.perform(get("/promotions")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].promotion_id", is(1)))
                .andExpect(jsonPath("$[0].promotion_title", is("Early Bird Special")))
                .andExpect(jsonPath("$[1].promotion_id", is(2)))
                .andExpect(jsonPath("$[1].promotion_title", is("Weekend Getaway")));

        verify(promotionService).getAllPromotions();
    }

    @Test
    @DisplayName("Get eligible promotions with property ID should return eligible promotions for property")
    void getEligiblePromotions_withPropertyId_shouldReturnEligiblePromotionsForProperty() throws Exception {
        when(promotionService.getEligiblePropertyPromotions(any(CombinedPromotionRequestDTO.class)))
                .thenReturn(promotions);

        mockMvc.perform(post("/promotions/eligible")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].promotion_id", is(1)))
                .andExpect(jsonPath("$[0].promotion_title", is("Early Bird Special")))
                .andExpect(jsonPath("$[1].promotion_id", is(2)))
                .andExpect(jsonPath("$[1].promotion_title", is("Weekend Getaway")));

        verify(promotionService).getEligiblePropertyPromotions(any(CombinedPromotionRequestDTO.class));
    }

    @Test
    @DisplayName("Get eligible promotions without property ID should return general eligible promotions")
    void getEligiblePromotions_withoutPropertyId_shouldReturnGeneralEligiblePromotions() throws Exception {
        // Create a request without property ID
        CombinedPromotionRequestDTO requestWithoutPropertyId = CombinedPromotionRequestDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .guests(3)
                .adults(2)
                .kids(1)
                .seniorCitizens(0)
                .isMilitaryPersonnel(false)
                .isKduMember(false)
                .isUpfrontPayment(true)
                .build();

        when(promotionService.getEligiblePromotions(any()))
                .thenReturn(promotions);

        mockMvc.perform(post("/promotions/eligible")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestWithoutPropertyId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].promotion_id", is(1)))
                .andExpect(jsonPath("$[1].promotion_id", is(2)));

        verify(promotionService).getEligiblePromotions(any());
    }

    @Test
    @DisplayName("Get eligible promotions with various guest types should correctly calculate total guest count")
    void getEligiblePromotions_withVariousGuestTypes_shouldCalculateTotalGuestCount() throws Exception {
        // Create a request with various guest types but no explicit guests field
        Map<String, Integer> detailedGuestCount = new HashMap<>();
        detailedGuestCount.put("adult", 2);
        detailedGuestCount.put("senior", 1);
        detailedGuestCount.put("child", 2);

        CombinedPromotionRequestDTO detailedRequest = CombinedPromotionRequestDTO.builder()
                .propertyId(1)
                .startDate(startDate)
                .endDate(endDate)
                .guestCount(detailedGuestCount)
                .adults(2)
                .kids(2)
                .seniorCitizens(1)
                .isMilitaryPersonnel(false)
                .isKduMember(true)
                .isUpfrontPayment(false)
                .build();

        when(promotionService.getEligiblePropertyPromotions(any(CombinedPromotionRequestDTO.class)))
                .thenReturn(promotions);

        mockMvc.perform(post("/promotions/eligible")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(detailedRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(promotionService).getEligiblePropertyPromotions(any(CombinedPromotionRequestDTO.class));
    }

    @Test
    @DisplayName("Get eligible promotions with empty response should return empty list")
    void getEligiblePromotions_withEmptyResponse_shouldReturnEmptyList() throws Exception {
        when(promotionService.getEligiblePropertyPromotions(any(CombinedPromotionRequestDTO.class)))
                .thenReturn(List.of());

        mockMvc.perform(post("/promotions/eligible")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(promotionService).getEligiblePropertyPromotions(any(CombinedPromotionRequestDTO.class));
    }
} 