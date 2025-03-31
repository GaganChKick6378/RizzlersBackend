package com.kdu.rizzlers.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kdu.rizzlers.dto.in.CombinedPromotionRequestDTO;
import com.kdu.rizzlers.dto.out.PromotionDTO;
import com.kdu.rizzlers.service.PromotionService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

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
    private CombinedPromotionRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        // Create sample promotions
        PromotionDTO promotion1 = PromotionDTO.builder()
                .promotionId(1)
                .title("Early Bird Discount")
                .description("Book 30 days in advance and get 15% off")
                .priceFactor(0.85)
                .isActive(true)
                .build();

        PromotionDTO promotion2 = PromotionDTO.builder()
                .promotionId(2)
                .title("Military Discount")
                .description("10% discount for military personnel")
                .priceFactor(0.90)
                .isActive(true)
                .build();

        promotions = Arrays.asList(promotion1, promotion2);

        // Create request DTO
        Map<String, Integer> guestCount = new HashMap<>();
        guestCount.put("adult", 2);
        guestCount.put("kid", 1);
        guestCount.put("seniorCitizen", 0);

        requestDTO = CombinedPromotionRequestDTO.builder()
                .propertyId(1)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(3))
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
    @DisplayName("Should return all promotions")
    void getAllPromotions_shouldReturnAllPromotions() throws Exception {
        // Arrange
        when(promotionService.getAllPromotions()).thenReturn(promotions);

        // Act & Assert
        mockMvc.perform(get("/promotions")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].promotionId", is(1)))
                .andExpect(jsonPath("$[0].title", is("Early Bird Discount")))
                .andExpect(jsonPath("$[0].priceFactor", is(0.85)))
                .andExpect(jsonPath("$[1].promotionId", is(2)))
                .andExpect(jsonPath("$[1].title", is("Military Discount")));
    }

    @Test
    @DisplayName("Should return eligible promotions")
    void getEligiblePromotions_shouldReturnEligiblePromotions() throws Exception {
        // Arrange
        when(promotionService.getEligiblePromotions(any(CombinedPromotionRequestDTO.class)))
                .thenReturn(promotions);

        // Act & Assert
        mockMvc.perform(post("/promotions/eligible")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].promotionId", is(1)))
                .andExpect(jsonPath("$[0].title", is("Early Bird Discount")))
                .andExpect(jsonPath("$[1].promotionId", is(2)))
                .andExpect(jsonPath("$[1].title", is("Military Discount")));
    }

    @Test
    @DisplayName("Should handle empty promotions list")
    void getAllPromotions_whenNoPromotions_shouldReturnEmptyList() throws Exception {
        // Arrange
        when(promotionService.getAllPromotions()).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/promotions")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should handle military personnel request")
    void getEligiblePromotions_forMilitaryPersonnel_shouldIncludeMilitaryDiscount() throws Exception {
        // Arrange
        requestDTO.setIsMilitaryPersonnel(true);
        
        when(promotionService.getEligiblePromotions(any(CombinedPromotionRequestDTO.class)))
                .thenReturn(List.of(promotions.get(1))); // Only return military discount

        // Act & Assert
        mockMvc.perform(post("/promotions/eligible")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].promotionId", is(2)))
                .andExpect(jsonPath("$[0].title", is("Military Discount")));
    }
} 