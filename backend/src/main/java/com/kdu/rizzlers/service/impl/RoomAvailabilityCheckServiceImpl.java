package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.service.RoomAvailabilityCheckService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the RoomAvailabilityCheckService
 */
@Slf4j
@Service
public class RoomAvailabilityCheckServiceImpl implements RoomAvailabilityCheckService {

    private final WebClient webClient;
    private final HttpGraphQlClient graphQlClient;

    public RoomAvailabilityCheckServiceImpl(
            WebClient.Builder webClientBuilder,
            @Value("${graphql.endpoint}") String graphqlEndpoint,
            @Value("${graphql.api-key}") String apiKey,
            @Value("${graphql.api-key-header}") String apiKeyHeader) {
        
        this.webClient = webClientBuilder
            .baseUrl(graphqlEndpoint)
            .defaultHeader(apiKeyHeader, apiKey)
            .build();
            
        this.graphQlClient = HttpGraphQlClient.builder(webClient)
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> checkRoomTypeAvailability(
            Integer propertyId, 
            Integer roomTypeId, 
            LocalDate startDate, 
            LocalDate endDate,
            Integer requiredRoomCount) {
        
        log.info("Checking availability for roomTypeId={}, propertyId={}, startDate={}, endDate={}, roomCount={}",
                roomTypeId, propertyId, startDate, endDate, requiredRoomCount);
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Step 1: Get room details
            List<Map<String, Object>> rooms = fetchRoomsByTypeAndProperty(propertyId, roomTypeId);
            
            if (rooms.isEmpty()) {
                log.info("No rooms found for propertyId={} and roomTypeId={}", propertyId, roomTypeId);
                return Collections.emptyMap();
            }
            
            // Get room type details from the first room (they all have the same type)
            Map<String, Object> firstRoom = rooms.get(0);
            Map<String, Object> roomType = (Map<String, Object>) firstRoom.get("room_type");
            
            // Extract room IDs
            List<Integer> allRoomIds = rooms.stream()
                    .map(room -> (Integer) room.get("room_id"))
                    .collect(Collectors.toList());
            
            log.info("Found {} rooms of type {} in property {}", 
                    allRoomIds.size(), roomTypeId, propertyId);
            
            // Step 2: Check availability for these room IDs
            Set<Integer> availableRoomIds = fetchAvailableRoomIds(
                    propertyId,
                    allRoomIds,
                    startDate,
                    endDate);
            
            if (availableRoomIds.isEmpty() || availableRoomIds.size() < requiredRoomCount) {
                log.info("Insufficient rooms available. Required: {}, Available: {}", 
                        requiredRoomCount, availableRoomIds.size());
                return Collections.emptyMap();
            }
            
            // Populate the result
            result.put("roomTypeId", roomTypeId);
            result.put("roomTypeName", roomType.get("room_type_name"));
            result.put("maxCapacity", roomType.get("max_capacity"));
            result.put("singleBedCount", roomType.get("single_bed"));
            result.put("doubleBedCount", roomType.get("double_bed"));
            result.put("areaInSquareFeet", roomType.get("area_in_square_feet"));
            result.put("availableRoomIds", new ArrayList<>(availableRoomIds));
            result.put("availableRoomCount", availableRoomIds.size());
            
            return result;
            
        } catch (Exception e) {
            log.error("Error checking room type availability: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }
    
    @Override
    public List<Map<String, Object>> fetchRoomsByTypeAndProperty(
            Integer propertyId, 
            Integer roomTypeId) {
        
        final String roomsQuery = """
            query getRooms($propertyId: Int!, $roomTypeId: Int!) {
              listRooms(
                where: {
                  property_id: {equals: $propertyId},
                  room_type_id: {equals: $roomTypeId}
                }
                take: 1000000
              ) {
                room_id
                room_number
                room_type {
                  room_type_id
                  room_type_name
                  max_capacity
                  area_in_square_feet
                  single_bed
                  double_bed
                }
              }
            }
        """;
        
        try {
            List<Map<String, Object>> rooms = graphQlClient.document(roomsQuery)
                .variable("propertyId", propertyId)
                .variable("roomTypeId", roomTypeId)
                .retrieve("listRooms")
                .toEntity(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .onErrorResume(e -> {
                    log.error("Error fetching rooms: {}", e.getMessage(), e);
                    return Mono.just(new ArrayList<>());
                })
                .block();
                
            return rooms != null ? rooms : new ArrayList<>();
        } catch (Exception e) {
            log.error("Error fetching rooms: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public Set<Integer> fetchAvailableRoomIds(
            Integer propertyId,
            List<Integer> roomIds,
            LocalDate startDate,
            LocalDate endDate) {
            
        // Format dates as AWSDateTime (ISO-8601 with timezone)
        final String startDateStr = startDate.atStartOfDay(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
        final String endDateStr = endDate.atTime(23, 59, 59).atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
        
        log.info("Checking availability for {} rooms from {} to {}", roomIds.size(), startDateStr, endDateStr);
        
        if (roomIds.isEmpty()) {
            return Collections.emptySet();
        }
        
        final String availabilityQuery = """
            query getAvailableRooms($propertyId: Int!, $roomIds: [Int!]!, $startDate: AWSDateTime!, $endDate: AWSDateTime!) {
              listRoomAvailabilities(
                where: {
                  property_id: {equals: $propertyId}, 
                  date: {gte: $startDate, lte: $endDate},
                  room_id: {in: $roomIds},
                  booking: {
                    booking_status: {
                      status: {not: {equals: "BOOKED"}},
                      is_deactivated: {equals: false}
                    }
                  }
                }
                take: 200000
              ) {
                booking_id
                property_id
                date
                room_id
                room {
                  room_id
                  room_type_id
                  room_type {
                    room_type_name
                    room_type_id
                  }
                }
              }
            }
        """;
        
        try {
            List<Map<String, Object>> availabilityList = graphQlClient.document(availabilityQuery)
                .variable("propertyId", propertyId)
                .variable("roomIds", roomIds)
                .variable("startDate", startDateStr)
                .variable("endDate", endDateStr)
                .retrieve("listRoomAvailabilities")
                .toEntity(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .onErrorResume(e -> {
                    log.error("Error fetching room availabilities: {}", e.getMessage(), e);
                    return Mono.just(new ArrayList<>());
                })
                .block();
                
            if (availabilityList == null || availabilityList.isEmpty()) {
                log.info("No availability records found for the specified criteria");
                return Collections.emptySet();
            }
            
            log.info("Found {} availability records for the date range", availabilityList.size());
            
            // Group by room ID to check if each room is available for the entire period
            Map<Integer, Set<String>> roomAvailabilityDates = new HashMap<>();
            
            for (Map<String, Object> availability : availabilityList) {
                Integer roomId = (Integer) availability.get("room_id");
                String date = (String) availability.get("date");
                
                // Add to date tracking for availability check
                roomAvailabilityDates.computeIfAbsent(roomId, k -> new HashSet<>()).add(date);
            }
            
            // Calculate the expected number of days in the date range
            long totalDays = endDate.toEpochDay() - startDate.toEpochDay() + 1;
            log.info("Expected availability records per room for full coverage: {}", totalDays);
            
            // Find rooms that are available for the entire period
            Set<Integer> availableRoomIds = roomAvailabilityDates.entrySet().stream()
                .filter(entry -> entry.getValue().size() >= totalDays)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
                
            log.info("Found {} fully available rooms during the requested period", availableRoomIds.size());
            return availableRoomIds;
            
        } catch (Exception e) {
            log.error("Error processing availabilities: {}", e.getMessage(), e);
            return Collections.emptySet();
        }
    }
} 