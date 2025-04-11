package com.kdu.rizzlers.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ReviewSubmissionDTOTest {

    @Test
    public void testBuilder() {
        // Arrange & Act
        ReviewSubmissionDTO dto = ReviewSubmissionDTO.builder()
                .token("test-token")
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

        // Assert
        assertEquals("test-token", dto.getToken());
        assertEquals(1, dto.getPropertyId());
        assertEquals(101, dto.getRoomTypeId());
        assertEquals(5, dto.getCleanlinessRating());
        assertEquals(4, dto.getStaffServiceRating());
        assertEquals(4, dto.getComfortRating());
        assertEquals(5, dto.getLocationRating());
        assertEquals(4, dto.getValueRating());
        assertEquals(5, dto.getOverallRating());
        assertEquals("Great stay!", dto.getComment());
        assertEquals(2, dto.getImages().length);
        assertEquals("image1.jpg", dto.getImages()[0]);
        assertEquals("image2.jpg", dto.getImages()[1]);
    }

    @Test
    public void testGettersAndSetters() {
        // Arrange
        ReviewSubmissionDTO dto = new ReviewSubmissionDTO();

        // Act
        dto.setToken("test-token");
        dto.setPropertyId(1);
        dto.setRoomTypeId(101);
        dto.setCleanlinessRating(5);
        dto.setStaffServiceRating(4);
        dto.setComfortRating(4);
        dto.setLocationRating(5);
        dto.setValueRating(4);
        dto.setOverallRating(5);
        dto.setComment("Great stay!");
        dto.setImages(new String[]{"image1.jpg", "image2.jpg"});

        // Assert
        assertEquals("test-token", dto.getToken());
        assertEquals(1, dto.getPropertyId());
        assertEquals(101, dto.getRoomTypeId());
        assertEquals(5, dto.getCleanlinessRating());
        assertEquals(4, dto.getStaffServiceRating());
        assertEquals(4, dto.getComfortRating());
        assertEquals(5, dto.getLocationRating());
        assertEquals(4, dto.getValueRating());
        assertEquals(5, dto.getOverallRating());
        assertEquals("Great stay!", dto.getComment());
        assertEquals(2, dto.getImages().length);
        assertEquals("image1.jpg", dto.getImages()[0]);
        assertEquals("image2.jpg", dto.getImages()[1]);
    }

    @Test
    public void testEqualsAndHashCode() {
        // Arrange
        ReviewSubmissionDTO dto1 = ReviewSubmissionDTO.builder()
                .token("test-token")
                .cleanlinessRating(5)
                .staffServiceRating(4)
                .build();

        ReviewSubmissionDTO dto2 = ReviewSubmissionDTO.builder()
                .token("test-token")
                .cleanlinessRating(5)
                .staffServiceRating(4)
                .build();

        ReviewSubmissionDTO dto3 = ReviewSubmissionDTO.builder()
                .token("different-token")
                .cleanlinessRating(3)
                .staffServiceRating(3)
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
        ReviewSubmissionDTO dto = ReviewSubmissionDTO.builder()
                .token("test-token")
                .cleanlinessRating(5)
                .overallRating(5)
                .build();

        // Act
        String toString = dto.toString();

        // Assert
        assertTrue(toString.contains("token=test-token"));
        assertTrue(toString.contains("cleanlinessRating=5"));
        assertTrue(toString.contains("overallRating=5"));
    }

    @Test
    public void testNoArgsConstructor() {
        // Act
        ReviewSubmissionDTO dto = new ReviewSubmissionDTO();

        // Assert
        assertNull(dto.getToken());
        assertNull(dto.getPropertyId());
        assertNull(dto.getRoomTypeId());
        assertNull(dto.getCleanlinessRating());
        assertNull(dto.getStaffServiceRating());
        assertNull(dto.getComfortRating());
        assertNull(dto.getLocationRating());
        assertNull(dto.getValueRating());
        assertNull(dto.getOverallRating());
        assertNull(dto.getComment());
        assertNull(dto.getImages());
    }

    @Test
    public void testAllArgsConstructor() {
        // Arrange
        String token = "test-token";
        Integer propertyId = 1;
        Integer roomTypeId = 101;
        Integer cleanlinessRating = 5;
        Integer staffServiceRating = 4;
        Integer comfortRating = 4;
        Integer locationRating = 5;
        Integer valueRating = 4;
        Integer overallRating = 5;
        String comment = "Great stay!";
        String[] images = new String[]{"image1.jpg", "image2.jpg"};

        // Act
        ReviewSubmissionDTO dto = new ReviewSubmissionDTO(
                token, propertyId, roomTypeId, cleanlinessRating, staffServiceRating,
                comfortRating, locationRating, valueRating, overallRating, comment, images
        );

        // Assert
        assertEquals(token, dto.getToken());
        assertEquals(propertyId, dto.getPropertyId());
        assertEquals(roomTypeId, dto.getRoomTypeId());
        assertEquals(cleanlinessRating, dto.getCleanlinessRating());
        assertEquals(staffServiceRating, dto.getStaffServiceRating());
        assertEquals(comfortRating, dto.getComfortRating());
        assertEquals(locationRating, dto.getLocationRating());
        assertEquals(valueRating, dto.getValueRating());
        assertEquals(overallRating, dto.getOverallRating());
        assertEquals(comment, dto.getComment());
        assertArrayEquals(images, dto.getImages());
    }
} 