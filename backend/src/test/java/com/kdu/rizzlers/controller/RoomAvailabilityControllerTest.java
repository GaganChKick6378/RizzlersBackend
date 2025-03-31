package com.kdu.rizzlers.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kdu.rizzlers.dto.common.PageResponse;
import com.kdu.rizzlers.dto.in.RoomAvailabilityRequestDTO;
import com.kdu.rizzlers.dto.out.AvailableRoomDTO;
import com.kdu.rizzlers.service.RoomAvailabilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(RoomAvailabilityController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoomAvailabilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RoomAvailabilityService roomAvailabilityService;

    private List<AvailableRoomDTO> availableRooms;
    private PageResponse<AvailableRoomDTO> pagedResponse;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer propertyId;

    @BeforeEach
    void setUp() {
        propertyId = 101;
        startDate = LocalDate.now();
        endDate = startDate.plusDays(3);

        // Create available rooms
        AvailableRoomDTO room1 = AvailableRoomDTO.builder()
                .roomTypeId(201)
                .roomId(301)
                .roomTypeName("Deluxe Suite")
                .maxCapacity(2)
                .areaInSquareFeet(500)
                .singleBed(1)
                .doubleBed(1)
                .propertyAddress("123 Main St")
                .price(250.0)
                .roomCount(1)
                .availableRoomIds(Arrays.asList(301, 302, 303))
                .availableRoomCount(3)
                .roomImages(Arrays.asList("image1.jpg", "image2.jpg"))
                .roomDescription("Luxurious room with a view")
                .rating(new AvailableRoomDTO.Rating(4.5, 120))
                .landmark("City Center")
                .bedTypes(Arrays.asList("Single", "Double"))
                .amenities(Arrays.asList("Wifi", "TV", "Minibar"))
                .build();

        AvailableRoomDTO room2 = AvailableRoomDTO.builder()
                .roomTypeId(202)
                .roomId(401)
                .roomTypeName("Standard Room")
                .maxCapacity(2)
                .areaInSquareFeet(350)
                .singleBed(2)
                .doubleBed(0)
                .propertyAddress("123 Main St")
                .price(150.0)
                .roomCount(1)
                .availableRoomIds(Arrays.asList(401, 402))
                .availableRoomCount(2)
                .roomImages(Arrays.asList("image3.jpg", "image4.jpg"))
                .roomDescription("Comfortable standard room")
                .rating(new AvailableRoomDTO.Rating(3.8, 95))
                .landmark("City Center")
                .bedTypes(Arrays.asList("Single"))
                .amenities(Arrays.asList("Wifi", "TV"))
                .build();

        availableRooms = Arrays.asList(room1, room2);
        pagedResponse = PageResponse.<AvailableRoomDTO>builder()
                .content(availableRooms)
                .pageNumber(0)
                .pageSize(10)
                .totalElements(2)
                .totalPages(1)
                .last(true)
                .build();
    }

    @Test
    @DisplayName("Get available rooms with legacy endpoint should return list of rooms")
    void getAvailableRoomsLegacy_shouldReturnRooms() throws Exception {
        when(roomAvailabilityService.getAvailableRooms(
                eq(propertyId), any(LocalDate.class), any(LocalDate.class), anyInt(), anyInt()))
                .thenReturn(availableRooms);

        mockMvc.perform(get("/rooms/available/legacy")
                .param("propertyId", propertyId.toString())
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
                .param("guestCount", "2")
                .param("roomCount", "1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].roomTypeId", is(201)))
                .andExpect(jsonPath("$[0].roomTypeName", is("Deluxe Suite")))
                .andExpect(jsonPath("$[0].price", is(250.0)))
                .andExpect(jsonPath("$[1].roomTypeId", is(202)))
                .andExpect(jsonPath("$[1].roomTypeName", is("Standard Room")))
                .andExpect(jsonPath("$[1].price", is(150.0)));
    }

    @Test
    @DisplayName("Get available rooms legacy with no rooms should return empty list")
    void getAvailableRoomsLegacy_withNoAvailableRooms_shouldReturnEmptyList() throws Exception {
        when(roomAvailabilityService.getAvailableRooms(
                eq(propertyId), any(LocalDate.class), any(LocalDate.class), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/rooms/available/legacy")
                .param("propertyId", propertyId.toString())
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
                .param("guestCount", "2")
                .param("roomCount", "1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Get paginated available rooms legacy should return page of rooms")
    void getAvailableRoomsPaginatedLegacy_shouldReturnPagedRooms() throws Exception {
        when(roomAvailabilityService.getAvailableRoomsPaginated(
                eq(propertyId), any(LocalDate.class), any(LocalDate.class), anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(pagedResponse);

        mockMvc.perform(get("/rooms/available/paged/legacy")
                .param("propertyId", propertyId.toString())
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
                .param("guestCount", "2")
                .param("roomCount", "1")
                .param("page", "0")
                .param("size", "10")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.pageNumber", is(0)))
                .andExpect(jsonPath("$.pageSize", is(10)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.last", is(true)));
    }

    @Test
    @DisplayName("Get available rooms with POST endpoint should return list of rooms")
    void getAvailableRooms_shouldReturnRooms() throws Exception {
        RoomAvailabilityRequestDTO request = RoomAvailabilityRequestDTO.builder()
                .propertyId(propertyId)
                .startDate(startDate)
                .endDate(endDate)
                .guests(2)
                .adults(2)
                .seniorCitizens(0)
                .kids(0)
                .roomCount(1)
                .build();

        when(roomAvailabilityService.getAvailableRooms(
                eq(propertyId), any(LocalDate.class), any(LocalDate.class), anyInt(), anyInt()))
                .thenReturn(availableRooms);

        mockMvc.perform(post("/rooms/available")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].roomTypeId", is(201)))
                .andExpect(jsonPath("$[0].roomTypeName", is("Deluxe Suite")))
                .andExpect(jsonPath("$[1].roomTypeId", is(202)))
                .andExpect(jsonPath("$[1].roomTypeName", is("Standard Room")));
    }

    @Test
    @DisplayName("Get paginated available rooms with POST endpoint should return page of rooms")
    void getAvailableRoomsPaginated_shouldReturnPagedRooms() throws Exception {
        RoomAvailabilityRequestDTO request = RoomAvailabilityRequestDTO.builder()
                .propertyId(propertyId)
                .startDate(startDate)
                .endDate(endDate)
                .guests(2)
                .adults(2)
                .seniorCitizens(0)
                .kids(0)
                .roomCount(1)
                .page(0)
                .size(10)
                .build();

        when(roomAvailabilityService.getAvailableRoomsPaginated(
                eq(propertyId), any(LocalDate.class), any(LocalDate.class), anyInt(), anyInt(), anyInt(), anyInt()))
                .thenReturn(pagedResponse);

        when(roomAvailabilityService.getAvailableRoomsWithFilters(
                eq(propertyId), any(LocalDate.class), any(LocalDate.class), anyInt(), anyInt(), any(), anyInt(), anyInt()))
                .thenReturn(pagedResponse);

        mockMvc.perform(post("/rooms/available/paged")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.pageNumber", is(0)))
                .andExpect(jsonPath("$.pageSize", is(10)))
                .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @Test
    @DisplayName("Get paginated available rooms with filters should return filtered page of rooms")
    void getAvailableRoomsPaginated_withFilters_shouldReturnFilteredPagedRooms() throws Exception {
        List<String> roomTypes = Arrays.asList("Deluxe Suite");
        List<Integer> ratings = Arrays.asList(4, 5);
        List<String> amenities = Arrays.asList("Wifi", "Minibar");
        List<Integer> priceRange = Arrays.asList(200, 300);

        RoomAvailabilityRequestDTO.FilterDTO filters = RoomAvailabilityRequestDTO.FilterDTO.builder()
                .roomType(roomTypes)
                .ratings(ratings)
                .amenities(amenities)
                .priceRange(priceRange)
                .sort("price-low-high")
                .build();

        RoomAvailabilityRequestDTO request = RoomAvailabilityRequestDTO.builder()
                .propertyId(propertyId)
                .startDate(startDate)
                .endDate(endDate)
                .guests(2)
                .adults(2)
                .roomCount(1)
                .page(0)
                .size(10)
                .filters(filters)
                .build();

        // Create a filtered response with only one room that matches the filters
        PageResponse<AvailableRoomDTO> filteredResponse = PageResponse.<AvailableRoomDTO>builder()
                .content(Collections.singletonList(availableRooms.get(0)))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();

        when(roomAvailabilityService.getAvailableRoomsWithFilters(
                eq(propertyId), any(LocalDate.class), any(LocalDate.class), anyInt(), anyInt(), any(), anyInt(), anyInt()))
                .thenReturn(filteredResponse);

        mockMvc.perform(post("/rooms/available/paged")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].roomTypeName", is("Deluxe Suite")))
                .andExpect(jsonPath("$.pageNumber", is(0)))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    @DisplayName("Get available rooms with invalid property ID should return empty list")
    void getAvailableRooms_withInvalidPropertyId_shouldReturnEmptyList() throws Exception {
        Integer invalidPropertyId = 999;
        
        when(roomAvailabilityService.getAvailableRooms(
                eq(invalidPropertyId), any(LocalDate.class), any(LocalDate.class), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/rooms/available/legacy")
                .param("propertyId", invalidPropertyId.toString())
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
                .param("guestCount", "2")
                .param("roomCount", "1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
} 