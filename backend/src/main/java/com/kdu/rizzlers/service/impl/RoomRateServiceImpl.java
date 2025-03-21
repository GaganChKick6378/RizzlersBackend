package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.dto.out.DailyRoomRateDTO;
import com.kdu.rizzlers.entity.PropertyPromotionSchedule;
import com.kdu.rizzlers.repository.PropertyPromotionScheduleRepository;
import com.kdu.rizzlers.service.RoomRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomRateServiceImpl implements RoomRateService {
    private static final DateTimeFormatter ISO_DATE_FORMATTER = DateTimeFormatter.ISO_INSTANT;
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2);
    
    private final WebClient.Builder webClientBuilder;
    private final PropertyPromotionScheduleRepository propertyPromotionScheduleRepository;
    
    @Value("${graphql.endpoint}")
    private String graphqlEndpoint;
    
    @Value("${graphql.api-key-header}")
    private String apiKeyHeader;
    
    @Value("${graphql.api-key}")
    private String apiKey;
    
    @Override
    @Transactional(readOnly = true)
    public List<PropertyPromotionSchedule> getActivePromotions(Integer propertyId, LocalDate startDate, LocalDate endDate) {
        return propertyPromotionScheduleRepository.findActivePromotionsForPropertyInPeriod(
                propertyId, startDate, endDate);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<PropertyPromotionSchedule> getAllPromotions(Integer propertyId) {
        return propertyPromotionScheduleRepository.findAllByPropertyId(propertyId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<DailyRoomRateDTO> getDailyRatesWithPromotions(Integer tenantId, Integer propertyId) {
        log.info("Fetching ALL daily rates with promotions for tenantId={}, propertyId={}", 
                tenantId, propertyId);
        
        // STEP 1: Get ALL room types and their rates from GraphQL directly
        // This approach uses a more efficient query structure
        Map<LocalDate, Double> allRates = fetchMinimumRoomRates(propertyId);
        
        if (allRates.isEmpty()) {
            log.info("No rates found for property {}", propertyId);
            return Collections.emptyList();
        }
        
        log.info("Fetched {} dates with rates for property {}", allRates.size(), propertyId);
        
        // STEP 2: Find min and max dates in the data to get all applicable promotions
        LocalDate minDate = allRates.keySet().stream().min(LocalDate::compareTo).orElse(LocalDate.now());
        LocalDate maxDate = allRates.keySet().stream().max(LocalDate::compareTo).orElse(LocalDate.now());
        log.info("Date range in fetched data: {} to {}", minDate, maxDate);
        
        // STEP 3: Get ALL promotions from the property_promotion_schedule table
        List<PropertyPromotionSchedule> promotions = getAllPromotions(propertyId);
        log.info("Found {} promotions for property {}", promotions.size(), propertyId);
        
        // STEP 4: Create a map of date -> promotion for faster lookups
        Map<LocalDate, PropertyPromotionSchedule> dateToPromotionMap = new HashMap<>();
        
        // For each date, find applicable promotions
        allRates.keySet().forEach(date -> {
            LocalDate finalDate = date; // Need final variable for lambda
            promotions.stream()
                .filter(promotion -> promotion.isDateInPromotionPeriod(finalDate))
                .findFirst()
                .ifPresent(promotion -> dateToPromotionMap.put(finalDate, promotion));
        });
        
        // STEP 5: Create DTOs with rate and promotion info for ALL dates
        List<DailyRoomRateDTO> result = allRates.entrySet().stream()
            .map(entry -> {
                LocalDate date = entry.getKey();
                Double minRate = entry.getValue();
                PropertyPromotionSchedule promotion = dateToPromotionMap.get(date);
                
                // Default values
                boolean hasPromotion = false;
                Integer promotionId = null;
                BigDecimal priceFactor = null;
                Double discountedRate = minRate;
                
                // Apply promotion if exists
                if (promotion != null) {
                    hasPromotion = true;
                    promotionId = promotion.getPromotionId();
                    priceFactor = promotion.getPriceFactor();
                    
                    // Calculate discounted rate using the price factor from property_promotion_schedule
                    discountedRate = BigDecimal.valueOf(minRate)
                        .multiply(priceFactor)
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue();
                } else {
                    // Use the database default (1.0) for price factor if no promotion
                    priceFactor = BigDecimal.ONE;
                }
                
                // Create and return the DTO
                return DailyRoomRateDTO.builder()
                    .date(date)
                    .minimumRate(minRate)
                    .hasPromotion(hasPromotion)
                    .promotionId(promotionId)
                    .priceFactor(priceFactor.doubleValue())
                    .discountedRate(discountedRate)
                    .build();
            })
            .sorted(Comparator.comparing(DailyRoomRateDTO::getDate))
            .collect(Collectors.toList());
        
        log.info("Returning {} daily rates with promotions", result.size());
        if (!result.isEmpty()) {
            LocalDate firstDate = result.get(0).getDate();
            LocalDate lastDate = result.get(result.size() - 1).getDate();
            log.info("Date range in final response: {} to {}", firstDate, lastDate);
        }
        
        return result;
    }

    /**
     * Fetches minimum room rates for all dates directly using a more efficient query
     * Based on the reference implementation and using document() method for GraphQL
     */
    private Map<LocalDate, Double> fetchMinimumRoomRates(Integer propertyId) {
        // Create a GraphQL client
        HttpGraphQlClient graphQlClient = createGraphQlClient();
        
        try {
            // 1. First fetch available room types for the property
            String roomQuery = """
                query getAvailableRooms($propertyId: Int!) {
                  listRooms(
                    where: {
                      property_id: {equals: $propertyId}
                    }
                    take: 1000
                  ) {
                    room_id
                    room_type {
                      room_type_id
                      room_type_name
                    }
                  }
                }
            """;
            
            // Get all room type IDs
            List<Integer> roomTypeIds = graphQlClient.document(roomQuery)
                .variable("propertyId", propertyId)
                .retrieve("listRooms")
                .toEntity(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .map(rooms -> {
                    log.debug("GraphQL rooms response size: {}", rooms.size());
                    Set<Integer> typeIds = new HashSet<>();
                    
                    try {
                        if (rooms != null) {
                            for (Map<String, Object> room : rooms) {
                                if (room.containsKey("room_type")) {
                                    Map<String, Object> roomType = (Map<String, Object>) room.get("room_type");
                                    
                                    if (roomType != null && roomType.containsKey("room_type_id")) {
                                        Integer roomTypeId = ((Number) roomType.get("room_type_id")).intValue();
                                        typeIds.add(roomTypeId);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("Error parsing GraphQL rooms response: {}", e.getMessage(), e);
                    }
                    
                    log.info("Found {} room type IDs for property {}", typeIds.size(), propertyId);
                    return new ArrayList<>(typeIds);
                })
                .onErrorResume(e -> {
                    log.error("Error fetching rooms: {}", e.getMessage(), e);
                    return Mono.just(new ArrayList<>());
                })
                .block();
                
            if (roomTypeIds == null || roomTypeIds.isEmpty()) {
                log.info("No room types found for property {}", propertyId);
                return Collections.emptyMap();
            }
            
            // 2. Now fetch all rates for these room types
            String ratesQuery = """
                query getRoomRates($roomTypeIds: [Int!]!) {
                  listRoomRateRoomTypeMappings(
                    where: {room_type_id: {in: $roomTypeIds}}
                    orderBy: {room_rate: {date: ASC}}
                    take: 1000
                  ) {
                    room_rate {
                      basic_nightly_rate
                      date
                      room_rate_id
                    }
                  }
                }
            """;
            
            // Get all rates for the room types
            return graphQlClient.document(ratesQuery)
                .variable("roomTypeIds", roomTypeIds)
                .retrieve("listRoomRateRoomTypeMappings")
                .toEntity(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .map(mappings -> {
                    log.debug("GraphQL rates mappings response size: {}", mappings.size());
                    Map<LocalDate, Double> ratesMap = new TreeMap<>(); // Use TreeMap for sorted dates
                    
                    try {
                        if (mappings != null) {
                            log.info("Received {} mappings from GraphQL", mappings.size());
                            
                            for (Map<String, Object> mapping : mappings) {
                                if (mapping.containsKey("room_rate")) {
                                    Map<String, Object> rate = (Map<String, Object>) mapping.get("room_rate");
                                    
                                    // Parse the date
                                    LocalDate rateDate = null;
                                    if (rate.containsKey("date")) {
                                        Object dateValue = rate.get("date");
                                        if (dateValue instanceof String) {
                                            String dateStr = (String) dateValue;
                                            // ISO format like "2025-03-03T00:00:00.000Z"
                                            try {
                                                if (dateStr.length() >= 10) {
                                                    rateDate = LocalDate.parse(dateStr.substring(0, 10));
                                                }
                                            } catch (Exception e) {
                                                log.error("Error parsing date '{}': {}", dateStr, e.getMessage());
                                            }
                                        }
                                    }
                                    
                                    // Parse the rate amount
                                    Double basicRate = null;
                                    if (rate.containsKey("basic_nightly_rate")) {
                                        Object rateValue = rate.get("basic_nightly_rate");
                                        if (rateValue instanceof Number) {
                                            basicRate = ((Number) rateValue).doubleValue();
                                        }
                                    }
                                    
                                    // Store all rates without any date filtering
                                    if (rateDate != null && basicRate != null) {
                                        final LocalDate finalRateDate = rateDate;
                                        final Double finalBasicRate = basicRate;
                                        
                                        // For each date, keep the minimum rate
                                        ratesMap.compute(finalRateDate, (k, existingMin) ->
                                            existingMin == null ? finalBasicRate : Math.min(existingMin, finalBasicRate));
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("Error parsing GraphQL room rates response: {}", e.getMessage(), e);
                    }
                    
                    // Log some info about the data we fetched
                    log.info("Fetched {} room rates from GraphQL", ratesMap.size());
                    
                    if (!ratesMap.isEmpty()) {
                        LocalDate minDate = ratesMap.keySet().stream().min(LocalDate::compareTo).orElse(null);
                        LocalDate maxDate = ratesMap.keySet().stream().max(LocalDate::compareTo).orElse(null);
                        log.info("Date range in fetched data: {} to {}", minDate, maxDate);
                        
                        // Sample some rates for debugging
                        log.info("Sample rates: {}", ratesMap.entrySet().stream().limit(5)
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                    }
                    
                    return ratesMap;
                })
                .onErrorResume(e -> {
                    log.error("Error fetching room rates: {}", e.getMessage(), e);
                    return Mono.just(new TreeMap<>());
                })
                .block();
            
        } catch (Exception e) {
            log.error("Exception fetching room rates: {}", e.getMessage(), e);
            return new TreeMap<>();
        }
    }

    /**
     * Creates a GraphQL client with the configured endpoint and API key
     */
    private HttpGraphQlClient createGraphQlClient() {
        WebClient webClient = webClientBuilder
            .baseUrl(graphqlEndpoint)
            .defaultHeader(apiKeyHeader, apiKey)
            .build();
            
        return HttpGraphQlClient.builder(webClient)
            .build();
    }
} 