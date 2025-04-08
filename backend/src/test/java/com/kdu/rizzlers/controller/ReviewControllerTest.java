package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.TestBase;
import com.kdu.rizzlers.dto.ReviewSubmissionDTO;
import com.kdu.rizzlers.dto.ReviewValidationResponseDTO;
import com.kdu.rizzlers.entity.Review;
import com.kdu.rizzlers.service.GuestReviewService;
import com.kdu.rizzlers.service.ReviewInvitationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
public class ReviewControllerTest extends TestBase {

    @MockBean
    private ReviewInvitationService reviewInvitationService;

    @MockBean
    private GuestReviewService guestReviewService;

    private ReviewSubmissionDTO validReviewSubmission;
    private ReviewValidationResponseDTO validValidationResponse;
    private ReviewValidationResponseDTO invalidValidationResponse;
    private Review mockReview;

    @BeforeEach
    public void setup() {
        // Set up mock responses and objects
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

        validValidationResponse = ReviewValidationResponseDTO.builder()
                .valid(true)
                .bookingId(1001)
                .guestId(2001)
                .propertyId(1)
                .propertyName("Test Hotel")
                .roomTypeId(101)
                .guestName("John Doe")
                .build();

        invalidValidationResponse = ReviewValidationResponseDTO.builder()
                .valid(false)
                .message("Invalid token")
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
    }

    @Test
    public void testValidateToken_ValidToken() throws Exception {
        // Arrange
        when(reviewInvitationService.validateToken(anyString())).thenReturn(validValidationResponse);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/reviews/validate")
                .param("token", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.bookingId").value(1001))
                .andExpect(jsonPath("$.propertyName").value("Test Hotel"))
                .andDo(print());
    }

    @Test
    public void testValidateToken_InvalidToken() throws Exception {
        // Arrange
        when(reviewInvitationService.validateToken(anyString())).thenReturn(invalidValidationResponse);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/reviews/validate")
                .param("token", "invalid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.message").value("Invalid token"))
                .andDo(print());
    }

    @Test
    public void testSubmitReview_ValidReview() throws Exception {
        // Arrange
        when(reviewInvitationService.validateToken(anyString())).thenReturn(validValidationResponse);
        when(guestReviewService.submitReview(any(ReviewSubmissionDTO.class))).thenReturn(Optional.of(mockReview));

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/reviews/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(validReviewSubmission)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Thank you for your review!"))
                .andExpect(jsonPath("$.reviewId").value(1))
                .andDo(print());
    }

    @Test
    public void testSubmitReview_InvalidToken() throws Exception {
        // Arrange
        when(reviewInvitationService.validateToken(anyString())).thenReturn(invalidValidationResponse);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/reviews/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(validReviewSubmission)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid token"))
                .andDo(print());
    }

    @Test
    public void testSubmitReview_FailedSubmission() throws Exception {
        // Arrange
        when(reviewInvitationService.validateToken(anyString())).thenReturn(validValidationResponse);
        when(guestReviewService.submitReview(any(ReviewSubmissionDTO.class))).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/reviews/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(validReviewSubmission)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Failed to submit your review. This link may be invalid or expired."))
                .andDo(print());
    }

    @Test
    public void testSendInvitations_Success() throws Exception {
        // Arrange
        when(reviewInvitationService.sendReviewInvitationsForCheckoutDate(any(LocalDate.class))).thenReturn(5);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/reviews/send-invitations")
                .param("date", "2023-05-15")
                .param("apiKey", "secret-admin-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.sent").value(5))
                .andExpect(jsonPath("$.date").value("2023-05-15"))
                .andDo(print());
    }

    @Test
    public void testSendInvitations_InvalidApiKey() throws Exception {
        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/reviews/send-invitations")
                .param("date", "2023-05-15")
                .param("apiKey", "wrong-key"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Unauthorized"))
                .andDo(print());
    }

    @Test
    public void testSendInvitations_InvalidDate() throws Exception {
        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/reviews/send-invitations")
                .param("date", "invalid-date-format")
                .param("apiKey", "secret-admin-key"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid date format. Use YYYY-MM-DD."))
                .andDo(print());
    }
} 