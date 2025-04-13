package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.GuestBookingDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import jakarta.annotation.PostConstruct;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for fetching guest bookings from GraphQL
 */
@Service
@Slf4j
public class GuestBookingsGraphQLService {

    @Value("${graphql.endpoint}")
    private String graphqlEndpoint;
    
    @Value("${graphql.api-key}")
    private String apiKey;
    
    @Value("${graphql.api-key-header}")
    private String apiKeyHeader;
    
    private WebClient webClient;
    private HttpGraphQlClient graphQlClient;
    
    // Cache for booking status names to avoid redundant API calls
    private final Map<Integer, String> statusCache = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder()
                .baseUrl(graphqlEndpoint)
                .defaultHeader(apiKeyHeader, apiKey)
                .build();
                
        this.graphQlClient = HttpGraphQlClient.builder(webClient)
                .build();
        
        log.info("Initialized GraphQL client with endpoint: {}", graphqlEndpoint);
    }
    
    /**
     * Fetch property information by ID
     * 
     * @param propertyId the property ID
     * @return Optional containing the property name if found
     */
    public Optional<String> fetchPropertyName(Integer propertyId) {
        final String query = """
            query GetProperty($propertyId: Int!) {
              getProperty(where: {property_id: $propertyId}) {
                property_name
              }
            }
        """;
        
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("propertyId", propertyId);
            
            Map<String, Object> result = graphQlClient.document(query)
                .variables(variables)
                .retrieve("getProperty")
                .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
                
            if (result == null || !result.containsKey("property_name")) {
                log.warn("Property name not found for propertyId={}", propertyId);
                return Optional.empty();
            }
            
            String propertyName = (String) result.get("property_name");
            log.info("Retrieved property name '{}' for propertyId={}", propertyName, propertyId);
            return Optional.of(propertyName);
            
        } catch (WebClientResponseException e) {
            log.error("GraphQL API error when fetching property: {}", e.getResponseBodyAsString(), e);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error fetching property from GraphQL: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    /**
     * Fetch booking status by status ID
     * 
     * @param statusId the status ID
     * @return status name or default value if not found
     */
    public String fetchBookingStatus(Integer statusId) {
        // Check cache first
        if (statusCache.containsKey(statusId)) {
            return statusCache.get(statusId);
        }
        
        final String query = """
            query GetBookingStatus($statusId: Int!) {
              getBookingStatus(where: {status_id: $statusId}) {
                status
              }
            }
        """;
        
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("statusId", statusId);
            
            Map<String, Object> result = graphQlClient.document(query)
                .variables(variables)
                .retrieve("getBookingStatus")
                .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
                
            if (result == null || !result.containsKey("status")) {
                log.warn("Status not found for statusId={}", statusId);
                String defaultStatus = "UNKNOWN";
                statusCache.put(statusId, defaultStatus);
                return defaultStatus;
            }
            
            String status = (String) result.get("status");
            log.info("Retrieved status '{}' for statusId={}", status, statusId);
            
            // Store in cache
            statusCache.put(statusId, status);
            
            return status;
            
        } catch (WebClientResponseException e) {
            log.error("GraphQL API error when fetching booking status: {}", e.getResponseBodyAsString(), e);
            return "UNKNOWN";
        } catch (Exception e) {
            log.error("Error fetching booking status from GraphQL: {}", e.getMessage(), e);
            return "UNKNOWN";
        }
    }
    
    /**
     * Fetch a guest's bookings from GraphQL
     * 
     * @param guestId the guest ID
     * @return List of GuestBookingDTO objects
     */
    public List<GuestBookingDTO> fetchGuestBookings(Integer guestId) {
        final String query = """
            query GetGuestBookings($guestId: Int!) {
              listBookings(where: {guest_id: {equals: $guestId}}) {
                booking_id
                check_in_date
                check_out_date
                guest {
                  guest_name
                  guest_id
                }
                property_booked {
                  property_name
                }
                status_id
                room_booked {
                  room {
                    room_type {
                      room_type_name
                      room_type_id
                    }
                  }
                }
              }
            }
        """;
        
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("guestId", guestId);
            
            List<Map<String, Object>> result = graphQlClient.document(query)
                .variables(variables)
                .retrieve("listBookings")
                .toEntity(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .block();
                
            if (result == null || result.isEmpty()) {
                log.info("No bookings found for guestId={}", guestId);
                return new ArrayList<>();
            }
            
            log.info("Retrieved {} bookings from GraphQL for guestId={}", 
                result.size(), guestId);
            
            // Convert the GraphQL response to our DTOs
            return result.stream()
                .map(this::mapToGuestBookingDTO)
                .toList();
            
        } catch (WebClientResponseException e) {
            log.error("GraphQL API error when fetching bookings: {}", e.getResponseBodyAsString(), e);
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("Error fetching bookings from GraphQL: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Maps a GraphQL response map to a GuestBookingDTO
     */
    @SuppressWarnings("unchecked")
    private GuestBookingDTO mapToGuestBookingDTO(Map<String, Object> bookingMap) {
        // Extract guest info
        Map<String, Object> guestMap = (Map<String, Object>) bookingMap.get("guest");
        GuestBookingDTO.GuestDTO guest = GuestBookingDTO.GuestDTO.builder()
            .guestId((Integer) guestMap.get("guest_id"))
            .guestName((String) guestMap.get("guest_name"))
            .build();
        
        // Extract property info
        Map<String, Object> propertyMap = (Map<String, Object>) bookingMap.get("property_booked");
        GuestBookingDTO.PropertyDTO property = GuestBookingDTO.PropertyDTO.builder()
            .propertyName((String) propertyMap.get("property_name"))
            .build();
        
        // Extract status ID and fetch status name
        Integer statusId = (Integer) bookingMap.get("status_id");
        String statusName = statusId != null ? fetchBookingStatus(statusId) : "UNKNOWN";
        
        // Extract room type ID
        Integer roomTypeId = null;
        try {
            Map<String, Object> roomBookedMap = (Map<String, Object>) bookingMap.get("room_booked");
            if (roomBookedMap != null) {
                Map<String, Object> roomMap = (Map<String, Object>) roomBookedMap.get("room");
                if (roomMap != null) {
                    Map<String, Object> roomTypeMap = (Map<String, Object>) roomMap.get("room_type");
                    if (roomTypeMap != null) {
                        roomTypeId = (Integer) roomTypeMap.get("room_type_id");
                        log.info("Found room type ID: {} for booking ID: {}", roomTypeId, bookingMap.get("booking_id"));
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Error extracting room type ID: {}", e.getMessage());
        }
        
        // Build the complete booking DTO
        return GuestBookingDTO.builder()
            .bookingId((Integer) bookingMap.get("booking_id"))
            .checkInDate(ZonedDateTime.parse((String) bookingMap.get("check_in_date")))
            .checkOutDate(ZonedDateTime.parse((String) bookingMap.get("check_out_date")))
            .guest(guest)
            .propertyBooked(property)
            .statusId(statusId)
            .statusName(statusName)
            .roomTypeId(roomTypeId)
            .build();
    }
} 