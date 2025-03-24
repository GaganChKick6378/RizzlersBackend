package com.kdu.rizzlers.integration;

import com.kdu.rizzlers.config.CustomTestConfiguration;
import com.kdu.rizzlers.entity.PropertyPromotionSchedule;
import com.kdu.rizzlers.repository.PropertyPromotionScheduleRepository;
import com.kdu.rizzlers.service.impl.RoomRateServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
    properties = {
        "graphql_endpoint=http://mockapi.example.com/graphql",
        "graphql.endpoint=http://mockapi.example.com/graphql",
        "graphql_api_key=test-api-key",
        "graphql.api-key=test-api-key",
        "graphql.api-key-header=x-api-key",
        "container_port=8080"
    }
)
@AutoConfigureMockMvc
@AutoConfigureTestEntityManager
@Import(CustomTestConfiguration.class)
@ActiveProfiles("test")
@Transactional
@TestPropertySource("classpath:application-test.yml")
@MockitoSettings(strictness = Strictness.LENIENT)
public class RoomRateControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PropertyPromotionScheduleRepository propertyPromotionScheduleRepository;
    
    @MockBean
    private WebClient.Builder webClientBuilder;
    
    @SpyBean
    private RoomRateServiceImpl roomRateService;
    
    @BeforeEach
    public void setup() {
        // Clean up the repository
        propertyPromotionScheduleRepository.deleteAll();
        
        // Mock the GraphQL-dependent method to avoid actual GraphQL calls
        doReturn(Collections.emptyList()).when(roomRateService)
            .getDailyRatesWithPromotions(any(Integer.class), any(Integer.class));
        
        // Add test data with created_at and updated_at fields
        LocalDateTime now = LocalDateTime.now();
        
        // Use the builder pattern which is more reliable
        PropertyPromotionSchedule promotion1 = PropertyPromotionSchedule.builder()
                .propertyId(1)
                .promotionId(101)
                .priceFactor(BigDecimal.valueOf(0.8))
                .startDate(LocalDate.now().minusDays(5))
                .endDate(LocalDate.now().plusDays(5))
                .isActive(true)
                .build();
        promotion1.setCreatedAt(now);
        
        PropertyPromotionSchedule promotion2 = PropertyPromotionSchedule.builder()
                .propertyId(1)
                .promotionId(102)
                .priceFactor(BigDecimal.valueOf(0.9))
                .startDate(LocalDate.now().plusDays(10))
                .endDate(LocalDate.now().plusDays(20))
                .isActive(true)
                .build();
        promotion2.setCreatedAt(now);
        
        PropertyPromotionSchedule promotion3 = PropertyPromotionSchedule.builder()
                .propertyId(2)
                .promotionId(201)
                .priceFactor(BigDecimal.valueOf(0.85))
                .startDate(LocalDate.now().minusDays(10))
                .endDate(LocalDate.now().plusDays(10))
                .isActive(true)
                .build();
        promotion3.setCreatedAt(now);
        
        try {
            // Save each entity separately to avoid bulk insert issues
            propertyPromotionScheduleRepository.save(promotion1);
            propertyPromotionScheduleRepository.save(promotion2);
            propertyPromotionScheduleRepository.save(promotion3);
        } catch (Exception e) {
            // Log error or handle it appropriately
            System.err.println("Error saving test data: " + e.getMessage());
            e.printStackTrace(); // Add stack trace for better debugging
        }
    }
    
    @Test
    public void testGetAllPromotions() throws Exception {
        // When & Then
        mockMvc.perform(get("/room-rates/all-promotions")
                .param("propertyId", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].propertyId", is(1)))
                .andExpect(jsonPath("$[0].promotionId", is(101)))
                .andExpect(jsonPath("$[1].propertyId", is(1)))
                .andExpect(jsonPath("$[1].promotionId", is(102)));
    }
    
    @Test
    public void testGetActivePromotions() throws Exception {
        // Given
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(2);
        String formattedStartDate = startDate.format(DateTimeFormatter.ISO_DATE);
        String formattedEndDate = endDate.format(DateTimeFormatter.ISO_DATE);
        
        // When & Then
        mockMvc.perform(get("/room-rates/promotions")
                .param("propertyId", "1")
                .param("startDate", formattedStartDate)
                .param("endDate", formattedEndDate)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].propertyId", is(1)))
                .andExpect(jsonPath("$[0].promotionId", is(101)));
    }
    
    @Test
    public void testGetActivePromotions_WithDifferentProperty() throws Exception {
        // Given
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusDays(2);
        String formattedStartDate = startDate.format(DateTimeFormatter.ISO_DATE);
        String formattedEndDate = endDate.format(DateTimeFormatter.ISO_DATE);
        
        // When & Then
        mockMvc.perform(get("/room-rates/promotions")
                .param("propertyId", "2")
                .param("startDate", formattedStartDate)
                .param("endDate", formattedEndDate)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].propertyId", is(2)))
                .andExpect(jsonPath("$[0].promotionId", is(201)));
    }
} 