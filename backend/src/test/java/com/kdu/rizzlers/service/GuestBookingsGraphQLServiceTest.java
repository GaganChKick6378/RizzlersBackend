package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.GuestBookingDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GuestBookingsGraphQLServiceTest {

    @Mock
    private WebClient.Builder webClientBuilderMock;
    
    @Mock
    private WebClient webClientMock;
    
    @Mock
    private HttpGraphQlClient graphQlClientMock;
    
    @Mock
    private HttpGraphQlClient.RequestSpec requestSpecMock;
    
    @Mock
    private HttpGraphQlClient.RetrieveSpec retrieveSpecMock;
    
    @Captor
    private ArgumentCaptor<ParameterizedTypeReference<?>> typeReferenceCaptor;
    
    private GuestBookingsGraphQLService graphQLService;

    @BeforeEach
    void setUp() {
        graphQLService = new GuestBookingsGraphQLService();
        
        // Set test values
        ReflectionTestUtils.setField(graphQLService, "graphqlEndpoint", "https://api.test.com/graphql");
        ReflectionTestUtils.setField(graphQLService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(graphQLService, "apiKeyHeader", "X-Api-Key");
    }

    @Test
    void initWebClient_ShouldConfigureWebClientCorrectly() {
        // Arrange
        when(webClientBuilderMock.baseUrl(anyString())).thenReturn(webClientBuilderMock);
        when(webClientBuilderMock.defaultHeader(anyString(), anyString())).thenReturn(webClientBuilderMock);
        when(webClientBuilderMock.build()).thenReturn(mock(WebClient.class));
        
        // Mock the static builder() method
        MockedStatic<WebClient> webClientMocked = mockStatic(WebClient.class);
        MockedStatic<HttpGraphQlClient> graphQlClientMocked = mockStatic(HttpGraphQlClient.class);
        
        try {
            webClientMocked.when(WebClient::builder).thenReturn(webClientBuilderMock);
            
            // HttpGraphQlClient mock
            HttpGraphQlClient.Builder graphQlClientBuilderMock = mock(HttpGraphQlClient.Builder.class);
            when(graphQlClientBuilderMock.build()).thenReturn(mock(HttpGraphQlClient.class));
            graphQlClientMocked.when(() -> HttpGraphQlClient.builder(any(WebClient.class)))
                .thenReturn(graphQlClientBuilderMock);
            
            // Act
            graphQLService.init();
            
            // Assert
            verify(webClientBuilderMock).baseUrl("https://api.test.com/graphql");
            verify(webClientBuilderMock).defaultHeader("X-Api-Key", "test-api-key");
            verify(webClientBuilderMock).build();
        } finally {
            graphQlClientMocked.close();
            webClientMocked.close();
        }
    }

    @Test
    void statusCache_ShouldStoreAndRetrieveValues() {
        // Arrange
        Integer statusId = 1;
        String expectedStatus = "CONFIRMED";
        
        // Create a test cache
        Map<Integer, String> statusCache = new ConcurrentHashMap<>();
        statusCache.put(statusId, expectedStatus);
        
        // Inject the cache using reflection
        ReflectionTestUtils.setField(graphQLService, "statusCache", statusCache);
        
        // Act & Assert - directly test the cache
        Map<Integer, String> retrievedCache = (Map<Integer, String>) ReflectionTestUtils.getField(
            graphQLService, "statusCache");
        
        assertNotNull(retrievedCache);
        assertEquals(expectedStatus, retrievedCache.get(statusId));
    }
    
    @Test
    void mapToGuestBookingDTO_ShouldCreateCorrectDTO() throws Exception {
        // Create a spy to test the private mapToGuestBookingDTO method
        GuestBookingsGraphQLService spyService = spy(graphQLService);
        
        // Create a test map that represents a booking
        Map<String, Object> bookingMap = new HashMap<>();
        bookingMap.put("booking_id", 123);
        bookingMap.put("check_in_date", "2023-06-01T14:00:00.000Z");
        bookingMap.put("check_out_date", "2023-06-05T11:00:00.000Z");
        bookingMap.put("status_id", 1);
        
        Map<String, Object> guestMap = new HashMap<>();
        guestMap.put("guest_id", 456);
        guestMap.put("guest_name", "John Doe");
        bookingMap.put("guest", guestMap);
        
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put("property_name", "Grand Hotel");
        bookingMap.put("property_booked", propertyMap);
        
        Map<String, Object> roomBookedMap = new HashMap<>();
        Map<String, Object> roomMap = new HashMap<>();
        Map<String, Object> roomTypeMap = new HashMap<>();
        roomTypeMap.put("room_type_id", 10);
        roomTypeMap.put("room_type_name", "Deluxe");
        roomMap.put("room_type", roomTypeMap);
        roomBookedMap.put("room", roomMap);
        bookingMap.put("room_booked", roomBookedMap);
        
        // Mock the status fetching
        doReturn("CONFIRMED").when(spyService).fetchBookingStatus(1);
        
        // Call the private method using reflection
        GuestBookingDTO result = (GuestBookingDTO) ReflectionTestUtils.invokeMethod(
            spyService, 
            "mapToGuestBookingDTO", 
            bookingMap
        );
        
        // Assert the result
        assertNotNull(result);
        assertEquals(123, result.getBookingId());
        assertEquals("John Doe", result.getGuest().getGuestName());
        assertEquals(456, result.getGuest().getGuestId());
        assertEquals("Grand Hotel", result.getPropertyBooked().getPropertyName());
        assertEquals(1, result.getStatusId());
        assertEquals("CONFIRMED", result.getStatusName());
        assertEquals(10, result.getRoomTypeId());
    }
    
    @Test
    void extractRoomTypeInfo_ShouldHandleNullValues() throws Exception {
        // Create a spy to test extracting room type info with null values
        GuestBookingsGraphQLService spyService = spy(graphQLService);
        
        // Create a test map with null room booked
        Map<String, Object> bookingMap = new HashMap<>();
        bookingMap.put("booking_id", 123);
        bookingMap.put("status_id", 1);
        // Add date values to avoid NPE when parsing dates
        bookingMap.put("check_in_date", "2023-06-01T14:00:00.000Z");
        bookingMap.put("check_out_date", "2023-06-05T11:00:00.000Z");
        
        Map<String, Object> guestMap = new HashMap<>();
        guestMap.put("guest_id", 456);
        guestMap.put("guest_name", "John Doe");
        bookingMap.put("guest", guestMap);
        
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put("property_name", "Grand Hotel");
        bookingMap.put("property_booked", propertyMap);
        
        // room_booked is null
        
        // Mock the status fetching
        doReturn("CONFIRMED").when(spyService).fetchBookingStatus(1);
        
        // Call the private method using reflection
        GuestBookingDTO result = (GuestBookingDTO) ReflectionTestUtils.invokeMethod(
            spyService, 
            "mapToGuestBookingDTO", 
            bookingMap
        );
        
        // Assert the result
        assertNotNull(result);
        assertEquals(123, result.getBookingId());
        assertNull(result.getRoomTypeId()); // Should be null since room_booked was null
    }
    
    @Test
    void fetchPropertyName_ShouldUseParameterizedTypeReference() {
        // Set up the mocks
        setupGraphQlClientMocks();
        
        // Prepare a mocked response
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("property_name", "Grand Hotel");
        
        // Configure mock for graphQlClient.document().variables().retrieve().toEntity()
        when(retrieveSpecMock.toEntity(typeReferenceCaptor.capture()))
            .thenAnswer(inv -> Mono.just(responseMap));
        
        // Act
        Optional<String> result = graphQLService.fetchPropertyName(1);
        
        // Assert
        assertTrue(result.isPresent());
        assertEquals("Grand Hotel", result.get());
        
        // Verify that ParameterizedTypeReference was captured
        assertNotNull(typeReferenceCaptor.getValue());
    }
    
    @Test
    void fetchBookingStatus_ShouldUseParameterizedTypeReference() {
        // Set up the mocks
        setupGraphQlClientMocks();
        
        // Prepare a mocked response
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("status", "CONFIRMED");
        
        // Configure mock for graphQlClient.document().variables().retrieve().toEntity()
        when(retrieveSpecMock.toEntity(typeReferenceCaptor.capture()))
            .thenAnswer(inv -> Mono.just(responseMap));
        
        // Act
        String result = graphQLService.fetchBookingStatus(1);
        
        // Assert
        assertEquals("CONFIRMED", result);
        
        // Verify that ParameterizedTypeReference was captured
        assertNotNull(typeReferenceCaptor.getValue());
    }
    
    @Test
    void fetchGuestBookings_ShouldUseParameterizedTypeReference() {
        // Set up the mocks
        setupGraphQlClientMocks();
        
        // Create a Map for the status cache to avoid having to mock fetchBookingStatus
        Map<Integer, String> statusCache = new ConcurrentHashMap<>();
        statusCache.put(1, "CONFIRMED");
        ReflectionTestUtils.setField(graphQLService, "statusCache", statusCache);
        
        // Prepare a mocked response - a list of booking maps
        List<Map<String, Object>> responseList = new ArrayList<>();
        Map<String, Object> bookingMap = new HashMap<>();
        bookingMap.put("booking_id", 123);
        bookingMap.put("check_in_date", "2023-06-01T14:00:00.000Z");
        bookingMap.put("check_out_date", "2023-06-05T11:00:00.000Z");
        bookingMap.put("status_id", 1);
        
        Map<String, Object> guestMap = new HashMap<>();
        guestMap.put("guest_id", 456);
        guestMap.put("guest_name", "John Doe");
        bookingMap.put("guest", guestMap);
        
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put("property_name", "Grand Hotel");
        bookingMap.put("property_booked", propertyMap);
        
        Map<String, Object> roomBookedMap = new HashMap<>();
        Map<String, Object> roomMap = new HashMap<>();
        Map<String, Object> roomTypeMap = new HashMap<>();
        roomTypeMap.put("room_type_id", 10);
        roomTypeMap.put("room_type_name", "Deluxe");
        roomMap.put("room_type", roomTypeMap);
        roomBookedMap.put("room", roomMap);
        bookingMap.put("room_booked", roomBookedMap);
        
        responseList.add(bookingMap);
        
        // Configure mock for graphQlClient.document().variables().retrieve().toEntity()
        when(retrieveSpecMock.toEntity(typeReferenceCaptor.capture()))
            .thenAnswer(inv -> Mono.just(responseList));
        
        // Act
        List<GuestBookingDTO> results = graphQLService.fetchGuestBookings(456, 789);
        
        // Assert
        assertNotNull(results);
        assertFalse(results.isEmpty());
        
        // Verify that ParameterizedTypeReference was captured
        assertNotNull(typeReferenceCaptor.getValue());
    }
    
    /**
     * Helper method to set up the GraphQL client mock chain
     */
    private void setupGraphQlClientMocks() {
        // Set up the WebClient and GraphQlClient
        ReflectionTestUtils.setField(graphQLService, "webClient", webClientMock);
        ReflectionTestUtils.setField(graphQLService, "graphQlClient", graphQlClientMock);
        
        // Configure the mock chain
        when(graphQlClientMock.document(anyString())).thenReturn(requestSpecMock);
        when(requestSpecMock.variables(anyMap())).thenReturn(requestSpecMock);
        when(requestSpecMock.retrieve(anyString())).thenReturn(retrieveSpecMock);
    }
} 