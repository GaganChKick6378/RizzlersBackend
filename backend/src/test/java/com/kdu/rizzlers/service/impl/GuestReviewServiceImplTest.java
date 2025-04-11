package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.dto.ReviewSubmissionDTO;
import com.kdu.rizzlers.dto.ReviewValidationResponseDTO;
import com.kdu.rizzlers.entity.Review;
import com.kdu.rizzlers.entity.ReviewInvitation;
import com.kdu.rizzlers.repository.GuestReviewRepository;
import com.kdu.rizzlers.repository.ReviewInvitationRepository;
import com.kdu.rizzlers.service.ReviewInvitationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GuestReviewServiceImplTest {

    @Mock
    private GuestReviewRepository guestReviewRepository;

    @Mock
    private ReviewInvitationRepository reviewInvitationRepository;

    @Mock
    private ReviewInvitationService reviewInvitationService;

    @InjectMocks
    private GuestReviewServiceImpl guestReviewService;

    private ReviewSubmissionDTO validReviewSubmission;
    private ReviewInvitation validInvitation;
    private Review mockReview;
    private ReviewValidationResponseDTO validValidationResponse;

    @BeforeEach
    public void setUp() {
        // Set up test data
        validReviewSubmission = ReviewSubmissionDTO.builder()
                .token("valid-token")
                .propertyId(1)
                .roomTypeId(101)
                .cleanlinessRating(5)
                .staffServiceRating(4)
                .comfortRating(4)
                .locationRating(5)
                .valueRating(4)
                .overallRating(5)
                .comment("Great stay!")
                .images(new String[]{"image1.jpg", "image2.jpg"})
                .build();

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

        mockReview = Review.builder()
                .id(1L)
                .invitationId(1L)
                .bookingId(1001)
                .guestId(2001)
                .propertyId(1)
                .roomTypeId(101)
                .cleanlinessRating(5)
                .staffServiceRating(4)
                .comfortRating(4)
                .locationRating(5)
                .valueRating(4)
                .overallRating(5)
                .comment("Great stay!")
                .build();

        validValidationResponse = ReviewValidationResponseDTO.builder()
                .valid(true)
                .bookingId(1001)
                .guestId(2001)
                .propertyId(1)
                .propertyName("Test Hotel")
                .roomTypeId(101)
                .roomTypeName("Deluxe Room")
                .checkInDate("2023-01-01")
                .checkOutDate("2023-01-05")
                .build();
    }

    @Test
    public void testSubmitReview_Success() {
        // Arrange
        when(reviewInvitationRepository.findByToken(anyString())).thenReturn(Optional.of(validInvitation));
        when(guestReviewRepository.existsByInvitationId(anyLong())).thenReturn(false);
        when(guestReviewRepository.save(any(Review.class))).thenReturn(mockReview);
        when(reviewInvitationService.markInvitationCompleted(anyString())).thenReturn(true);

        // Act
        Optional<Review> result = guestReviewService.submitReview(validReviewSubmission);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals(1001, result.get().getBookingId());
        assertEquals(2001, result.get().getGuestId());
        assertEquals(5, result.get().getOverallRating());
        
        verify(guestReviewRepository).save(any(Review.class));
        verify(reviewInvitationService).markInvitationCompleted("valid-token");
    }

    @Test
    public void testSubmitReview_NullToken() {
        // Arrange
        validReviewSubmission.setToken(null);

        // Act
        Optional<Review> result = guestReviewService.submitReview(validReviewSubmission);

        // Assert
        assertTrue(result.isEmpty());
        verify(guestReviewRepository, never()).save(any(Review.class));
    }

    @Test
    public void testSubmitReview_InvalidToken() {
        // Arrange
        when(reviewInvitationRepository.findByToken(anyString())).thenReturn(Optional.empty());

        // Act
        Optional<Review> result = guestReviewService.submitReview(validReviewSubmission);

        // Assert
        assertTrue(result.isEmpty());
        verify(guestReviewRepository, never()).save(any(Review.class));
    }

    @Test
    public void testSubmitReview_AlreadyCompleted() {
        // Arrange
        validInvitation.setIsCompleted(true);
        when(reviewInvitationRepository.findByToken(anyString())).thenReturn(Optional.of(validInvitation));

        // Act
        Optional<Review> result = guestReviewService.submitReview(validReviewSubmission);

        // Assert
        assertTrue(result.isEmpty());
        verify(guestReviewRepository, never()).save(any(Review.class));
    }

    @Test
    public void testSubmitReview_Expired() {
        // Arrange
        validInvitation.setExpiresAt(ZonedDateTime.now().minusDays(1));
        when(reviewInvitationRepository.findByToken(anyString())).thenReturn(Optional.of(validInvitation));

        // Act
        Optional<Review> result = guestReviewService.submitReview(validReviewSubmission);

        // Assert
        assertTrue(result.isEmpty());
        verify(guestReviewRepository, never()).save(any(Review.class));
    }

    @Test
    public void testSubmitReview_ReviewAlreadyExists() {
        // Arrange
        when(reviewInvitationRepository.findByToken(anyString())).thenReturn(Optional.of(validInvitation));
        when(guestReviewRepository.existsByInvitationId(anyLong())).thenReturn(true);

        // Act
        Optional<Review> result = guestReviewService.submitReview(validReviewSubmission);

        // Assert
        assertTrue(result.isEmpty());
        verify(guestReviewRepository, never()).save(any(Review.class));
    }

    @Test
    public void testSubmitReview_MissingPropertyAndRoomType() {
        // Arrange
        validReviewSubmission.setPropertyId(null);
        validReviewSubmission.setRoomTypeId(null);
        
        when(reviewInvitationRepository.findByToken(anyString())).thenReturn(Optional.of(validInvitation));
        when(guestReviewRepository.existsByInvitationId(anyLong())).thenReturn(false);
        when(reviewInvitationService.validateToken(anyString())).thenReturn(validValidationResponse);
        when(guestReviewRepository.save(any(Review.class))).thenReturn(mockReview);

        // Act
        Optional<Review> result = guestReviewService.submitReview(validReviewSubmission);

        // Assert
        assertTrue(result.isPresent());
        verify(reviewInvitationService).validateToken("valid-token");
        verify(guestReviewRepository).save(any(Review.class));
    }

    @Test
    public void testGetReviewsByRoomType_Success() {
        // Arrange
        List<Review> expectedReviews = Arrays.asList(mockReview);
        when(guestReviewRepository.findByRoomTypeId(anyInt())).thenReturn(expectedReviews);

        // Act
        List<Review> result = guestReviewService.getReviewsByRoomType(101);

        // Assert
        assertEquals(1, result.size());
        assertEquals(mockReview.getId(), result.get(0).getId());
        verify(guestReviewRepository).findByRoomTypeId(101);
    }

    @Test
    public void testGetReviewsByRoomType_NullRoomTypeId() {
        // Act
        List<Review> result = guestReviewService.getReviewsByRoomType(null);

        // Assert
        assertTrue(result.isEmpty());
        verify(guestReviewRepository, never()).findByRoomTypeId(anyInt());
    }

    @Test
    public void testGetReviewsByRoomType_ExceptionHandling() {
        // Arrange
        when(guestReviewRepository.findByRoomTypeId(anyInt())).thenThrow(new RuntimeException("Database error"));

        // Act
        List<Review> result = guestReviewService.getReviewsByRoomType(101);

        // Assert
        assertTrue(result.isEmpty());
        verify(guestReviewRepository).findByRoomTypeId(101);
    }

    @Test
    public void testGetReviewsByProperty_Success() {
        // Arrange
        List<Review> expectedReviews = Arrays.asList(mockReview);
        when(guestReviewRepository.findByPropertyId(anyInt())).thenReturn(expectedReviews);

        // Act
        List<Review> result = guestReviewService.getReviewsByProperty(1);

        // Assert
        assertEquals(1, result.size());
        assertEquals(mockReview.getId(), result.get(0).getId());
        verify(guestReviewRepository).findByPropertyId(1);
    }

    @Test
    public void testGetReviewsByProperty_NullPropertyId() {
        // Act
        List<Review> result = guestReviewService.getReviewsByProperty(null);

        // Assert
        assertTrue(result.isEmpty());
        verify(guestReviewRepository, never()).findByPropertyId(anyInt());
    }

    @Test
    public void testGetReviewsByProperty_ExceptionHandling() {
        // Arrange
        when(guestReviewRepository.findByPropertyId(anyInt())).thenThrow(new RuntimeException("Database error"));

        // Act
        List<Review> result = guestReviewService.getReviewsByProperty(1);

        // Assert
        assertTrue(result.isEmpty());
        verify(guestReviewRepository).findByPropertyId(1);
    }
} 