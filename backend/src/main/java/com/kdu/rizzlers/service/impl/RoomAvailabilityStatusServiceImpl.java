package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.dto.PropertyConfigurationDTO;
import com.kdu.rizzlers.dto.in.RoomAvailabilityStatusRequestDTO;
import com.kdu.rizzlers.dto.out.RoomAvailabilityStatusResponseDTO;
import com.kdu.rizzlers.dto.out.RoomAvailabilityStatusResponseDTO.DailyPriceDTO;
import com.kdu.rizzlers.entity.PropertyPromotion;
import com.kdu.rizzlers.exception.ResourceNotFoundException;
import com.kdu.rizzlers.repository.PropertyPromotionRepository;
import com.kdu.rizzlers.repository.ReviewRepository;
import com.kdu.rizzlers.service.PropertyConfigurationService;
import com.kdu.rizzlers.service.PromotionGraphQLService;
import com.kdu.rizzlers.service.RoomAvailabilityCheckService;
import com.kdu.rizzlers.service.RoomAvailabilityStatusService;
import com.kdu.rizzlers.service.RoomDailyRatesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the RoomAvailabilityStatusService
 */
@Slf4j
@Service
public class RoomAvailabilityStatusServiceImpl implements RoomAvailabilityStatusService {

    private final RoomAvailabilityCheckService availabilityCheckService;
    private final RoomDailyRatesService dailyRatesService;
    private final ReviewRepository reviewRepository;
    private final PropertyConfigurationService propertyConfigurationService;
    private final PropertyPromotionRepository propertyPromotionRepository;
    private final PromotionGraphQLService promotionGraphQLService;

    public RoomAvailabilityStatusServiceImpl(
            RoomAvailabilityCheckService availabilityCheckService,
            RoomDailyRatesService dailyRatesService,
            ReviewRepository reviewRepository,
            PropertyConfigurationService propertyConfigurationService,
            PropertyPromotionRepository propertyPromotionRepository,
            PromotionGraphQLService promotionGraphQLService) {
        this.availabilityCheckService = availabilityCheckService;
        this.dailyRatesService = dailyRatesService;
        this.reviewRepository = reviewRepository;
        this.propertyConfigurationService = propertyConfigurationService;
        this.propertyPromotionRepository = propertyPromotionRepository;
        this.promotionGraphQLService = promotionGraphQLService;
    }

    @Override
    @Transactional(readOnly = true)
    public RoomAvailabilityStatusResponseDTO checkRoomAvailabilityStatus(RoomAvailabilityStatusRequestDTO request) {
        log.info("Checking room availability status for propertyId={}, roomTypeId={}, startDate={}, endDate={}, " +
                "guests={}, roomCount={}, bedCount={}", 
                request.getPropertyId(), request.getRoomTypeId(), request.getStartDate(), 
                request.getEndDate(), request.getGuests(), request.getRoomCount(), request.getBedCount());
        
        try {
            // Step 1: Check room availability
            Map<String, Object> availabilityResult = availabilityCheckService.checkRoomTypeAvailability(
                    request.getPropertyId(),
                    request.getRoomTypeId(),
                    request.getStartDate(),
                    request.getEndDate(),
                    request.getRoomCount());
            
            if (availabilityResult.isEmpty()) {
                log.info("No available rooms found for the criteria");
                return buildUnavailableResponse(request);
            }
            
            // Extract room type details and available room IDs
            Integer roomTypeId = (Integer) availabilityResult.get("roomTypeId");
            String roomTypeName = (String) availabilityResult.get("roomTypeName");
            Integer maxCapacity = (Integer) availabilityResult.get("maxCapacity");
            Integer singleBedCount = (Integer) availabilityResult.get("singleBedCount");
            Integer doubleBedCount = (Integer) availabilityResult.get("doubleBedCount");
            Integer totalBedCount = singleBedCount + doubleBedCount;
            @SuppressWarnings("unchecked")
            List<Integer> availableRoomIds = (List<Integer>) availabilityResult.get("availableRoomIds");
            Integer availableRoomCount = (Integer) availabilityResult.get("availableRoomCount");
            
            // Validate bed count if specified
            if (request.getBedCount() != null && totalBedCount < request.getBedCount()) {
                log.info("Room type has insufficient beds. Required: {}, Available: {}", 
                        request.getBedCount(), totalBedCount);
                return buildUnavailableResponse(request);
            }
            
            // Calculate total guests
            int totalGuests = request.getGuests() != null ? request.getGuests() : 
                    request.getGuestCount() != null ? 
                            request.getGuestCount().values().stream().mapToInt(Integer::intValue).sum() : 0;
            
            // Check if capacity is sufficient
            int requiredCapacityPerRoom = (int) Math.ceil((double) totalGuests / request.getRoomCount());
            if (maxCapacity < requiredCapacityPerRoom) {
                log.info("Room type has insufficient capacity. Required: {}, Available: {}", 
                        requiredCapacityPerRoom, maxCapacity);
                return buildUnavailableResponse(request);
            }
            
            // Step 2: Get daily prices
            Map<LocalDate, Double> dailyPrices = dailyRatesService.fetchDailyRoomTypePrices(
                    request.getRoomTypeId(),
                    request.getStartDate(),
                    request.getEndDate());
            
            if (dailyPrices.isEmpty()) {
                log.warn("No pricing information available for roomTypeId={} and date range", 
                        request.getRoomTypeId());
                return buildUnavailableResponse(request);
            }
            
            // Calculate total price
            double totalPrice = dailyRatesService.calculateTotalPrice(dailyPrices, request.getRoomCount());
            BigDecimal baseAmount = BigDecimal.valueOf(totalPrice).setScale(2, RoundingMode.HALF_UP);
            
            // Step 3: Get property configuration for surcharge, fees, and tax
            PropertyConfigurationDTO.Response propertyConfig = null;
            BigDecimal surcharge = BigDecimal.ZERO;
            BigDecimal surchargeAmount = BigDecimal.ZERO;
            BigDecimal fees = BigDecimal.ZERO;
            BigDecimal tax = BigDecimal.ZERO;
            BigDecimal taxAmount = BigDecimal.ZERO;
            BigDecimal totalWithSurchargeAndFees = baseAmount;
            BigDecimal finalTotal = baseAmount;
            BigDecimal resortFee = BigDecimal.ZERO;
            
            try {
                propertyConfig = propertyConfigurationService.getPropertyConfigurationByPropertyId(request.getPropertyId());
                
                if (propertyConfig != null) {
                    // 1. Get configuration values
                    surcharge = propertyConfig.getSurcharge() != null ? propertyConfig.getSurcharge() : BigDecimal.ZERO;
                    fees = propertyConfig.getFees() != null ? propertyConfig.getFees() : BigDecimal.ZERO;
                    tax = propertyConfig.getTax() != null ? propertyConfig.getTax() : BigDecimal.ZERO;
                    
                    // 2. Order of calculation:
                    // Step 1: Calculate surcharge (percentage of base amount)
                    surchargeAmount = baseAmount
                            .multiply(surcharge)
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    
                    // Step 2: Add fixed fees
                    totalWithSurchargeAndFees = baseAmount
                            .add(surchargeAmount)
                            .add(fees);
                    
                    // Step 3: Calculate tax on the total with surcharge and fees
                    taxAmount = totalWithSurchargeAndFees
                            .multiply(tax)
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    
                    // Calculate final total
                    finalTotal = totalWithSurchargeAndFees.add(taxAmount);
                    
                    // Calculate resort fee (combined surcharge and fees)
                    resortFee = surchargeAmount.add(fees);
                    
                    log.info("Price calculation: Base amount: {}, Surcharge({}%): {}, Fees: {}, " +
                            "Subtotal: {}, Tax({}%): {}, Final Total: {}", 
                            baseAmount, surcharge, surchargeAmount, fees, 
                            totalWithSurchargeAndFees, tax, taxAmount, finalTotal);
                }
            } catch (ResourceNotFoundException e) {
                log.warn("Property configuration not found for propertyId={}. Using default values.", 
                        request.getPropertyId());
            }
            
            // Build response
            return RoomAvailabilityStatusResponseDTO.builder()
                    .available(true)
                    .roomTypeId(roomTypeId)
                    .roomTypeName(roomTypeName)
                    .propertyId(request.getPropertyId())
                    .availableRoomIds(availableRoomIds)
                    .availableRoomCount(availableRoomCount)
                    .requestedRoomCount(request.getRoomCount())
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .totalGuests(totalGuests)
                    .guestBreakdown(request.getGuestCount())
                    .totalPrice(totalPrice)
                    .dailyPrices(buildDailyPricesList(dailyPrices))
                    .surcharge(surcharge)
                    .surchargeAmount(surchargeAmount)
                    .fees(fees)
                    .tax(tax)
                    .taxAmount(taxAmount)
                    .totalWithSurchargeAndFees(totalWithSurchargeAndFees)
                    .finalTotal(finalTotal)
                    .occupancyTax(taxAmount)
                    .resortFee(resortFee)
                    .build();
            
        } catch (Exception e) {
            log.error("Error checking room availability status: {}", e.getMessage(), e);
            return buildUnavailableResponse(request);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public RoomAvailabilityStatusResponseDTO checkRoomAvailabilityStatusWithPromotion(
            RoomAvailabilityStatusRequestDTO request, Integer promotionId) {
        log.info("Checking room availability status with promotion ID {} for propertyId={}, roomTypeId={}, startDate={}, endDate={}, " +
                "guests={}, roomCount={}, bedCount={}", 
                promotionId, request.getPropertyId(), request.getRoomTypeId(), request.getStartDate(), 
                request.getEndDate(), request.getGuests(), request.getRoomCount(), request.getBedCount());
        
        try {
            // Step 1: Check room availability
            Map<String, Object> availabilityResult = availabilityCheckService.checkRoomTypeAvailability(
                    request.getPropertyId(),
                    request.getRoomTypeId(),
                    request.getStartDate(),
                    request.getEndDate(),
                    request.getRoomCount());
            
            if (availabilityResult.isEmpty()) {
                log.info("No available rooms found for the criteria");
                return buildUnavailableResponse(request);
            }
            
            // Extract room type details and available room IDs
            Integer roomTypeId = (Integer) availabilityResult.get("roomTypeId");
            String roomTypeName = (String) availabilityResult.get("roomTypeName");
            Integer maxCapacity = (Integer) availabilityResult.get("maxCapacity");
            Integer singleBedCount = (Integer) availabilityResult.get("singleBedCount");
            Integer doubleBedCount = (Integer) availabilityResult.get("doubleBedCount");
            Integer totalBedCount = singleBedCount + doubleBedCount;
            @SuppressWarnings("unchecked")
            List<Integer> availableRoomIds = (List<Integer>) availabilityResult.get("availableRoomIds");
            Integer availableRoomCount = (Integer) availabilityResult.get("availableRoomCount");
            
            // Validate bed count if specified
            if (request.getBedCount() != null && totalBedCount < request.getBedCount()) {
                log.info("Room type has insufficient beds. Required: {}, Available: {}", 
                        request.getBedCount(), totalBedCount);
                return buildUnavailableResponse(request);
            }
            
            // Calculate total guests
            int totalGuests = request.getGuests() != null ? request.getGuests() : 
                    request.getGuestCount() != null ? 
                            request.getGuestCount().values().stream().mapToInt(Integer::intValue).sum() : 0;
            
            // Check if capacity is sufficient
            int requiredCapacityPerRoom = (int) Math.ceil((double) totalGuests / request.getRoomCount());
            if (maxCapacity < requiredCapacityPerRoom) {
                log.info("Room type has insufficient capacity. Required: {}, Available: {}", 
                        requiredCapacityPerRoom, maxCapacity);
                return buildUnavailableResponse(request);
            }
            
            // Step 2: Get daily prices
            Map<LocalDate, Double> dailyPrices = dailyRatesService.fetchDailyRoomTypePrices(
                    request.getRoomTypeId(),
                    request.getStartDate(),
                    request.getEndDate());
            
            if (dailyPrices.isEmpty()) {
                log.warn("No pricing information available for roomTypeId={} and date range", 
                        request.getRoomTypeId());
                return buildUnavailableResponse(request);
            }
            
            // Calculate total price
            double totalPrice = dailyRatesService.calculateTotalPrice(dailyPrices, request.getRoomCount());
            BigDecimal baseAmount = BigDecimal.valueOf(totalPrice).setScale(2, RoundingMode.HALF_UP);
            
            // Step 3: Apply promotion
            BigDecimal priceFactor = BigDecimal.ONE;
            String promoTitle = "";
            String promoDescription = "";
            
            // Check if promotion is from GraphQL or RDS based on the ID
            boolean isGraphQLPromotion = false;
            boolean isRDSPromotion = false;
            Integer originalPromotionId = 0;
            
            if (promotionId != null) {
                // Check promotion ID format
                int remainder = promotionId % 1000;
                originalPromotionId = promotionId / 1000;
                
                if (remainder == 1) {
                    // This is a GraphQL promotion (ends with 001)
                    isGraphQLPromotion = true;
                    log.info("Processing GraphQL promotion with ID: {}, original ID: {}", promotionId, originalPromotionId);
                    
                    try {
                        // Call GraphQL API to get promotion details
                        Map<String, Object> graphQLPromotion = promotionGraphQLService.fetchPromotion(originalPromotionId);
                        
                        if (graphQLPromotion != null && !graphQLPromotion.isEmpty()) {
                            priceFactor = new BigDecimal(graphQLPromotion.get("price_factor").toString());
                            promoTitle = graphQLPromotion.get("promotion_title").toString();
                            promoDescription = graphQLPromotion.get("promotion_description").toString();
                            log.info("Found GraphQL promotion: {}, price factor: {}", promoTitle, priceFactor);
                        } else {
                            log.warn("GraphQL promotion not found with ID: {}", originalPromotionId);
                        }
                    } catch (Exception e) {
                        log.error("Error fetching GraphQL promotion: {}", e.getMessage(), e);
                    }
                } else if (remainder == 2) {
                    // This is an RDS promotion (ends with 002)
                    isRDSPromotion = true;
                    log.info("Processing RDS promotion with ID: {}, original ID: {}", promotionId, originalPromotionId);
                    
                    try {
                        // Get promotion from database
                        Optional<PropertyPromotion> optionalPromotion = propertyPromotionRepository
                                .findById(Long.valueOf(originalPromotionId));
                        
                        if (optionalPromotion.isPresent()) {
                            PropertyPromotion rdsPromotion = optionalPromotion.get();
                            priceFactor = BigDecimal.valueOf(rdsPromotion.getPriceFactor());
                            promoTitle = rdsPromotion.getTitle();
                            promoDescription = rdsPromotion.getDescription();
                            log.info("Found RDS promotion: {}, price factor: {}", promoTitle, priceFactor);
                        } else {
                            log.warn("RDS promotion not found with ID: {}", originalPromotionId);
                        }
                    } catch (Exception e) {
                        log.error("Error fetching RDS promotion: {}", e.getMessage(), e);
                    }
                } else {
                    log.warn("Invalid promotion ID format: {}", promotionId);
                }
            }
            
            // Apply promotion discount to base amount
            BigDecimal discountedBaseAmount = baseAmount.multiply(priceFactor).setScale(2, RoundingMode.HALF_UP);
            BigDecimal promoDiscount = baseAmount.subtract(discountedBaseAmount).setScale(2, RoundingMode.HALF_UP);
            
            // Check if this is an upfront payment promotion (ID 5001)
            boolean isUpfrontPaymentPromotion = promotionId != null && promotionId == 5001;
            if (isUpfrontPaymentPromotion) {
                promoTitle = "Upfront payment discount";
                promoDescription = "Pay 100% while booking and get a discount of 10%";
                log.info("Special handling for upfront payment promotion (ID: 5001)");
            }
            
            log.info("Applied promotion discount: original price: {}, price factor: {}, " +
                    "discounted price: {}, discount amount: {}", 
                    baseAmount, priceFactor, discountedBaseAmount, promoDiscount);
            
            // Step 4: Get property configuration for surcharge, fees, and tax
            PropertyConfigurationDTO.Response propertyConfig = null;
            BigDecimal surcharge = BigDecimal.ZERO;
            BigDecimal surchargeAmount = BigDecimal.ZERO;
            BigDecimal fees = BigDecimal.ZERO;
            BigDecimal tax = BigDecimal.ZERO;
            BigDecimal taxAmount = BigDecimal.ZERO;
            BigDecimal totalWithSurchargeAndFees = discountedBaseAmount;
            BigDecimal finalTotal = discountedBaseAmount;
            BigDecimal resortFee = BigDecimal.ZERO;
            
            try {
                propertyConfig = propertyConfigurationService.getPropertyConfigurationByPropertyId(request.getPropertyId());
                
                if (propertyConfig != null) {
                    // 1. Get configuration values
                    surcharge = propertyConfig.getSurcharge() != null ? propertyConfig.getSurcharge() : BigDecimal.ZERO;
                    fees = propertyConfig.getFees() != null ? propertyConfig.getFees() : BigDecimal.ZERO;
                    tax = propertyConfig.getTax() != null ? propertyConfig.getTax() : BigDecimal.ZERO;
                    
                    // 2. Order of calculation:
                    // Step 1: Calculate surcharge (percentage of discounted base amount)
                    surchargeAmount = discountedBaseAmount
                            .multiply(surcharge)
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    
                    // Step 2: Add fixed fees
                    totalWithSurchargeAndFees = discountedBaseAmount
                            .add(surchargeAmount)
                            .add(fees);
                    
                    // Step 3: Calculate tax on the total with surcharge and fees
                    taxAmount = totalWithSurchargeAndFees
                            .multiply(tax)
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    
                    // Calculate final total
                    finalTotal = totalWithSurchargeAndFees.add(taxAmount);
                    
                    // Calculate resort fee (combined surcharge and fees)
                    resortFee = surchargeAmount.add(fees);
                    
                    log.info("Price calculation with promotion: Base amount: {}, Discounted amount: {}, " +
                            "Promotion discount: {}, Surcharge({}%): {}, Fees: {}, " +
                            "Subtotal: {}, Tax({}%): {}, Final Total: {}", 
                            baseAmount, discountedBaseAmount, promoDiscount, surcharge, surchargeAmount, fees, 
                            totalWithSurchargeAndFees, tax, taxAmount, finalTotal);
                }
            } catch (ResourceNotFoundException e) {
                log.warn("Property configuration not found for propertyId={}. Using default values.", 
                        request.getPropertyId());
            }
            
            // Calculate payment amounts (due now and due at resort)
            BigDecimal dueNow;
            BigDecimal dueAtResort;
            
            if (isUpfrontPaymentPromotion) {
                // For upfront payment promotion, 100% is due now
                dueNow = finalTotal;
                dueAtResort = BigDecimal.ZERO;
                log.info("Upfront payment required: dueNow={}, dueAtResort={}", dueNow, dueAtResort);
            } else {
                // For regular promotions, 5% is due now, rest at resort
                dueNow = finalTotal.multiply(new BigDecimal("0.05")).setScale(2, RoundingMode.HALF_UP);
                dueAtResort = finalTotal.subtract(dueNow).setScale(2, RoundingMode.HALF_UP);
                log.info("Regular payment schedule: dueNow={} (5%), dueAtResort={} (95%)", dueNow, dueAtResort);
            }
            
            // Build response
            return RoomAvailabilityStatusResponseDTO.builder()
                    .available(true)
                    .roomTypeId(roomTypeId)
                    .roomTypeName(roomTypeName)
                    .propertyId(request.getPropertyId())
                    .availableRoomIds(availableRoomIds)
                    .availableRoomCount(availableRoomCount)
                    .requestedRoomCount(request.getRoomCount())
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .totalGuests(totalGuests)
                    .guestBreakdown(request.getGuestCount())
                    .totalPrice(totalPrice)
                    .dailyPrices(buildDailyPricesList(dailyPrices))
                    .promotionId(promotionId)
                    .promotionTitle(promoTitle)
                    .promotionDescription(promoDescription)
                    .priceFactor(priceFactor.doubleValue())
                    .discountedBaseAmount(discountedBaseAmount)
                    .promoDiscount(promoDiscount)
                    .surcharge(surcharge)
                    .surchargeAmount(surchargeAmount)
                    .fees(fees)
                    .tax(tax)
                    .taxAmount(taxAmount)
                    .totalWithSurchargeAndFees(totalWithSurchargeAndFees)
                    .finalTotal(finalTotal)
                    .occupancyTax(taxAmount)
                    .resortFee(resortFee)
                    .dueNow(dueNow)
                    .dueAtResort(dueAtResort)
                    .build();
            
        } catch (Exception e) {
            log.error("Error checking room availability status with promotion: {}", e.getMessage(), e);
            return buildUnavailableResponse(request);
        }
    }
    
    /**
     * Builds a list of daily price DTOs from the price map
     */
    private List<DailyPriceDTO> buildDailyPricesList(Map<LocalDate, Double> dailyPrices) {
        return dailyPrices.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> DailyPriceDTO.builder()
                        .date(entry.getKey())
                        .price(entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }
    
    /**
     * Builds a response for unavailable rooms
     */
    private RoomAvailabilityStatusResponseDTO buildUnavailableResponse(RoomAvailabilityStatusRequestDTO request) {
        return RoomAvailabilityStatusResponseDTO.builder()
                .available(false)
                .roomTypeId(request.getRoomTypeId())
                .propertyId(request.getPropertyId())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .requestedRoomCount(request.getRoomCount())
                .availableRoomCount(0)
                .build();
    }
} 