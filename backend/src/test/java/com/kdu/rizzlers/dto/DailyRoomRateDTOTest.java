package com.kdu.rizzlers.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kdu.rizzlers.dto.out.DailyRoomRateDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class DailyRoomRateDTOTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    public void testDailyRoomRateDTO_Serialization() throws Exception {
        // Given
        LocalDate testDate = LocalDate.of(2024, 3, 15);
        Double minimumRate = 100.0;
        Boolean hasPromotion = true;
        Integer promotionId = 101;
        Double priceFactor = 0.8;
        Double discountedRate = 80.0;
        
        DailyRoomRateDTO dto = DailyRoomRateDTO.builder()
                .date(testDate)
                .minimumRate(minimumRate)
                .hasPromotion(hasPromotion)
                .promotionId(promotionId)
                .priceFactor(priceFactor)
                .discountedRate(discountedRate)
                .build();
        
        // When
        String json = objectMapper.writeValueAsString(dto);
        
        // Then
        assertTrue(json.contains("\"date\":\"2024-03-15\""));
        assertTrue(json.contains("\"minimum_rate\":100.0"));
        assertTrue(json.contains("\"has_promotion\":true"));
        assertTrue(json.contains("\"promotion_id\":101"));
        assertTrue(json.contains("\"price_factor\":0.8"));
        assertTrue(json.contains("\"discounted_rate\":80.0"));
    }
    
    @Test
    public void testDailyRoomRateDTO_Deserialization() throws Exception {
        // Given
        String json = "{\"date\":\"2024-03-15\",\"minimum_rate\":100.0,\"has_promotion\":true,\"promotion_id\":101,\"price_factor\":0.8,\"discounted_rate\":80.0}";
        
        // When
        DailyRoomRateDTO dto = objectMapper.readValue(json, DailyRoomRateDTO.class);
        
        // Then
        assertEquals(LocalDate.of(2024, 3, 15), dto.getDate());
        assertEquals(100.0, dto.getMinimumRate());
        assertTrue(dto.getHasPromotion());
        assertEquals(101, dto.getPromotionId());
        assertEquals(0.8, dto.getPriceFactor());
        assertEquals(80.0, dto.getDiscountedRate());
    }
    
    @Test
    public void testDailyRoomRateDTO_Builder() {
        // When
        DailyRoomRateDTO dto = DailyRoomRateDTO.builder()
                .date(LocalDate.of(2024, 3, 15))
                .minimumRate(100.0)
                .hasPromotion(true)
                .promotionId(101)
                .priceFactor(0.8)
                .discountedRate(80.0)
                .build();
                
        // Then
        assertEquals(LocalDate.of(2024, 3, 15), dto.getDate());
        assertEquals(100.0, dto.getMinimumRate());
        assertTrue(dto.getHasPromotion());
        assertEquals(101, dto.getPromotionId());
        assertEquals(0.8, dto.getPriceFactor());
        assertEquals(80.0, dto.getDiscountedRate());
    }
    
    @Test
    public void testDailyRoomRateDTO_NoArgsConstructor() {
        // When
        DailyRoomRateDTO dto = new DailyRoomRateDTO();
        dto.setDate(LocalDate.of(2024, 3, 15));
        dto.setMinimumRate(100.0);
        dto.setHasPromotion(true);
        dto.setPromotionId(101);
        dto.setPriceFactor(0.8);
        dto.setDiscountedRate(80.0);
        
        // Then
        assertEquals(LocalDate.of(2024, 3, 15), dto.getDate());
        assertEquals(100.0, dto.getMinimumRate());
        assertTrue(dto.getHasPromotion());
        assertEquals(101, dto.getPromotionId());
        assertEquals(0.8, dto.getPriceFactor());
        assertEquals(80.0, dto.getDiscountedRate());
    }
    
    @Test
    public void testDailyRoomRateDTO_AllArgsConstructor() {
        // When
        DailyRoomRateDTO dto = new DailyRoomRateDTO(
                LocalDate.of(2024, 3, 15),
                100.0,
                true,
                101,
                0.8,
                80.0
        );
        
        // Then
        assertEquals(LocalDate.of(2024, 3, 15), dto.getDate());
        assertEquals(100.0, dto.getMinimumRate());
        assertTrue(dto.getHasPromotion());
        assertEquals(101, dto.getPromotionId());
        assertEquals(0.8, dto.getPriceFactor());
        assertEquals(80.0, dto.getDiscountedRate());
    }
    
    @Test
    public void testDailyRoomRateDTO_Equals() {
        // Given
        DailyRoomRateDTO dto1 = new DailyRoomRateDTO(
                LocalDate.of(2024, 3, 15),
                100.0,
                true,
                101,
                0.8,
                80.0
        );
        
        DailyRoomRateDTO dto2 = new DailyRoomRateDTO(
                LocalDate.of(2024, 3, 15),
                100.0,
                true,
                101,
                0.8,
                80.0
        );
        
        DailyRoomRateDTO dto3 = new DailyRoomRateDTO(
                LocalDate.of(2024, 3, 16),
                110.0,
                false,
                null,
                1.0,
                110.0
        );
        
        // Then
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }
} 