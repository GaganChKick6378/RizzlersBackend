package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.service.RoomDailyRatesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.*;

/**
 * Implementation of the RoomDailyRatesService
 */
@Slf4j
@Service
public class RoomDailyRatesServiceImpl implements RoomDailyRatesService {

    private final HttpGraphQlClient graphQlClient;

    public RoomDailyRatesServiceImpl(
            WebClient.Builder webClientBuilder,
            @Value("${graphql.endpoint}") String graphqlEndpoint,
            @Value("${graphql.api-key}") String apiKey,
            @Value("${graphql.api-key-header}") String apiKeyHeader) {
        
        WebClient webClient = webClientBuilder
            .baseUrl(graphqlEndpoint)
            .defaultHeader(apiKeyHeader, apiKey)
            .build();
            
        this.graphQlClient = HttpGraphQlClient.builder(webClient)
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<LocalDate, Double> fetchDailyRoomTypePrices(
            Integer roomTypeId,
            LocalDate startDate,
            LocalDate endDate) {
            
        final String ratesQuery = """
            query getRoomRates($roomTypeId: Int!) {
              listRoomRateRoomTypeMappings(
                where: {room_type_id: {equals: $roomTypeId}}
                orderBy: {room_rate: {date: ASC}}
                take: 1000
              ) {
                room_type_id
                room_rate {
                  basic_nightly_rate
                  date
                  room_rate_id
                }
              }
            }
        """;
        
        try {
            List<Map<String, Object>> mappingsList = graphQlClient.document(ratesQuery)
                .variable("roomTypeId", roomTypeId)
                .retrieve("listRoomRateRoomTypeMappings")
                .toEntity(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .onErrorResume(e -> {
                    log.error("Error fetching room rates: {}", e.getMessage(), e);
                    return Mono.just(new ArrayList<>());
                })
                .block();
                
            Map<LocalDate, Double> dailyPrices = new LinkedHashMap<>();
            
            if (mappingsList != null && !mappingsList.isEmpty()) {
                // First pass - get prices for specific dates
                for (Map<String, Object> mapping : mappingsList) {
                    Map<String, Object> roomRate = (Map<String, Object>) mapping.get("room_rate");
                    
                    if (roomRate != null) {
                        String dateStr = (String) roomRate.get("date");
                        Double rate = ((Number) roomRate.get("basic_nightly_rate")).doubleValue();
                        
                        // Parse date and check if it's in our range
                        try {
                            LocalDate rateDate;
                            if (dateStr.contains("T")) {
                                rateDate = LocalDate.parse(dateStr.substring(0, 10));
                            } else {
                                rateDate = LocalDate.parse(dateStr);
                            }
                            
                            // Only include rates within our range
                            if (!rateDate.isBefore(startDate) && rateDate.isBefore(endDate)) {
                                dailyPrices.put(rateDate, rate);
                            }
                        } catch (Exception e) {
                            log.warn("Could not parse date: {}", dateStr);
                        }
                    }
                }
            }
            
            // Check if we have prices for all days in the range
            LocalDate currentDate = startDate;
            // Get average price to use as fallback
            double averagePrice = !dailyPrices.isEmpty() 
                ? dailyPrices.values().stream().mapToDouble(Double::doubleValue).average().getAsDouble()
                : 0.0;
                
            // Fill in any missing dates with the average price
            while (currentDate.isBefore(endDate)) {
                if (!dailyPrices.containsKey(currentDate)) {
                    dailyPrices.put(currentDate, averagePrice);
                }
                currentDate = currentDate.plusDays(1);
            }
            
            return dailyPrices;
            
        } catch (Exception e) {
            log.error("Error fetching room rates: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }
    
    @Override
    public double calculateTotalPrice(Map<LocalDate, Double> dailyPrices, int roomCount) {
        // Calculate the sum of daily prices for a single room
        double pricePerRoom = dailyPrices.values().stream().mapToDouble(Double::doubleValue).sum();
        // Multiply by the number of rooms to get the total price
        return pricePerRoom * roomCount;
    }
} 