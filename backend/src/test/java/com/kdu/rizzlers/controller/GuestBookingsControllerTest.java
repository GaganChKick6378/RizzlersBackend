package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.TestBase;
import com.kdu.rizzlers.dto.GuestBookingDTO;
import com.kdu.rizzlers.dto.GuestBookingsResponseDTO;
import com.kdu.rizzlers.dto.MyBookingsOtpRequestDTO;
import com.kdu.rizzlers.dto.MyBookingsOtpVerificationDTO;
import com.kdu.rizzlers.service.GuestBookingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
public class GuestBookingsControllerTest extends TestBase {

    @MockBean
    private GuestBookingsService guestBookingsService;

    private MyBookingsOtpRequestDTO otpRequestDTO;
    private MyBookingsOtpVerificationDTO otpVerificationDTO;
    private GuestBookingsResponseDTO successResponseDTO;
    private GuestBookingsResponseDTO errorResponseDTO;
    private GuestBookingsResponseDTO bookingsResponseDTO;

    @BeforeEach
    public void setup() {
        // Set up test data
        otpRequestDTO = new MyBookingsOtpRequestDTO();
        otpRequestDTO.setEmail("test@example.com");

        otpVerificationDTO = new MyBookingsOtpVerificationDTO();
        otpVerificationDTO.setEmail("test@example.com");
        otpVerificationDTO.setOtp("123456");

        successResponseDTO = GuestBookingsResponseDTO.builder()
                .success(true)
                .message("OTP sent successfully")
                .build();

        errorResponseDTO = GuestBookingsResponseDTO.builder()
                .success(false)
                .message("Invalid OTP")
                .build();

        List<GuestBookingDTO> bookings = Arrays.asList(
                GuestBookingDTO.builder()
                        .bookingId(12345)
                        .checkInDate(ZonedDateTime.now().minusDays(5))
                        .checkOutDate(ZonedDateTime.now())
                        .guest(GuestBookingDTO.GuestDTO.builder().guestName("John Doe").guestId(1).build())
                        .propertyBooked(GuestBookingDTO.PropertyDTO.builder().propertyName("Test Hotel").build())
                        .statusId(1)
                        .statusName("Confirmed")
                        .roomTypeId(101)
                        .roomImage("room.jpg")
                        .build(),
                GuestBookingDTO.builder()
                        .bookingId(12346)
                        .checkInDate(ZonedDateTime.now().plusDays(10))
                        .checkOutDate(ZonedDateTime.now().plusDays(15))
                        .guest(GuestBookingDTO.GuestDTO.builder().guestName("John Doe").guestId(1).build())
                        .propertyBooked(GuestBookingDTO.PropertyDTO.builder().propertyName("Another Hotel").build())
                        .statusId(1)
                        .statusName("Confirmed")
                        .roomTypeId(102)
                        .roomImage("suite.jpg")
                        .build()
        );

        bookingsResponseDTO = GuestBookingsResponseDTO.builder()
                .success(true)
                .message("Bookings retrieved successfully")
                .bookings(bookings)
                .build();
    }

    @Test
    public void testRequestOtp_Success() throws Exception {
        // Arrange
        when(guestBookingsService.requestOtp(any(MyBookingsOtpRequestDTO.class)))
                .thenReturn(successResponseDTO);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/guest-bookings/request-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(otpRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OTP sent successfully"))
                .andDo(print());
    }

    @Test
    public void testRequestOtp_Error() throws Exception {
        // Arrange
        GuestBookingsResponseDTO errorResponse = GuestBookingsResponseDTO.builder()
                .success(false)
                .message("Email not found")
                .build();

        when(guestBookingsService.requestOtp(any(MyBookingsOtpRequestDTO.class)))
                .thenReturn(errorResponse);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/guest-bookings/request-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(otpRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email not found"))
                .andDo(print());
    }

    @Test
    public void testVerifyOtpAndGetBookings_Success() throws Exception {
        // Arrange
        when(guestBookingsService.verifyOtpAndGetBookings(any(MyBookingsOtpVerificationDTO.class)))
                .thenReturn(bookingsResponseDTO);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/guest-bookings/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(otpVerificationDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Bookings retrieved successfully"))
                .andExpect(jsonPath("$.bookings").isArray())
                .andExpect(jsonPath("$.bookings.length()").value(2))
                .andExpect(jsonPath("$.bookings[0].bookingId").value(12345))
                .andExpect(jsonPath("$.bookings[1].bookingId").value(12346))
                .andDo(print());
    }

    @Test
    public void testVerifyOtpAndGetBookings_InvalidOtp() throws Exception {
        // Arrange
        when(guestBookingsService.verifyOtpAndGetBookings(any(MyBookingsOtpVerificationDTO.class)))
                .thenReturn(errorResponseDTO);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/guest-bookings/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(otpVerificationDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid OTP"))
                .andDo(print());
    }

    @Test
    public void testRequestOtp_InvalidInput() throws Exception {
        // Arrange - Create an invalid request (missing email)
        MyBookingsOtpRequestDTO invalidRequest = new MyBookingsOtpRequestDTO();
        // Email is required but missing

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/guest-bookings/request-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    public void testVerifyOtp_InvalidInput() throws Exception {
        // Arrange - Create an invalid verification request (missing OTP)
        MyBookingsOtpVerificationDTO invalidRequest = new MyBookingsOtpVerificationDTO();
        invalidRequest.setEmail("test@example.com");
        // OTP is required but missing

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/guest-bookings/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }
} 