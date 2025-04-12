package com.kdu.rizzlers.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kdu.rizzlers.dto.RoomBookingDTO;
import com.kdu.rizzlers.service.RoomBookingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of RoomBookingService that fetches data from GraphQL
 */
@Service
@Slf4j
public class RoomBookingServiceImpl implements RoomBookingService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${graphql.endpoint}")
    private String graphqlEndpoint;
    
    @Value("${graphql.api-key}")
    private String apiKey;
    
    @Value("${graphql.api-key-header}")
    private String apiKeyHeader;
    
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'00:00:00.000'Z'");

    public RoomBookingServiceImpl(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public List<RoomBookingDTO> getRoomsWithCheckInOnDate(Integer propertyId, LocalDate date) {
        String formattedDate = date.format(ISO_FORMATTER);
        String query = String.format(
            "query MyQuery {" +
            "  listBookings(" +
            "    where: {status_id: {equals: 1}, property_id: {equals: %d}, check_in_date: {equals: \"%s\"}}" +
            "    take: 1000000" +
            "  ) {" +
            "    check_in_date" +
            "    status_id" +
            "    room_booked {" +
            "      room {" +
            "        room_number" +
            "        room_id" +
            "      }" +
            "    }" +
            "  }" +
            "}", propertyId, formattedDate);
        
        JsonNode responseData = executeGraphQLQuery(query);
        return parseCheckInRooms(responseData, date);
    }
    
    @Override
    public List<RoomBookingDTO> getRoomsWithCheckOutOnDate(Integer propertyId, LocalDate date) {
        String formattedDate = date.format(ISO_FORMATTER);
        String query = String.format(
            "query MyQuery {" +
            "  listBookings(" +
            "    where: {status_id: {equals: 1}, property_id: {equals: %d}, check_out_date: {equals: \"%s\"}}" +
            "    take: 1000000" +
            "  ) {" +
            "    status_id" +
            "    room_booked {" +
            "      room {" +
            "        room_number" +
            "        room_id" +
            "      }" +
            "    }" +
            "    check_out_date" +
            "  }" +
            "}", propertyId, formattedDate);
        
        JsonNode responseData = executeGraphQLQuery(query);
        return parseCheckOutRooms(responseData, date);
    }
    
    @Override
    public List<RoomBookingDTO> getOccupiedRooms(Integer propertyId, LocalDate date) {
        String formattedDate = date.format(ISO_FORMATTER);
        
        // Query for rooms where: 
        // 1. check-in date <= current date
        // 2. check-out date > current date
        // This gets rooms that are occupied specifically on the target date
        String query = String.format(
            "query MyQuery {" +
            "  listBookings(" +
            "    where: {" +
            "      status_id: {equals: 1}, " +
            "      property_id: {equals: %d}, " +
            "      check_in_date: {lte: \"%s\"}, " +
            "      check_out_date: {gt: \"%s\"}" +
            "    }" +
            "    take: 1000000" +
            "  ) {" +
            "    check_in_date" +
            "    check_out_date" +
            "    status_id" +
            "    room_booked {" +
            "      room {" +
            "        room_number" +
            "        room_id" +
            "      }" +
            "    }" +
            "  }" +
            "}", propertyId, formattedDate, formattedDate);
        
        JsonNode responseData = executeGraphQLQuery(query);
        return parseOccupiedRooms(responseData, date);
    }
    
    private JsonNode executeGraphQLQuery(String query) {
        try {
            // Set up headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(apiKeyHeader, apiKey);
            
            // Create the request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("query", query);
            
            log.debug("Executing GraphQL query: {}", query);
            
            // Create the request entity
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
            
            // Execute the request
            String response = restTemplate.postForObject(graphqlEndpoint, requestEntity, String.class);
            
            if (response != null) {
                JsonNode jsonNode = objectMapper.readTree(response);
                log.debug("GraphQL response: {}", jsonNode);
                return jsonNode.path("data");
            }
            
            return objectMapper.createObjectNode();
        } catch (Exception e) {
            log.error("Error executing GraphQL query: {}", e.getMessage(), e);
            return objectMapper.createObjectNode();
        }
    }
    
    private List<RoomBookingDTO> parseCheckInRooms(JsonNode data, LocalDate checkInDate) {
        List<RoomBookingDTO> results = new ArrayList<>();
        
        if (data.has("listBookings") && !data.path("listBookings").isEmpty()) {
            JsonNode bookings = data.path("listBookings");
            
            for (JsonNode booking : bookings) {
                if (booking.has("room_booked") && !booking.path("room_booked").isEmpty()) {
                    for (JsonNode roomBooked : booking.path("room_booked")) {
                        JsonNode room = roomBooked.path("room");
                        
                        RoomBookingDTO roomBookingDTO = RoomBookingDTO.builder()
                            .roomId(room.path("room_id").asInt())
                            .roomNumber(room.path("room_number").asText())
                            .checkInDate(checkInDate)
                            .isOccupied(false) // Will be occupied after check-in
                            .build();
                        
                        results.add(roomBookingDTO);
                    }
                }
            }
        }
        
        return results;
    }
    
    private List<RoomBookingDTO> parseCheckOutRooms(JsonNode data, LocalDate checkOutDate) {
        List<RoomBookingDTO> results = new ArrayList<>();
        
        if (data.has("listBookings") && !data.path("listBookings").isEmpty()) {
            JsonNode bookings = data.path("listBookings");
            
            for (JsonNode booking : bookings) {
                if (booking.has("room_booked") && !booking.path("room_booked").isEmpty()) {
                    for (JsonNode roomBooked : booking.path("room_booked")) {
                        JsonNode room = roomBooked.path("room");
                        
                        RoomBookingDTO roomBookingDTO = RoomBookingDTO.builder()
                            .roomId(room.path("room_id").asInt())
                            .roomNumber(room.path("room_number").asText())
                            .checkOutDate(checkOutDate)
                            .isOccupied(true) // Currently occupied, will be vacated
                            .build();
                        
                        results.add(roomBookingDTO);
                    }
                }
            }
        }
        
        return results;
    }
    
    private List<RoomBookingDTO> parseOccupiedRooms(JsonNode data, LocalDate targetDate) {
        // Use a Map to deduplicate rooms by roomId
        Map<Integer, RoomBookingDTO> uniqueRooms = new HashMap<>();
        
        if (data.has("listBookings") && !data.path("listBookings").isEmpty()) {
            JsonNode bookings = data.path("listBookings");
            
            for (JsonNode booking : bookings) {
                // Parse check-in and check-out dates
                LocalDate checkInDate = LocalDate.parse(
                    booking.path("check_in_date").asText().substring(0, 10));
                LocalDate checkOutDate = LocalDate.parse(
                    booking.path("check_out_date").asText().substring(0, 10));
                
                // Verify the room is actually occupied on the target date
                if (checkInDate.compareTo(targetDate) <= 0 && checkOutDate.compareTo(targetDate) > 0) {
                    if (booking.has("room_booked") && !booking.path("room_booked").isEmpty()) {
                        for (JsonNode roomBooked : booking.path("room_booked")) {
                            JsonNode room = roomBooked.path("room");
                            Integer roomId = room.path("room_id").asInt();
                            
                            // Only add if we haven't seen this room ID before
                            if (!uniqueRooms.containsKey(roomId)) {
                                RoomBookingDTO roomBookingDTO = RoomBookingDTO.builder()
                                    .roomId(roomId)
                                    .roomNumber(room.path("room_number").asText())
                                    .checkInDate(checkInDate)
                                    .checkOutDate(checkOutDate)
                                    .isOccupied(true)
                                    .build();
                                
                                uniqueRooms.put(roomId, roomBookingDTO);
                            }
                        }
                    }
                }
            }
        }
        
        // Return the deduplicated list of rooms
        return new ArrayList<>(uniqueRooms.values());
    }
    
    // Overloaded method for backward compatibility
    private List<RoomBookingDTO> parseOccupiedRooms(JsonNode data) {
        // We no longer use this method, but keep it for compatibility
        throw new UnsupportedOperationException("This method is deprecated. Use parseOccupiedRooms(JsonNode, LocalDate) instead.");
    }
} 