package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.dto.in.CombinedPromotionRequestDTO;
import com.kdu.rizzlers.dto.in.PromotionEligibilityRequestDTO;
import com.kdu.rizzlers.dto.out.PromotionDTO;
import com.kdu.rizzlers.dto.out.PropertyPromotionDTO;
import com.kdu.rizzlers.entity.PropertyPromotion;
import com.kdu.rizzlers.repository.PropertyPromotionRepository;
import com.kdu.rizzlers.service.PromotionGraphQLService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Spy;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromotionServiceImplTest {

    @Mock
    private PromotionGraphQLService promotionGraphQLService;

    @Mock
    private PropertyPromotionRepository propertyPromotionRepository;

    @InjectMocks
    @Spy
    private PromotionServiceImpl promotionService;

    private PromotionDTO seniorDiscount;
    private PromotionDTO militaryDiscount;
    private PromotionDTO weekendDiscount;
    private PropertyPromotion dbPromotion;
    private PropertyPromotionDTO propertyPromotionDTO;
    private PromotionDTO dbPromotionDTO;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer propertyId;

    @BeforeEach
    void setUp() {
        // Common test data
        propertyId = 123;
        startDate = LocalDate.now();
        endDate = startDate.plusDays(3);

        // GraphQL promotions
        seniorDiscount = PromotionDTO.builder()
                .promotionId(1)
                .promotionTitle("SENIOR_CITIZEN_DISCOUNT")
                .promotionDescription("10% discount for senior citizens")
                .priceFactor(0.9)
                .minimumDaysOfStay(1)
                .isDeactivated(false)
                .build();

        militaryDiscount = PromotionDTO.builder()
                .promotionId(2)
                .promotionTitle("Military personnel discount")
                .promotionDescription("15% discount for military personnel")
                .priceFactor(0.85)
                .minimumDaysOfStay(1)
                .isDeactivated(false)
                .build();

        weekendDiscount = PromotionDTO.builder()
                .promotionId(3)
                .promotionTitle("Weekend discount")
                .promotionDescription("5% discount for weekend stays")
                .priceFactor(0.95)
                .minimumDaysOfStay(2)
                .isDeactivated(false)
                .build();

        // DB promotion
        dbPromotion = mock(PropertyPromotion.class);
        propertyPromotionDTO = PropertyPromotionDTO.builder()
                .id(100L)
                .propertyId(propertyId)
                .promotionId(1) // Same ID as seniorDiscount to test overriding
                .title("SENIOR_CITIZEN_DISCOUNT")
                .description("15% discount for seniors (DB version)")
                .priceFactor(0.85) // Different factor from GraphQL
                .isActive(true)
                .isVisible(true)
                .startDate(startDate)
                .endDate(endDate)
                .build();
        
        dbPromotionDTO = PromotionDTO.builder()
                .promotionId(1)
                .promotionTitle("SENIOR_CITIZEN_DISCOUNT")
                .promotionDescription("15% discount for seniors (DB version)")
                .priceFactor(0.85)
                .minimumDaysOfStay(1)
                .isDeactivated(false)
                .build();
        
        lenient().when(dbPromotion.toDTO()).thenReturn(propertyPromotionDTO);
    }

    @Test
    @DisplayName("getAllPromotions should return promotions from GraphQL service")
    void getAllPromotions_shouldReturnPromotionsFromGraphQLService() {
        // Arrange
        List<PromotionDTO> expectedPromotions = List.of(seniorDiscount, militaryDiscount, weekendDiscount);
        when(promotionGraphQLService.fetchAllPromotions()).thenReturn(expectedPromotions);

        // Act
        List<PromotionDTO> result = promotionService.getAllPromotions();

        // Assert
        assertEquals(expectedPromotions.size(), result.size());
        assertEquals(expectedPromotions, result);
        verify(promotionGraphQLService).fetchAllPromotions();
    }

    @Test
    @DisplayName("getAllPromotions should return empty list when GraphQL service throws exception")
    void getAllPromotions_whenGraphQLServiceThrowsException_shouldReturnEmptyList() {
        // Arrange
        when(promotionGraphQLService.fetchAllPromotions()).thenThrow(new RuntimeException("GraphQL service error"));

        // Act
        List<PromotionDTO> result = promotionService.getAllPromotions();

        // Assert
        assertTrue(result.isEmpty());
        verify(promotionGraphQLService).fetchAllPromotions();
    }

    @Test
    @DisplayName("getEligiblePromotions should filter out ineligible promotions")
    void getEligiblePromotions_shouldFilterOutIneligiblePromotions() {
        // Arrange
        List<PromotionDTO> allPromotions = List.of(seniorDiscount, militaryDiscount, weekendDiscount);
        when(promotionGraphQLService.fetchAllPromotions()).thenReturn(allPromotions);

        // Create request for senior discount
        PromotionEligibilityRequestDTO request = PromotionEligibilityRequestDTO.builder()
                .startDate(startDate)
                .endDate(endDate)
                .adults(2)
                .seniorCitizens(1) // Has senior citizens
                .isMilitaryPersonnel(false)
                .isKduMember(false)
                .build();

        // Act
        List<PromotionDTO> result = promotionService.getEligiblePromotions(request);

        // Assert
        assertEquals(1, result.size());
        assertEquals("SENIOR_CITIZEN_DISCOUNT", result.get(0).getPromotionTitle());
    }

    @Test
    @DisplayName("getCombinedPromotionsForProperty should merge GraphQL and DB promotions")
    void getCombinedPromotionsForProperty_shouldMergePromotions() {
        // Arrange
        List<PromotionDTO> graphQlPromotions = List.of(seniorDiscount, militaryDiscount);
        List<PropertyPromotion> dbPromotions = List.of(dbPromotion);
        
        when(promotionGraphQLService.fetchAllPromotions()).thenReturn(graphQlPromotions);
        when(propertyPromotionRepository.findActiveAndVisiblePromotionsForPropertyInDateRange(
                eq(propertyId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(dbPromotions);
                
        // Create a partial mock of PropertyPromotionDTO to mock final method
        PropertyPromotionDTO mockPropPromotionDTO = spy(propertyPromotionDTO);
        when(dbPromotion.toDTO()).thenReturn(mockPropPromotionDTO);
        when(mockPropPromotionDTO.toPromotionDTO()).thenReturn(dbPromotionDTO);

        // Act
        List<PromotionDTO> result = promotionService.getCombinedPromotionsForProperty(propertyId, startDate, endDate);

        // Assert
        assertEquals(2, result.size());
        
        // Check if our DB promotion is in the result
        boolean foundDbPromotion = false;
        boolean foundMilitaryPromotion = false;
        
        for (PromotionDTO dto : result) {
            if (dto.getPromotionId() == 1 && dto.getPriceFactor() == 0.85) {
                foundDbPromotion = true;
            } else if (dto.getPromotionId() == 2) {
                foundMilitaryPromotion = true;
            }
        }
        
        assertTrue(foundDbPromotion, "DB promotion should be in the result");
        assertTrue(foundMilitaryPromotion, "Military promotion should be in the result");
    }

    @Test
    @DisplayName("getEligiblePropertyPromotions should return eligible promotions")
    void getEligiblePropertyPromotions_shouldReturnEligiblePromotions() {
        // Arrange
        CombinedPromotionRequestDTO request = CombinedPromotionRequestDTO.builder()
                .propertyId(propertyId)
                .startDate(startDate)
                .endDate(endDate)
                .adults(2)
                .seniorCitizens(1) // Has senior citizens
                .isMilitaryPersonnel(false)
                .isKduMember(false)
                .build();
        
        // Mock the getCombinedPromotionsForProperty method
        doReturn(List.of(seniorDiscount, militaryDiscount, weekendDiscount))
            .when(promotionService).getCombinedPromotionsForProperty(eq(propertyId), eq(startDate), eq(endDate));
        
        // Act
        List<PromotionDTO> result = promotionService.getEligiblePropertyPromotions(request);
        
        // Assert
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getPromotionId());
        assertEquals("SENIOR_CITIZEN_DISCOUNT", result.get(0).getPromotionTitle());
    }

    @Test
    @DisplayName("getCombinedPromotions (deprecated) should work correctly")
    void getCombinedPromotions_deprecatedMethod_shouldWorkCorrectly() {
        // Arrange
        List<PromotionDTO> graphQlPromotions = List.of(seniorDiscount, militaryDiscount);
        List<PropertyPromotion> dbPromotions = List.of(dbPromotion);
        
        when(promotionGraphQLService.fetchAllPromotions()).thenReturn(graphQlPromotions);
        when(propertyPromotionRepository.findActiveAndVisiblePromotionsInDateRange(
                any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(dbPromotions);
                
        // Create a partial mock of PropertyPromotionDTO to mock final method
        PropertyPromotionDTO mockPropPromotionDTO = spy(propertyPromotionDTO);
        when(dbPromotion.toDTO()).thenReturn(mockPropPromotionDTO);
        when(mockPropPromotionDTO.toPromotionDTO()).thenReturn(dbPromotionDTO);
        
        // Act
        List<PromotionDTO> result = promotionService.getCombinedPromotions(startDate, endDate);
        
        // Assert
        assertEquals(2, result.size());
        
        // Check if our DB promotion is in the result
        boolean foundDbPromotion = false;
        boolean foundMilitaryPromotion = false;
        
        for (PromotionDTO dto : result) {
            if (dto.getPromotionId() == 1 && dto.getPriceFactor() == 0.85) {
                foundDbPromotion = true;
            } else if (dto.getPromotionId() == 2) {
                foundMilitaryPromotion = true;
            }
        }
        
        assertTrue(foundDbPromotion, "DB promotion should be in the result");
        assertTrue(foundMilitaryPromotion, "Military promotion should be in the result");
    }
} 