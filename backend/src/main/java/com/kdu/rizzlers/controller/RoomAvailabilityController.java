package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.constants.AppConstants;
import com.kdu.rizzlers.dto.common.PageResponse;
import com.kdu.rizzlers.dto.in.RoomAvailabilityRequestDTO;
import com.kdu.rizzlers.dto.out.AvailableRoomDTO;
import com.kdu.rizzlers.service.RoomAvailabilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms")
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
    public ResponseEntity<List<AvailableRoomDTO>> getAvailableRoomsLegacy(
            @RequestParam Integer propertyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "2") Integer guestCount,
            @RequestParam(defaultValue = "1") Integer roomCount) {
        
        log.info("Legacy GET request to find available rooms for property: {}, dates: {} to {}, guests: {}, rooms: {}", 
                propertyId, startDate, endDate, guestCount, roomCount);
        
        List<AvailableRoomDTO> availableRooms = roomAvailabilityService.getAvailableRooms(
                propertyId, startDate, endDate, guestCount, roomCount);
        
        return ResponseEntity.ok(availableRooms);
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
    public ResponseEntity<PageResponse<AvailableRoomDTO>> getAvailableRoomsPaginatedLegacy(
            @RequestParam Integer propertyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "2") Integer guestCount,
            @RequestParam(defaultValue = "1") Integer roomCount,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        
        log.info("Legacy GET request to find paginated available rooms for property: {}, dates: {} to {}, guests: {}, rooms: {}, page: {}, size: {}", 
                propertyId, startDate, endDate, guestCount, roomCount, page, size);
        
        PageResponse<AvailableRoomDTO> pagedRooms = roomAvailabilityService.getAvailableRoomsPaginated(
                propertyId, startDate, endDate, guestCount, roomCount, page, size);
        
        return ResponseEntity.ok(pagedRooms);
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
    public ResponseEntity<List<AvailableRoomDTO>> getAvailableRooms(
            @RequestBody RoomAvailabilityRequestDTO request) {
        
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
        
        return ResponseEntity.ok(availableRooms);
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
    public ResponseEntity<PageResponse<AvailableRoomDTO>> getAvailableRoomsPaginated(
            @RequestBody RoomAvailabilityRequestDTO request) {
        
        int totalGuestCount = request.getTotalGuestCount();
        
        // Get the raw page and size values directly from the request
        final int requestedPageNumber = request.getPage() != null ? request.getPage() : 0;
        final int requestedPageSize = request.getSize() != null ? request.getSize() : 10;
        
        // Log room availability constraints for clarity
        log.info("Request for {} rooms: Only room types with at least this many available rooms will be returned", 
                request.getRoomCount());
        
        log.info("POST request to find paginated available rooms with parameters:");
        log.info("- Property ID: {}", request.getPropertyId());
        log.info("- Date Range: {} to {}", request.getStartDate(), request.getEndDate());
        log.info("- Explicit guests field: {}", request.getGuests());
        log.info("- Guest count map: {}", request.getGuestCount());
        log.info("- Legacy fields: adults={}, seniors={}, kids={}", 
                request.getAdults(), request.getSeniorCitizens(), request.getKids());
        log.info("- Final total guest count used: {}", totalGuestCount);
        log.info("- Room count: {}", request.getRoomCount());
        
        // Explicitly log the raw pagination values from the request
        log.info("- Raw pagination values - page: {}, size: {}", request.getPage(), request.getSize());
        log.info("- Raw pagination object: {}", request.getPagination());
        log.info("- Directly accessed pagination values - page: {}, size: {}", requestedPageNumber, requestedPageSize);
        
        PageResponse<AvailableRoomDTO> pagedRooms;
        
        // Check if filters are provided
        if (request.getFilters() != null) {
            log.info("- Filters: roomTypes={}, ratings={}, amenities={}, priceRange={}, sort={}",
                    request.getFilters().getRoomType(),
                    request.getFilters().getRatings(),
                    request.getFilters().getAmenities(),
                    request.getFilters().getPriceRange(),
                    request.getFilters().getSort());
            
            // Use the filtered service method but ask for ALL results to ensure we have everything
            // for manual pagination
            pagedRooms = roomAvailabilityService.getAvailableRoomsWithFilters(
                    request.getPropertyId(), 
                    request.getStartDate(), 
                    request.getEndDate(), 
                    totalGuestCount, 
                    request.getRoomCount(),
                    request.getFilters(),
                    0,  // Get first page
                    1000); // Get all results (assuming no more than 1000 rooms)
        } else {
            // Legacy pagination handling but ask for ALL results
            pagedRooms = roomAvailabilityService.getAvailableRoomsPaginated(
                    request.getPropertyId(), 
                    request.getStartDate(), 
                    request.getEndDate(), 
                    totalGuestCount, 
                    request.getRoomCount(),
                    0,  // Get first page
                    1000); // Get all results (assuming no more than 1000 rooms)
        }
        
        log.info("Original response - total: {}, content size: {}", 
                pagedRooms.getTotalElements(), pagedRooms.getContent().size());
        
        // Get all rooms for pagination
        List<AvailableRoomDTO> allRooms = pagedRooms.getContent();
        int totalElements = allRooms.size();
        int totalPages = (int) Math.ceil((double) totalElements / requestedPageSize);
        
        // Calculate the correct slices
        int start = Math.min(requestedPageNumber * requestedPageSize, totalElements);
        int end = Math.min((requestedPageNumber + 1) * requestedPageSize, totalElements);
        
        log.info("Manual pagination - total: {}, page: {}, size: {}, start: {}, end: {}", 
                totalElements, requestedPageNumber, requestedPageSize, start, end);
                
        // Create a sublist with the correct content
        List<AvailableRoomDTO> pageContent = start < end ? 
                new ArrayList<>(allRooms.subList(start, end)) : new ArrayList<>();
        
        // Create a new page response with the correct content and metadata
        PageResponse<AvailableRoomDTO> customPagedResponse = PageResponse.<AvailableRoomDTO>builder()
                .content(pageContent)
                .pageNumber(requestedPageNumber)
                .pageSize(requestedPageSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .last(requestedPageNumber >= totalPages - 1)
                .build();
        
        log.info("Final custom response - page: {}, size: {}, content size: {}", 
                customPagedResponse.getPageNumber(), customPagedResponse.getPageSize(), 
                customPagedResponse.getContent().size());
                
        return ResponseEntity.ok(customPagedResponse);
    }
} 