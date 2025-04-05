package com.kdu.rizzlers.dto.out;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for room availability status check
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomAvailabilityStatusResponseDTO {
    private boolean available;
    private Integer roomTypeId;
    private String roomTypeName;
    private Integer propertyId;
    private List<Integer> availableRoomIds;
    private Integer availableRoomCount;
    private Integer requestedRoomCount;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalGuests;
    private Map<String, Integer> guestBreakdown;
    private Double totalPrice;
    private List<DailyPriceDTO> dailyPrices;
    
    // Promotion-related fields
    private Integer promotionId;
    private String promotionTitle;
    private String promotionDescription;
    private Double priceFactor;
    private BigDecimal discountedBaseAmount;
    private BigDecimal promoDiscount;
    
    // Property configuration fields
    private BigDecimal surcharge; // Percentage value
    private BigDecimal surchargeAmount; // Calculated amount based on percentage
    private BigDecimal fees; // Fixed fees
    private BigDecimal tax; // Tax percentage
    private BigDecimal taxAmount; // Calculated tax amount
    private BigDecimal totalWithSurchargeAndFees;
    private BigDecimal finalTotal; // Total after adding tax
    
    // Additional fields for frontend display
    private BigDecimal occupancyTax; // Same as tax
    private BigDecimal resortFee; // Combined surcharge and fees
    
    // Payment breakdown fields
    private BigDecimal dueNow; // Amount due immediately during booking
    private BigDecimal dueAtResort; // Amount to be paid at the resort
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyPriceDTO {
        private LocalDate date;
        private Double price;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomDetailsDTO {
        private Integer maxCapacity;
        private Integer singleBedCount;
        private Integer doubleBedCount;
        private Integer totalBedCount;
        private Integer areaInSquareFeet;
        private List<String> amenities;
    }
} 