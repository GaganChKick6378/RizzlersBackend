package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.constants.AppConstants;
import com.kdu.rizzlers.dto.common.PageResponse;
import com.kdu.rizzlers.dto.in.RoomAvailabilityRequestDTO;
import com.kdu.rizzlers.dto.out.AvailableRoomDTO;
import com.kdu.rizzlers.exception.ApiError;
import com.kdu.rizzlers.exception.RoomUnavailableException;
import com.kdu.rizzlers.service.RoomAvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms")
@Tag(name = "Room Availability", description = "APIs for checking room availability")
public class RoomAvailabilityController {

    private final RoomAvailabilityService roomAvailabilityService;

    /**
     * Get available rooms for a property based on dates and guest/room counts (Legacy endpoint)
     * 
     * @param propertyId The property ID to search for
     * @param startDate Check-in date
     * @param endDate Check-out date
     * @param guestCount Number of guests (default: 2)
     * @param roomCount Number of rooms required (default: 1)
     * @return List of available rooms with their details
     */
    @GetMapping("/available/legacy")
    @Operation(summary = "Get available rooms (legacy)", 
              description = "Retrieves available rooms for a property based on dates and guest/room counts")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved available rooms",
                   content = @Content(schema = @Schema(implementation = AvailableRoomDTO.class))),
        @ApiResponse(responseCode = "409", description = "No rooms available for the specified criteria",
                   content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<?> getAvailableRoomsLegacy(
            @Parameter(description = "Property ID", required = true) 
            @RequestParam Integer propertyId,
            
            @Parameter(description = "Check-in date (YYYY-MM-DD)", required = true) 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "Check-out date (YYYY-MM-DD)", required = true) 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            
            @Parameter(description = "Number of guests", example = "2") 
            @RequestParam(defaultValue = "2") Integer guestCount,
            
            @Parameter(description = "Number of rooms required", example = "1") 
            @RequestParam(defaultValue = "1") Integer roomCount) {
        
        log.info("Legacy GET request to find available rooms for property: {}, dates: {} to {}, guests: {}, rooms: {}", 
                propertyId, startDate, endDate, guestCount, roomCount);
        
        try {
            List<AvailableRoomDTO> availableRooms = roomAvailabilityService.getAvailableRooms(
                    propertyId, startDate, endDate, guestCount, roomCount);
            
            if (availableRooms.isEmpty()) {
                throw new RoomUnavailableException("No rooms available for the specified criteria");
            }
            
            return ResponseEntity.ok(availableRooms);
        } catch (RoomUnavailableException e) {
            log.warn("No rooms available: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("status", HttpStatus.CONFLICT.value());
            response.put("error", "Conflict");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } catch (Exception e) {
            log.error("Error getting available rooms: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("error", "Internal Server Error");
            response.put("message", "An unexpected error occurred while processing your request");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Get paginated available rooms for a property based on dates and guest/room counts (Legacy endpoint)
     * 
     * @param propertyId The property ID to search for
     * @param startDate Check-in date
     * @param endDate Check-out date
     * @param guestCount Number of guests (default: 2)
     * @param roomCount Number of rooms required (default: 1)
     * @param page Page number (0-based, default: 0)
     * @param size Size of each page (default: 10)
     * @return Paginated list of available rooms with their details
     */
    @GetMapping("/available/paged/legacy")
    @Operation(summary = "Get paginated available rooms (legacy)", 
              description = "Retrieves paginated available rooms for a property based on dates and guest/room counts")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated available rooms",
                   content = @Content(schema = @Schema(implementation = PageResponse.class))),
        @ApiResponse(responseCode = "409", description = "No rooms available for the specified criteria",
                   content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<?> getAvailableRoomsPaginatedLegacy(
            @Parameter(description = "Property ID", required = true) 
            @RequestParam Integer propertyId,
            
            @Parameter(description = "Check-in date (YYYY-MM-DD)", required = true) 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "Check-out date (YYYY-MM-DD)", required = true) 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            
            @Parameter(description = "Number of guests", example = "2") 
            @RequestParam(defaultValue = "2") Integer guestCount,
            
            @Parameter(description = "Number of rooms required", example = "1") 
            @RequestParam(defaultValue = "1") Integer roomCount,
            
            @Parameter(description = "Page number (0-based)", example = "0") 
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            
            @Parameter(description = "Size of each page", example = "10") 
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        
        log.info("Legacy GET request to find paginated available rooms for property: {}, dates: {} to {}, guests: {}, rooms: {}, page: {}, size: {}", 
                propertyId, startDate, endDate, guestCount, roomCount, page, size);
        
        try {
            PageResponse<AvailableRoomDTO> pagedRooms = roomAvailabilityService.getAvailableRoomsPaginated(
                    propertyId, startDate, endDate, guestCount, roomCount, page, size);
            
            if (pagedRooms.getContent().isEmpty() && pagedRooms.getTotalElements() == 0) {
                throw new RoomUnavailableException("No rooms available for the specified criteria");
            }
            
            return ResponseEntity.ok(pagedRooms);
        } catch (RoomUnavailableException e) {
            log.warn("No rooms available: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("status", HttpStatus.CONFLICT.value());
            response.put("error", "Conflict");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } catch (Exception e) {
            log.error("Error getting paginated available rooms: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("error", "Internal Server Error");
            response.put("message", "An unexpected error occurred while processing your request");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get available rooms for a property based on dates and detailed guest counts
     * 
     * @param request Request containing:
     *                - propertyId: The property ID to search for
     *                - startDate: Check-in date
     *                - endDate: Check-out date
     *                - guests: Total number of guests for pricing calculations (overrides sum of individual guest types)
     *                - adults: Number of adult guests (default: 2)
     *                - seniorCitizens: Number of senior citizens (default: 0)
     *                - kids: Number of kids (default: 0)
     *                - roomCount: Number of rooms required (default: 1)
     * @return List of available rooms with their details
     */
    @PostMapping("/available")
    @Operation(summary = "Get available rooms", 
              description = "Retrieves available rooms for a property based on dates and detailed guest counts")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved available rooms",
                   content = @Content(schema = @Schema(implementation = AvailableRoomDTO.class))),
        @ApiResponse(responseCode = "409", description = "No rooms available for the specified criteria",
                   content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters",
                   content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<?> getAvailableRooms(
            @Parameter(description = "Room availability request with detailed criteria", required = true)
            @RequestBody RoomAvailabilityRequestDTO request) {
        
        try {
            // Validate the request
            if (request.getPropertyId() == null) {
                throw new IllegalArgumentException("Property ID is required");
            }
            
            if (request.getStartDate() == null || request.getEndDate() == null) {
                throw new IllegalArgumentException("Both start date and end date are required");
            }
            
            if (request.getStartDate().isAfter(request.getEndDate())) {
                throw new IllegalArgumentException("Start date cannot be after end date");
            }
            
            int totalGuestCount = request.getTotalGuestCount();
            
            log.info("POST request with body to find available rooms for property: {}, dates: {} to {}, " +
                    "guests: {}, guestCount: {}, adults: {}, seniors: {}, kids: {}, rooms: {}", 
                    request.getPropertyId(), request.getStartDate(), request.getEndDate(), 
                    request.getGuests(), request.getGuestCount(),
                    request.getAdults(), request.getSeniorCitizens(), request.getKids(), 
                    request.getRoomCount());
            
            List<AvailableRoomDTO> availableRooms = roomAvailabilityService.getAvailableRooms(
                    request.getPropertyId(), 
                    request.getStartDate(), 
                    request.getEndDate(), 
                    totalGuestCount, 
                    request.getRoomCount());
            
            if (availableRooms.isEmpty()) {
                throw new RoomUnavailableException("No rooms available for the specified criteria");
            }
            
            return ResponseEntity.ok(availableRooms);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("error", "Bad Request");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (RoomUnavailableException e) {
            log.warn("No rooms available: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("status", HttpStatus.CONFLICT.value());
            response.put("error", "Conflict");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } catch (Exception e) {
            log.error("Error getting available rooms: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("error", "Internal Server Error");
            response.put("message", "An unexpected error occurred while processing your request");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Get paginated available rooms for a property based on dates and detailed guest counts
     * 
     * @param request Request containing:
     *                - propertyId: The property ID to search for
     *                - startDate: Check-in date
     *                - endDate: Check-out date
     *                - guests: Total number of guests for pricing calculations (overrides sum of individual guest types)
     *                - adults: Number of adult guests (default: 2)
     *                - seniorCitizens: Number of senior citizens (default: 0)
     *                - kids: Number of kids (default: 0)
     *                - roomCount: Number of rooms required (default: 1)
     *                - page: Page number (0-based, default: 0)
     *                - size: Size of each page (default: 10)
     * @return Paginated list of available rooms with their details
     */
    @PostMapping("/available/paged")
    @Operation(summary = "Get paginated available rooms", 
              description = "Retrieves paginated available rooms for a property based on dates and detailed guest counts with optional filtering")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated available rooms",
                   content = @Content(schema = @Schema(implementation = PageResponse.class))),
        @ApiResponse(responseCode = "409", description = "No rooms available for the specified criteria",
                   content = @Content(schema = @Schema(implementation = ApiError.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters",
                   content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<?> getAvailableRoomsPaginated(
            @Parameter(description = "Room availability request with detailed criteria and pagination options", required = true)
            @RequestBody RoomAvailabilityRequestDTO request) {
        
        try {
            // Validate the request
            if (request.getPropertyId() == null) {
                throw new IllegalArgumentException("Property ID is required");
            }
            
            if (request.getStartDate() == null || request.getEndDate() == null) {
                throw new IllegalArgumentException("Both start date and end date are required");
            }
            
            if (request.getStartDate().isAfter(request.getEndDate())) {
                throw new IllegalArgumentException("Start date cannot be after end date");
            }
            
            int totalGuestCount = request.getTotalGuestCount();
            
            log.info("POST request to find paginated available rooms for property: {}, dates: {} to {}, " +
                    "guests: {}, guestCount: {}, adults: {}, seniors: {}, kids: {}, rooms: {}, page: {}, size: {}", 
                    request.getPropertyId(), request.getStartDate(), request.getEndDate(), 
                    request.getGuests(), request.getGuestCount(),
                    request.getAdults(), request.getSeniorCitizens(), request.getKids(), 
                    request.getRoomCount(), request.getPage(), request.getSize());
            
            PageResponse<AvailableRoomDTO> pagedRooms = roomAvailabilityService.getAvailableRoomsPaginated(
                    request.getPropertyId(), 
                    request.getStartDate(), 
                    request.getEndDate(), 
                    totalGuestCount, 
                    request.getRoomCount(),
                    request.getPage(), 
                    request.getSize());
            
            if (pagedRooms.getContent().isEmpty() && pagedRooms.getTotalElements() == 0) {
                throw new RoomUnavailableException("No rooms available for the specified criteria");
            }
            
            return ResponseEntity.ok(pagedRooms);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("status", HttpStatus.BAD_REQUEST.value());
            response.put("error", "Bad Request");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (RoomUnavailableException e) {
            log.warn("No rooms available: {}", e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("status", HttpStatus.CONFLICT.value());
            response.put("error", "Conflict");
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } catch (Exception e) {
            log.error("Error getting paginated available rooms: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.put("error", "Internal Server Error");
            response.put("message", "An unexpected error occurred while processing your request");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
} 