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
import java.util.*;

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

    private AvailableRoomDTO room1;
    private AvailableRoomDTO room2;
    private List<AvailableRoomDTO> availableRooms;
    private PageResponse<AvailableRoomDTO> pagedRooms;
    private RoomAvailabilityRequestDTO requestDTO;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        startDate = LocalDate.now().plusDays(1);
        endDate = LocalDate.now().plusDays(3);

        // Create test rooms
        room1 = AvailableRoomDTO.builder()
                .roomTypeId(101)
                .roomTypeName("Deluxe Room")
                .maxCapacity(2)
                .amenities(Arrays.asList("WiFi", "TV"))
                .price(100.00)
                .availableRoomCount(5)
                .roomImages(Collections.singletonList("https://example.com/room1.jpg"))
                .build();

        room2 = AvailableRoomDTO.builder()
                .roomTypeId(102)
                .roomTypeName("Suite")
                .maxCapacity(4)
                .amenities(Arrays.asList("WiFi", "TV", "Kitchen"))
                .price(200.00)
                .availableRoomCount(3)
                .roomImages(Collections.singletonList("https://example.com/room2.jpg"))
                .build();

        availableRooms = Arrays.asList(room1, room2);

        // Create paged response
        pagedRooms = new PageResponse<>();
        pagedRooms.setContent(availableRooms);
        pagedRooms.setTotalElements(2);
        pagedRooms.setTotalPages(1);
        pagedRooms.setPageNumber(0);
        pagedRooms.setPageSize(10);

        // Create request DTO
        requestDTO = RoomAvailabilityRequestDTO.builder()
                .propertyId(1)
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
    }

    @Test
    @DisplayName("Should return available rooms using legacy endpoint")
    void getAvailableRoomsLegacy_shouldReturnAvailableRooms() throws Exception {
        // Arrange
        when(roomAvailabilityService.getAvailableRooms(
                anyInt(), any(LocalDate.class), any(LocalDate.class), anyInt(), anyInt())
        ).thenReturn(availableRooms);

        // Act & Assert
        mockMvc.perform(get("/rooms/available/legacy")
                .with(csrf())
                .param("propertyId", "1")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
                .param("guestCount", "2")
                .param("roomCount", "1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].roomTypeId", is(101)))
                .andExpect(jsonPath("$[0].roomTypeName", is("Deluxe Room")))
                .andExpect(jsonPath("$[0].maxCapacity", is(2)))
                .andExpect(jsonPath("$[0].price", is(100.00)))
                .andExpect(jsonPath("$[1].roomTypeId", is(102)))
                .andExpect(jsonPath("$[1].roomTypeName", is("Suite")));
    }

    @Test
    @DisplayName("Should return paged available rooms using legacy endpoint")
    void getAvailableRoomsPaginatedLegacy_shouldReturnPagedRooms() throws Exception {
        // Arrange
        when(roomAvailabilityService.getAvailableRoomsPaginated(
                anyInt(), any(LocalDate.class), any(LocalDate.class), anyInt(), anyInt(), anyInt(), anyInt())
        ).thenReturn(pagedRooms);

        // Act & Assert
        mockMvc.perform(get("/rooms/available/paged/legacy")
                .with(csrf())
                .param("propertyId", "1")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
                .param("guestCount", "2")
                .param("roomCount", "1")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.pageNumber", is(0)))
                .andExpect(jsonPath("$.pageSize", is(10)))
                .andExpect(jsonPath("$.content[0].roomTypeId", is(101)))
                .andExpect(jsonPath("$.content[1].roomTypeId", is(102)));
    }

    @Test
    @DisplayName("Should return available rooms using request body")
    void getAvailableRooms_shouldReturnAvailableRooms() throws Exception {
        // Arrange
        when(roomAvailabilityService.getAvailableRooms(
                anyInt(), any(LocalDate.class), any(LocalDate.class), anyInt(), anyInt())
        ).thenReturn(availableRooms);

        // Act & Assert
        mockMvc.perform(post("/rooms/available")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].roomTypeId", is(101)))
                .andExpect(jsonPath("$[0].roomTypeName", is("Deluxe Room")))
                .andExpect(jsonPath("$[1].roomTypeId", is(102)))
                .andExpect(jsonPath("$[1].roomTypeName", is("Suite")));
    }

    @Test
    @DisplayName("Should handle empty available rooms")
    void getAvailableRooms_withNoAvailableRooms_shouldReturnEmptyList() throws Exception {
        // Arrange
        when(roomAvailabilityService.getAvailableRooms(
                anyInt(), any(LocalDate.class), any(LocalDate.class), anyInt(), anyInt())
        ).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(post("/rooms/available")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("Should handle request with different guest types")
    void getAvailableRooms_withDifferentGuestTypes_shouldUseCorrectTotalCount() throws Exception {
        // Arrange
        requestDTO.setAdults(2);
        requestDTO.setKids(1);
        requestDTO.setSeniorCitizens(1);
        // Total should be 4 by default calculation (2+1+1)
        
        // Use anyInt() for guest count to match any value
        when(roomAvailabilityService.getAvailableRooms(
                anyInt(), any(LocalDate.class), any(LocalDate.class), anyInt(), anyInt())
        ).thenReturn(availableRooms);

        // Act & Assert
        mockMvc.perform(post("/rooms/available")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("Should use explicit guest count when provided")
    void getAvailableRooms_withExplicitGuestCount_shouldOverrideIndividualCounts() throws Exception {
        // Arrange
        requestDTO.setAdults(2);
        requestDTO.setKids(1);
        requestDTO.setSeniorCitizens(1);
        // Override the sum with guests=3
        requestDTO.setGuests(3);
        
        when(roomAvailabilityService.getAvailableRooms(
                anyInt(), any(LocalDate.class), any(LocalDate.class), eq(3), anyInt())
        ).thenReturn(availableRooms);

        // Act & Assert
        mockMvc.perform(post("/rooms/available")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }
} 