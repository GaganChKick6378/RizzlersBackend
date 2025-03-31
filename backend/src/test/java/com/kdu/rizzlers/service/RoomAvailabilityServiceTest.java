package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.common.PageResponse;
import com.kdu.rizzlers.dto.in.RoomAvailabilityRequestDTO.FilterDTO;
import com.kdu.rizzlers.dto.out.AvailableRoomDTO;
import com.kdu.rizzlers.service.impl.RoomAvailabilityServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomAvailabilityServiceTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private RoomRateService roomRateService;

    @InjectMocks
    private RoomAvailabilityServiceImpl roomAvailabilityService;

    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        when(webClientBuilder.build()).thenReturn(webClient);
        startDate = LocalDate.now();
        endDate = startDate.plusDays(3);
    }

    @Test
    @DisplayName("RoomAvailabilityService interface is correctly implemented")
    void testInterface() {
        // This test verifies that RoomAvailabilityServiceImpl implements RoomAvailabilityService
        RoomAvailabilityService service = roomAvailabilityService;
        assertNotNull(service);
    }

    @Test
    @DisplayName("getAvailableRooms should return empty list when API call fails")
    void getAvailableRooms_whenApiCallFails_shouldReturnEmptyList() {
        // No need to set up the WebClient mocking since we want it to fail

        List<AvailableRoomDTO> result = roomAvailabilityService.getAvailableRooms(
                1, startDate, endDate, 2, 1);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getAvailableRoomsPaginated should return empty page when API call fails")
    void getAvailableRoomsPaginated_whenApiCallFails_shouldReturnEmptyPage() {
        // No need to set up the WebClient mocking since we want it to fail

        PageResponse<AvailableRoomDTO> result = roomAvailabilityService.getAvailableRoomsPaginated(
                1, startDate, endDate, 2, 1, 0, 10);
        
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getTotalPages());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    @DisplayName("getAvailableRoomsWithFilters should return empty page when API call fails")
    void getAvailableRoomsWithFilters_whenApiCallFails_shouldReturnEmptyPage() {
        // No need to set up the WebClient mocking since we want it to fail
        FilterDTO filters = new FilterDTO();

        PageResponse<AvailableRoomDTO> result = roomAvailabilityService.getAvailableRoomsWithFilters(
                1, startDate, endDate, 2, 1, filters, 0, 10);
        
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getTotalPages());
        assertTrue(result.getContent().isEmpty());
    }
} 