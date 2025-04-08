package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.dto.BookingDetailsDTO;
import com.kdu.rizzlers.dto.TravelItineraryDto;
import com.kdu.rizzlers.dto.out.BookingConfirmationDetailsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailServiceImpl emailService;

    private TravelItineraryDto itineraryDto;
    private BookingConfirmationDetailsResponse bookingConfirmationDetails;
    private BookingDetailsDTO bookingDetails;

    @BeforeEach
    public void setup() throws MessagingException {
        // Set up sender email via reflection
        ReflectionTestUtils.setField(emailService, "senderEmail", "test@example.com");

        // Mock mailSender to return mimeMessage
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        
        // Mock templateEngine to return some content
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html><body>Template content</body></html>");

        // Create test data
        setupTestData();
    }

    private void setupTestData() {
        // Set up TravelItineraryDto
        itineraryDto = new TravelItineraryDto();
        itineraryDto.setBookingId("12345");
        itineraryDto.setBookingDate(LocalDate.now());
        itineraryDto.setTravelerName("John Doe");
        itineraryDto.setTravelerEmail("john.doe@example.com");
        itineraryDto.setTravelerPhone("123-456-7890");
        itineraryDto.setCheckInDate(LocalDate.now().plusDays(30));
        itineraryDto.setCheckOutDate(LocalDate.now().plusDays(35));
        itineraryDto.setDestination("Test Resort");
        itineraryDto.setTotalAmount(new BigDecimal("1000.0"));
        itineraryDto.setAmountPaid(new BigDecimal("500.0"));
        itineraryDto.setAmountDue(new BigDecimal("500.0"));
        itineraryDto.setPromotionApplied(new BigDecimal("100.0"));
        itineraryDto.setPaymentMethod("Credit Card");
        itineraryDto.setBillingAddress("123 Main St, City, Country");

        // Set up BookingConfirmationDetailsResponse (simplified for testing)
        bookingConfirmationDetails = new BookingConfirmationDetailsResponse();
        
        // Create necessary nested objects - these would need to be defined based on the actual DTO structure
        bookingConfirmationDetails.setBookingDetails(new BookingConfirmationDetailsResponse.BookingDetails());
        bookingConfirmationDetails.getBookingDetails().setBookingId(12345);
        bookingConfirmationDetails.getBookingDetails().setCheckInDate(LocalDate.now().plusDays(30));
        bookingConfirmationDetails.getBookingDetails().setCheckOutDate(LocalDate.now().plusDays(35));
        bookingConfirmationDetails.getBookingDetails().setRoomTypeName("Deluxe Room");
        bookingConfirmationDetails.getBookingDetails().setRoomImage("https://example.com/room.jpg");
        
        bookingConfirmationDetails.setGuestInformation(new BookingConfirmationDetailsResponse.GuestInformation());
        bookingConfirmationDetails.getGuestInformation().setFirstName("John");
        bookingConfirmationDetails.getGuestInformation().setLastName("Doe");
        bookingConfirmationDetails.getGuestInformation().setEmail("john.doe@example.com");
        bookingConfirmationDetails.getGuestInformation().setPhone("123-456-7890");
        
        bookingConfirmationDetails.setPaymentInformation(new BookingConfirmationDetailsResponse.PaymentInformation());
        bookingConfirmationDetails.getPaymentInformation().setMaskedCardNumber("**** **** **** 1234");
        
        bookingConfirmationDetails.setRoomTotalSummary(new BookingConfirmationDetailsResponse.RoomTotalSummary());
        bookingConfirmationDetails.getRoomTotalSummary().setTotalForStay(new BigDecimal("1000.0"));

        // Set up BookingDetailsDTO for OTP testing
        bookingDetails = new BookingDetailsDTO();
        bookingDetails.setBookingId(12345);
        bookingDetails.setPropertyName("Test Resort");
        bookingDetails.setCheckInDate(ZonedDateTime.now().plusDays(30));
        bookingDetails.setCheckOutDate(ZonedDateTime.now().plusDays(35));
        bookingDetails.setGuestName("John Doe");
    }

    @Test
    public void testSendTravelItineraryEmail_WithLegacyDto_Success() throws MessagingException {
        // Act
        boolean result = emailService.sendTravelItineraryEmail(itineraryDto, "recipient@example.com");
        
        // Assert
        assertTrue(result);
        verify(mailSender, times(1)).send(mimeMessage);
        verify(templateEngine, times(1)).process(eq("travel-itinerary-email"), any(Context.class));
    }

    @Test
    public void testSendTravelItineraryEmail_WithBookingDetails_Success() throws MessagingException {
        // Act
        boolean result = emailService.sendTravelItineraryEmail(bookingConfirmationDetails, "recipient@example.com");
        
        // Assert
        assertTrue(result);
        verify(mailSender, times(1)).send(mimeMessage);
        verify(templateEngine, times(1)).process(eq("travel-itinerary-email"), any(Context.class));
    }

    @Test
    public void testSendTravelItineraryEmail_WithException() throws MessagingException {
        // Arrange
        doThrow(new MessagingException("Messaging error")).when(mailSender).send(mimeMessage);
        
        // Act
        boolean result = emailService.sendTravelItineraryEmail(itineraryDto, "recipient@example.com");
        
        // Assert
        assertFalse(result);
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    public void testSendOtpEmail() {
        // Arrange & Act - no return value to check
        emailService.sendOtpEmail("recipient@example.com", "123456", bookingDetails);
        
        // Assert
        verify(mailSender, times(1)).send(mimeMessage);
        verify(templateEngine, times(1)).process(eq("booking-cancellation-otp"), any(Context.class));
    }

    @Test
    public void testSendMyBookingsOtpEmail() {
        // Arrange & Act - no return value to check
        emailService.sendMyBookingsOtpEmail("recipient@example.com", "123456", "John Doe", "Test Resort");
        
        // Assert
        verify(mailSender, times(1)).send(mimeMessage);
        verify(templateEngine, times(1)).process(eq("my-bookings-otp"), any(Context.class));
    }

    @Test
    public void testSendReviewInvitationEmail_Success() {
        // Act
        boolean result = emailService.sendReviewInvitationEmail(
                "recipient@example.com",
                "https://example.com/review?token=abc123",
                "John Doe",
                "Test Resort",
                7);
        
        // Assert
        assertTrue(result);
        verify(mailSender, times(1)).send(mimeMessage);
        verify(templateEngine, times(1)).process(eq("review-invitation"), any(Context.class));
    }

    @Test
    public void testSendReviewInvitationEmail_Exception() throws MessagingException {
        // Arrange
        doThrow(new MessagingException("Messaging error")).when(mailSender).send(mimeMessage);
        
        // Act
        boolean result = emailService.sendReviewInvitationEmail(
                "recipient@example.com",
                "https://example.com/review?token=abc123",
                "John Doe",
                "Test Resort",
                7);
        
        // Assert
        assertFalse(result);
        verify(mailSender, times(1)).send(mimeMessage);
    }
} 