package com.kdu.rizzlers.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ReviewValidationResponseDTOTest {

    @Test
    public void testBuilder() {
        // Arrange & Act
        ReviewValidationResponseDTO dto = ReviewValidationResponseDTO.builder()
                .valid(true)
                .bookingId(1001)
                .guestId(2001)
                .propertyId(1)
                .propertyName("Test Hotel")
                .roomTypeId(101)
                .guestName("John Doe")
                .message("Success")
                .build();

        // Assert
        assertTrue(dto.isValid());
        assertEquals(1001, dto.getBookingId());
        assertEquals(2001, dto.getGuestId());
        assertEquals(1, dto.getPropertyId());
        assertEquals("Test Hotel", dto.getPropertyName());
        assertEquals(101, dto.getRoomTypeId());
        assertEquals("John Doe", dto.getGuestName());
        assertEquals("Success", dto.getMessage());
    }

    @Test
    public void testGettersAndSetters() {
        // Arrange
        ReviewValidationResponseDTO dto = new ReviewValidationResponseDTO();

        // Act
        dto.setValid(true);
        dto.setBookingId(1001);
        dto.setGuestId(2001);
        dto.setPropertyId(1);
        dto.setPropertyName("Test Hotel");
        dto.setRoomTypeId(101);
        dto.setGuestName("John Doe");
        dto.setMessage("Success");

        // Assert
        assertTrue(dto.isValid());
        assertEquals(1001, dto.getBookingId());
        assertEquals(2001, dto.getGuestId());
        assertEquals(1, dto.getPropertyId());
        assertEquals("Test Hotel", dto.getPropertyName());
        assertEquals(101, dto.getRoomTypeId());
        assertEquals("John Doe", dto.getGuestName());
        assertEquals("Success", dto.getMessage());
    }

    @Test
    public void testEqualsAndHashCode() {
        // Arrange
        ReviewValidationResponseDTO dto1 = ReviewValidationResponseDTO.builder()
                .valid(true)
                .bookingId(1001)
                .guestId(2001)
                .propertyId(1)
                .propertyName("Test Hotel")
                .build();

        ReviewValidationResponseDTO dto2 = ReviewValidationResponseDTO.builder()
                .valid(true)
                .bookingId(1001)
                .guestId(2001)
                .propertyId(1)
                .propertyName("Test Hotel")
                .build();

        ReviewValidationResponseDTO dto3 = ReviewValidationResponseDTO.builder()
                .valid(false)
                .bookingId(1002)
                .guestId(2002)
                .propertyId(2)
                .propertyName("Another Hotel")
                .build();

        // Act & Assert
        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1, dto3);
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }

    @Test
    public void testToString() {
        // Arrange
        ReviewValidationResponseDTO dto = ReviewValidationResponseDTO.builder()
                .valid(true)
                .bookingId(1001)
                .guestId(2001)
                .propertyName("Test Hotel")
                .build();

        // Act
        String toString = dto.toString();

        // Assert
        assertTrue(toString.contains("valid=true"));
        assertTrue(toString.contains("bookingId=1001"));
        assertTrue(toString.contains("guestId=2001"));
        assertTrue(toString.contains("propertyName=Test Hotel"));
    }
} 