package com.kdu.rizzlers.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kdu.rizzlers.dto.ReviewValidationResponseDTO;
import com.kdu.rizzlers.entity.ReviewInvitation;
import com.kdu.rizzlers.entity.User;
import com.kdu.rizzlers.repository.ReviewInvitationRepository;
import com.kdu.rizzlers.repository.UserRepository;
import com.kdu.rizzlers.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewInvitationServiceImplTest {

    @Mock
    private ReviewInvitationRepository reviewInvitationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private RestTemplate restTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private ReviewInvitationServiceImpl reviewInvitationService;

    private ReviewInvitation validInvitation;
    private ReviewInvitation expiredInvitation;
    private ReviewInvitation completedInvitation;
    private User mockUser;

    @BeforeEach
    public void setup() {
        // Set necessary values via reflection
        ReflectionTestUtils.setField(reviewInvitationService, "graphqlEndpoint", "http://test-api/graphql");
        ReflectionTestUtils.setField(reviewInvitationService, "apiKey", "test-api-key");
        ReflectionTestUtils.setField(reviewInvitationService, "apiKeyHeader", "x-api-key");
        ReflectionTestUtils.setField(reviewInvitationService, "frontendReviewUrl", "http://test-frontend/review");

        // Create test data with Long IDs
        validInvitation = ReviewInvitation.builder()
                .id(1L)
                .bookingId(1001)
                .guestId(2001)
                .guestEmail("test@example.com")
                .token("valid-token")
                .sentAt(ZonedDateTime.now().minusDays(1))
                .expiresAt(ZonedDateTime.now().plusDays(6))
                .isCompleted(false)
                .build();

        expiredInvitation = ReviewInvitation.builder()
                .id(2L)
                .bookingId(1002)
                .guestId(2002)
                .guestEmail("expired@example.com")
                .token("expired-token")
                .sentAt(ZonedDateTime.now().minusDays(10))
                .expiresAt(ZonedDateTime.now().minusDays(3))
                .isCompleted(false)
                .build();

        completedInvitation = ReviewInvitation.builder()
                .id(3L)
                .bookingId(1003)
                .guestId(2003)
                .guestEmail("completed@example.com")
                .token("completed-token")
                .sentAt(ZonedDateTime.now().minusDays(2))
                .expiresAt(ZonedDateTime.now().plusDays(5))
                .isCompleted(true)
                .build();

        // If User entity requires UUID, keep it as UUID
        UUID userId = UUID.randomUUID();
        mockUser = User.builder()
                .id(userId)
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();
    }

    @Test
    public void testValidateToken_ValidToken() {
        // Arrange
        when(reviewInvitationRepository.findByToken(anyString())).thenReturn(Optional.of(validInvitation));
        
        // Mock the bookingDetails retrieval - we would normally need to mock GraphQL response here
        // but for simplicity we'll just use reflection to bypass the actual GraphQL call
        ReflectionTestUtils.setField(validInvitation, "bookingDetails", "{\"propertyId\":1,\"propertyName\":\"Test Hotel\",\"roomTypeId\":101,\"roomTypeName\":\"Deluxe Room\",\"checkInDate\":\"2023-01-01\",\"checkOutDate\":\"2023-01-05\"}");

        // Act
        ReviewValidationResponseDTO result = reviewInvitationService.validateToken("valid-token");

        // Assert
        assertTrue(result.isValid());
        assertEquals(1001, result.getBookingId());
        assertEquals(2001, result.getGuestId());
        verify(reviewInvitationRepository).findByToken("valid-token");
    }

    @Test
    public void testValidateToken_InvalidToken() {
        // Arrange
        when(reviewInvitationRepository.findByToken(anyString())).thenReturn(Optional.empty());

        // Act
        ReviewValidationResponseDTO result = reviewInvitationService.validateToken("invalid-token");

        // Assert
        assertFalse(result.isValid());
        assertEquals("Invalid review token", result.getMessage());
    }

    @Test
    public void testValidateToken_ExpiredToken() {
        // Arrange
        when(reviewInvitationRepository.findByToken(anyString())).thenReturn(Optional.of(expiredInvitation));

        // Act
        ReviewValidationResponseDTO result = reviewInvitationService.validateToken("expired-token");

        // Assert
        assertFalse(result.isValid());
        assertEquals("This review invitation has expired", result.getMessage());
    }

    @Test
    public void testValidateToken_CompletedInvitation() {
        // Arrange
        when(reviewInvitationRepository.findByToken(anyString())).thenReturn(Optional.of(completedInvitation));

        // Act
        ReviewValidationResponseDTO result = reviewInvitationService.validateToken("completed-token");

        // Assert
        assertFalse(result.isValid());
        assertEquals("This review has already been submitted", result.getMessage());
    }

    @Test
    public void testMarkInvitationCompleted_Success() {
        // Arrange
        when(reviewInvitationRepository.findByToken(anyString())).thenReturn(Optional.of(validInvitation));
        when(reviewInvitationRepository.save(any(ReviewInvitation.class))).thenReturn(validInvitation);

        // Act
        boolean result = reviewInvitationService.markInvitationCompleted("valid-token");

        // Assert
        assertTrue(result);
        assertTrue(validInvitation.getIsCompleted());
        verify(reviewInvitationRepository).save(validInvitation);
    }

    @Test
    public void testMarkInvitationCompleted_InvalidToken() {
        // Arrange
        when(reviewInvitationRepository.findByToken(anyString())).thenReturn(Optional.empty());

        // Act
        boolean result = reviewInvitationService.markInvitationCompleted("invalid-token");

        // Assert
        assertFalse(result);
        verify(reviewInvitationRepository, never()).save(any(ReviewInvitation.class));
    }

    @Test
    public void testFindByToken_Success() {
        // Arrange
        when(reviewInvitationRepository.findByToken(anyString())).thenReturn(Optional.of(validInvitation));

        // Act
        Optional<ReviewInvitation> result = reviewInvitationService.findByToken("valid-token");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(validInvitation.getId(), result.get().getId());
    }

    @Test
    public void testFindByToken_NotFound() {
        // Arrange
        when(reviewInvitationRepository.findByToken(anyString())).thenReturn(Optional.empty());

        // Act
        Optional<ReviewInvitation> result = reviewInvitationService.findByToken("invalid-token");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    public void testSendReviewInvitationsForCheckoutDate_NoCheckouts() {
        // Arrange - Mock the private method using ReflectionTestUtils
        List<Object> emptyList = new ArrayList<>();
        ReflectionTestUtils.invokeMethod(reviewInvitationService, "getCheckoutsForDate", LocalDate.now());

        // Override the getCheckoutsForDate method to return an empty list
        ReflectionTestUtils.setField(reviewInvitationService, "getCheckoutsMethod", (CheckoutsProvider) date -> emptyList);

        // Act
        int result = reviewInvitationService.sendReviewInvitationsForCheckoutDate(LocalDate.now());

        // Assert
        assertEquals(0, result);
        verify(reviewInvitationRepository, never()).save(any(ReviewInvitation.class));
    }

    // This is a functional interface for mocking the private method
    @FunctionalInterface
    private interface CheckoutsProvider {
        List<?> getCheckouts(LocalDate date);
    }
} 