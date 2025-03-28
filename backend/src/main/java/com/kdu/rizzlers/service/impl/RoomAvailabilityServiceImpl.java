package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.dto.out.AvailableRoomDTO;
import com.kdu.rizzlers.dto.common.PageResponse;
import com.kdu.rizzlers.repository.ReviewRepository;
import com.kdu.rizzlers.service.RoomAvailabilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomAvailabilityServiceImpl implements RoomAvailabilityService {

    private final WebClient.Builder webClientBuilder;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Value("${graphql.endpoint}")
    private String graphqlEndpoint;
    
    @Value("${graphql.api-key-header}")
    private String apiKeyHeader;
    
    @Value("${graphql.api-key}")
    private String apiKey;

    @Override
    @Transactional(readOnly = true)
    public List<AvailableRoomDTO> getAvailableRooms(
            final Integer propertyId, 
            final LocalDate startDate, 
            final LocalDate endDate, 
            final Integer guestCount,
            final Integer roomCount) {
        
        log.info("Fetching available rooms for propertyId={}, startDate={}, endDate={}, guests={}, rooms={}", 
                propertyId, startDate, endDate, guestCount, roomCount);

        // Create a GraphQL client
        final HttpGraphQlClient graphQlClient = createGraphQlClient();
        
        try {
            // First get the property details to get the address
            final String propertyQuery = """
                query getPropertyDetails($propertyId: Int!) {
                  getProperty(where: {property_id: $propertyId}) {
                    property_id
                    property_name
                    property_address
                  }
                }
            """;
            
            final Map<String, Object> propertyData = graphQlClient.document(propertyQuery)
                .variable("propertyId", propertyId)
                .retrieve("getProperty")
                .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {})
                .onErrorResume(e -> {
                    log.error("Error fetching property details: {}", e.getMessage(), e);
                    return Mono.just(new HashMap<>());
                })
                .block();
                
            // Make property address final
            final String propertyAddress;
            if (propertyData != null && propertyData.containsKey("property_address")) {
                propertyAddress = (String) propertyData.get("property_address");
            } else {
                propertyAddress = "";
            }
            
            // Calculate guest capacity per room based on requested rooms and guests
            final int minCapacityPerRoom = (int) Math.ceil((double) guestCount / roomCount);
            
            // Get all rooms for the property
            final List<Map<String, Object>> roomsList = fetchRooms(graphQlClient, propertyId);
            
            if (roomsList == null || roomsList.isEmpty()) {
                log.info("No rooms found for property {}", propertyId);
                return Collections.emptyList();
            }
            
            // Filter rooms by capacity
            final List<Map<String, Object>> roomsWithCapacity = roomsList.stream()
                .filter(room -> {
                    final Map<String, Object> roomType = (Map<String, Object>) room.get("room_type");
                    final Integer maxCapacity = (Integer) roomType.get("max_capacity");
                    return maxCapacity >= minCapacityPerRoom;
                })
                .collect(Collectors.toList());
            
            log.info("Found {} rooms with sufficient capacity out of {} total rooms", 
                    roomsWithCapacity.size(), roomsList.size());
            
            if (roomsWithCapacity.isEmpty()) {
                return Collections.emptyList();
            }
            
            // Check room availability using the availability table
            final Set<Integer> availableRoomIds = fetchAvailableRoomIds(graphQlClient, propertyId, startDate, endDate);
            
            // Filter to only include rooms that are marked as available
            final List<Map<String, Object>> availableRooms = roomsWithCapacity.stream()
                .filter(room -> {
                    final Integer roomId = (Integer) room.get("room_id");
                    return availableRoomIds.contains(roomId);
                })
                .collect(Collectors.toList());
                
            log.info("Found {} available rooms out of {} capacity-suitable rooms", 
                    availableRooms.size(), roomsWithCapacity.size());
                
            if (availableRooms.isEmpty()) {
                return Collections.emptyList();
            }
            
            // Group the available rooms by room type
            final Map<Integer, List<Map<String, Object>>> roomsByType = availableRooms.stream()
                .collect(Collectors.groupingBy(room -> {
                    final Map<String, Object> roomType = (Map<String, Object>) room.get("room_type");
                    return (Integer) roomType.get("room_type_id");
                }));
                
            log.info("Found {} distinct room types with available rooms", roomsByType.size());
            
            // Extract room type IDs for pricing
            final List<Integer> roomTypeIds = new ArrayList<>(roomsByType.keySet());
                
            // Fetch room rates separately
            final Map<Integer, Integer> roomTypePrices = fetchRoomTypePrices(graphQlClient, roomTypeIds, startDate, endDate);
            
            // Filter out room types without available rates and create one DTO per room type
            final List<AvailableRoomDTO> result = new ArrayList<>();
            
            // Process each room type - creating ONE entry per room type
            for (Map.Entry<Integer, List<Map<String, Object>>> entry : roomsByType.entrySet()) {
                final Integer roomTypeId = entry.getKey();
                final List<Map<String, Object>> roomsOfType = entry.getValue();
                
                // Skip if no pricing available for this room type
                if (!roomTypePrices.containsKey(roomTypeId)) {
                    continue;
                }
                
                // Get the first room to extract room type info (they all share the same room type)
                final Map<String, Object> firstRoom = roomsOfType.get(0);
                final Map<String, Object> roomType = (Map<String, Object>) firstRoom.get("room_type");
                
                // Get price from our map as Integer and convert to Double for DTO
                final Integer priceValue = roomTypePrices.get(roomTypeId);
                final Double price = priceValue.doubleValue();
                
                // Extract all room IDs for this room type
                final List<Integer> roomIds = roomsOfType.stream()
                    .map(room -> (Integer) room.get("room_id"))
                    .collect(Collectors.toList());
                
                // Fetch additional data from RDS
                // Get review data
                Optional<Map<String, Object>> reviewData = reviewRepository.findReviewByRoomTypeId(roomTypeId);
                
                // Get amenities
                List<String> amenities = reviewRepository.findAmenitiesByRoomTypeId(roomTypeId);
                
                // Create bed types list
                List<String> bedTypes = new ArrayList<>();
                Integer singleBeds = (Integer) roomType.get("single_bed");
                Integer doubleBeds = (Integer) roomType.get("double_bed");
                
                if (singleBeds > 0) {
                    bedTypes.add(singleBeds + " Single Bed" + (singleBeds > 1 ? "s" : ""));
                }
                
                if (doubleBeds > 0) {
                    bedTypes.add(doubleBeds + " Double Bed" + (doubleBeds > 1 ? "s" : ""));
                }
                
                // Build DTO with combined GraphQL and RDS data
                var dtoBuilder = AvailableRoomDTO.builder()
                    .roomTypeId(roomTypeId)
                    .roomId(roomIds.get(0)) // Use first room ID as primary
                    .roomTypeName((String) roomType.get("room_type_name"))
                    .maxCapacity((Integer) roomType.get("max_capacity"))
                    .areaInSquareFeet((Integer) roomType.get("area_in_square_feet"))
                    .singleBed((Integer) roomType.get("single_bed"))
                    .doubleBed((Integer) roomType.get("double_bed"))
                    .propertyAddress(propertyAddress)
                    .price(price)
                    .availableRoomIds(roomIds)
                    .availableRoomCount(roomIds.size())
                    .bedTypes(bedTypes)
                    .amenities(amenities);
                
                if (reviewData.isPresent()) {
                    Map<String, Object> review = reviewData.get();
                    
                    // Parse images array from PostgreSQL
                    Object imagesObject = review.get("images");
                    List<String> images;
                    
                    if (imagesObject instanceof java.sql.Array) {
                        // Handle JDBC array interface
                        try {
                            java.sql.Array sqlArray = (java.sql.Array) imagesObject;
                            Object arrayData = sqlArray.getArray();
                            
                            if (arrayData instanceof String[]) {
                                images = Arrays.asList((String[]) arrayData);
                            } else if (arrayData instanceof Object[]) {
                                // Convert Object[] to List<String>
                                Object[] objArray = (Object[]) arrayData;
                                images = new ArrayList<>(objArray.length);
                                for (Object item : objArray) {
                                    if (item != null) {
                                        images.add(item.toString());
                                    }
                                }
                            } else {
                                log.warn("Unexpected array data type: {}", 
                                    arrayData != null ? arrayData.getClass().getName() : "null");
                                images = new ArrayList<>();
                            }
                        } catch (Exception e) {
                            log.warn("Error extracting JDBC array: {}", e.getMessage());
                            images = new ArrayList<>();
                        }
                    } else if (imagesObject instanceof String) {
                        // Keep backward compatibility with String format
                        images = reviewRepository.parseTextArray((String) imagesObject);
                    } else {
                        log.warn("Unexpected type for images: {}", 
                            imagesObject != null ? imagesObject.getClass().getName() : "null");
                        images = new ArrayList<>();
                    }
                    
                    // Set review-related fields
                    dtoBuilder
                        .roomDescription((String) review.get("description"))
                        .roomImages(images)
                        .rating(AvailableRoomDTO.Rating.builder()
                            .stars(((Number) review.get("rating")).doubleValue())
                            .reviewCount((Integer) review.get("review_count"))
                            .build());
                }
                
                result.add(dtoBuilder.build());
            }
                
            // Sort by price ascending
            return result.stream()
                .sorted(Comparator.comparing(AvailableRoomDTO::getPrice))
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("Error in getAvailableRooms: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Fetch all rooms for a property
     */
    private List<Map<String, Object>> fetchRooms(
            final HttpGraphQlClient graphQlClient,
            final Integer propertyId) {
            
        // Get available room types for the property with sufficient capacity
        final String roomsQuery = """
            query getRooms($propertyId: Int!) {
              listRooms(
                where: {
                  property_id: {equals: $propertyId}
                }
                take: 100
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
        
        return graphQlClient.document(roomsQuery)
            .variable("propertyId", propertyId)
            .retrieve("listRooms")
            .toEntity(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
            .onErrorResume(e -> {
                log.error("Error fetching rooms: {}", e.getMessage(), e);
                return Mono.just(new ArrayList<>());
            })
            .block();
    }
    
    /**
     * Fetch available room IDs for a date range
     */
    private Set<Integer> fetchAvailableRoomIds(
            final HttpGraphQlClient graphQlClient,
            final Integer propertyId,
            final LocalDate startDate,
            final LocalDate endDate) {
            
        // Format dates as AWSDateTime (ISO-8601 with timezone)
        final String startDateStr = startDate.atStartOfDay(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
        final String endDateStr = endDate.atTime(23, 59, 59).atZone(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
        
        log.info("Checking room availability from {} to {}", startDateStr, endDateStr);
        
        // Query to get all available rooms for the period, using booking status filtering
        final String availabilityQuery = String.format("""
            query getAvailableRooms {
              listRoomAvailabilities(
                where: {
                  property_id: {equals: %d}, 
                  date: {gte: "%s", lte: "%s"},
                  booking: {booking_status: {status: {not: {equals: "BOOKED"}}}}
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
        """, propertyId, startDateStr, endDateStr);
        
        try {
            final List<Map<String, Object>> availabilityList = graphQlClient.document(availabilityQuery)
                .retrieve("listRoomAvailabilities")
                .toEntity(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .onErrorResume(e -> {
                    log.error("Error fetching room availabilities: {}", e.getMessage(), e);
                    return Mono.just(new ArrayList<>());
                })
                .block();
                
            if (availabilityList == null || availabilityList.isEmpty()) {
                log.info("No room availabilities found for the period");
                return Collections.emptySet();
            }
            
            // Group by room ID to check if each room is available for the entire period
            Map<Integer, Set<String>> roomAvailabilityDates = new HashMap<>();
            
            // Also track room types for logging
            Map<String, Set<Integer>> roomTypeToRooms = new HashMap<>();
            
            for (Map<String, Object> availability : availabilityList) {
                Integer roomId = (Integer) availability.get("room_id");
                String date = (String) availability.get("date");
                
                // Add to date tracking for availability check
                roomAvailabilityDates.computeIfAbsent(roomId, k -> new HashSet<>()).add(date);
                
                // Track room types for logging
                Map<String, Object> room = (Map<String, Object>) availability.get("room");
                if (room != null) {
                    Map<String, Object> roomType = (Map<String, Object>) room.get("room_type");
                    if (roomType != null) {
                        String roomTypeName = (String) roomType.get("room_type_name");
                        roomTypeToRooms.computeIfAbsent(roomTypeName, k -> new HashSet<>()).add(roomId);
                    }
                }
            }
            
            // Log room types and their available rooms count
            roomTypeToRooms.forEach((roomTypeName, roomIds) -> {
                log.info("Room type: {} has {} available rooms", roomTypeName, roomIds.size());
            });
            
            // Calculate the expected number of days in the date range
            long totalDays = endDate.toEpochDay() - startDate.toEpochDay() + 1;
            
            // Find rooms that are available for the entire period
            final Set<Integer> availableRoomIds = roomAvailabilityDates.entrySet().stream()
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
    
    /**
     * Fetch room rates for the specified room types and date range
     */
    private Map<Integer, Integer> fetchRoomTypePrices(
            final HttpGraphQlClient graphQlClient, 
            final List<Integer> roomTypeIds, 
            final LocalDate startDate, 
            final LocalDate endDate) {
            
        // If no room types found, return empty map
        if (roomTypeIds.isEmpty()) {
            return Collections.emptyMap();
        }
        
        try {
            log.info("Fetching rates for room types: {}", roomTypeIds);
            
            // Use the query from user's example
            final String ratesQuery = """
                query getRoomRates($roomTypeIds: [Int!]!) {
                  listRoomRateRoomTypeMappings(
                    where: {room_type_id: {in: $roomTypeIds}}
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
            
            final List<Map<String, Object>> mappingsList = graphQlClient.document(ratesQuery)
                .variable("roomTypeIds", roomTypeIds)
                .retrieve("listRoomRateRoomTypeMappings")
                .toEntity(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .onErrorResume(e -> {
                    log.error("Error fetching room rates: {}", e.getMessage(), e);
                    return Mono.just(new ArrayList<>());
                })
                .block();
                
            // Calculate average price for each room type
            final Map<Integer, List<Integer>> pricesByRoomType = new HashMap<>();
            
            if (mappingsList != null && !mappingsList.isEmpty()) {
                for (final Map<String, Object> mapping : mappingsList) {
                    final Integer roomTypeId = (Integer) mapping.get("room_type_id");
                    final Map<String, Object> roomRate = (Map<String, Object>) mapping.get("room_rate");
                    
                    if (roomRate != null) {
                        final String dateStr = (String) roomRate.get("date");
                        
                        // Handle basic_nightly_rate as Integer instead of Double
                        final Integer nightlyRate = ((Number) roomRate.get("basic_nightly_rate")).intValue();
                        
                        // Filter by date range
                        try {
                            // Parse the date string (may be in different formats)
                            final LocalDate rateDate;
                            if (dateStr.contains("T")) {
                                rateDate = LocalDate.parse(dateStr.substring(0, 10));
                            } else {
                                rateDate = LocalDate.parse(dateStr);
                            }
                            
                            // Only include rates within our date range
                            if (!rateDate.isBefore(startDate) && !rateDate.isAfter(endDate)) {
                                pricesByRoomType.computeIfAbsent(roomTypeId, k -> new ArrayList<>()).add(nightlyRate);
                            }
                        } catch (Exception e) {
                            log.warn("Could not parse date: {}", dateStr);
                        }
                    }
                }
                
                log.info("Found rates for {} room types within date range", pricesByRoomType.size());
            } else {
                log.info("No rates found for the requested room types");
            }
            
            // Calculate average price for each room type
            final Map<Integer, Integer> result = new HashMap<>();
            for (final Map.Entry<Integer, List<Integer>> entry : pricesByRoomType.entrySet()) {
                final Integer roomTypeId = entry.getKey();
                final List<Integer> prices = entry.getValue();
                
                if (!prices.isEmpty()) {
                    // Calculate average as Integer
                    final int avg = (int) prices.stream().mapToInt(Integer::intValue).average().orElse(0);
                    result.put(roomTypeId, avg);
                }
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("Error fetching room rates: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }

    /**
     * Creates a GraphQL client with the configured endpoint and API key
     */
    private HttpGraphQlClient createGraphQlClient() {
        final WebClient webClient = webClientBuilder
            .baseUrl(graphqlEndpoint)
            .defaultHeader(apiKeyHeader, apiKey)
            .build();
            
        return HttpGraphQlClient.builder(webClient)
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AvailableRoomDTO> getAvailableRoomsPaginated(
            final Integer propertyId, 
            final LocalDate startDate, 
            final LocalDate endDate, 
            final Integer guestCount,
            final Integer roomCount,
            final int pageNumber,
            final int pageSize) {
        
        log.info("Fetching paginated available rooms for propertyId={}, startDate={}, endDate={}, guests={}, rooms={}, page={}, size={}", 
                propertyId, startDate, endDate, guestCount, roomCount, pageNumber, pageSize);
        
        // Reuse the existing method to get all available rooms
        List<AvailableRoomDTO> allAvailableRooms = getAvailableRooms(
                propertyId, startDate, endDate, guestCount, roomCount);
        
        // Convert the list to a paginated response
        return PageResponse.of(allAvailableRooms, pageNumber, pageSize);
    }
} 