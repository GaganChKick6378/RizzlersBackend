package com.kdu.rizzlers.entity;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class ReviewTest {

    @Test
    public void testBuilder() {
        // Arrange
        ZonedDateTime now = ZonedDateTime.now();
        String[] images = new String[]{"image1.jpg", "image2.jpg"};

        // Act
        Review review = Review.builder()
                .id(1L)
                .invitationId(2L)
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
                .images(images)
                .submittedAt(now)
                .build();

        // Assert
        assertEquals(1L, review.getId());
        assertEquals(2L, review.getInvitationId());
        assertEquals(1001, review.getBookingId());
        assertEquals(2001, review.getGuestId());
        assertEquals(1, review.getPropertyId());
        assertEquals(101, review.getRoomTypeId());
        assertEquals(5, review.getCleanlinessRating());
        assertEquals(4, review.getStaffServiceRating());
        assertEquals(4, review.getComfortRating());
        assertEquals(5, review.getLocationRating());
        assertEquals(4, review.getValueRating());
        assertEquals(5, review.getOverallRating());
        assertEquals("Great stay!", review.getComment());
        assertArrayEquals(images, review.getImages());
        assertEquals(now, review.getSubmittedAt());
    }

    @Test
    public void testNoArgsConstructor() {
        // Act
        Review review = new Review();

        // Assert
        assertNull(review.getId());
        assertNull(review.getInvitationId());
        assertNull(review.getBookingId());
        assertNull(review.getGuestId());
        assertNull(review.getPropertyId());
        assertNull(review.getRoomTypeId());
        assertNull(review.getCleanlinessRating());
        assertNull(review.getStaffServiceRating());
        assertNull(review.getComfortRating());
        assertNull(review.getLocationRating());
        assertNull(review.getValueRating());
        assertNull(review.getOverallRating());
        assertNull(review.getComment());
        assertNull(review.getImages());
        assertNull(review.getSubmittedAt());
    }

    @Test
    public void testGettersAndSetters() {
        // Arrange
        Review review = new Review();
        ZonedDateTime now = ZonedDateTime.now();
        String[] images = new String[]{"image1.jpg", "image2.jpg"};

        // Act
        review.setId(1L);
        review.setInvitationId(2L);
        review.setBookingId(1001);
        review.setGuestId(2001);
        review.setPropertyId(1);
        review.setRoomTypeId(101);
        review.setCleanlinessRating(5);
        review.setStaffServiceRating(4);
        review.setComfortRating(4);
        review.setLocationRating(5);
        review.setValueRating(4);
        review.setOverallRating(5);
        review.setComment("Great stay!");
        review.setImages(images);
        review.setSubmittedAt(now);

        // Assert
        assertEquals(1L, review.getId());
        assertEquals(2L, review.getInvitationId());
        assertEquals(1001, review.getBookingId());
        assertEquals(2001, review.getGuestId());
        assertEquals(1, review.getPropertyId());
        assertEquals(101, review.getRoomTypeId());
        assertEquals(5, review.getCleanlinessRating());
        assertEquals(4, review.getStaffServiceRating());
        assertEquals(4, review.getComfortRating());
        assertEquals(5, review.getLocationRating());
        assertEquals(4, review.getValueRating());
        assertEquals(5, review.getOverallRating());
        assertEquals("Great stay!", review.getComment());
        assertArrayEquals(images, review.getImages());
        assertEquals(now, review.getSubmittedAt());
    }

    @Test
    public void testEqualsAndHashCode() {
        // Arrange
        Review review1 = Review.builder()
                .id(1L)
                .bookingId(1001)
                .guestId(2001)
                .build();

        Review review2 = Review.builder()
                .id(1L)
                .bookingId(1001)
                .guestId(2001)
                .build();

        Review review3 = Review.builder()
                .id(2L)
                .bookingId(1002)
                .guestId(2002)
                .build();

        // Act & Assert
        assertEquals(review1, review2);
        assertEquals(review1.hashCode(), review2.hashCode());
        assertNotEquals(review1, review3);
        assertNotEquals(review1.hashCode(), review3.hashCode());
    }

    @Test
    public void testToString() {
        // Arrange
        Review review = Review.builder()
                .id(1L)
                .bookingId(1001)
                .guestId(2001)
                .cleanlinessRating(5)
                .build();

        // Act
        String toString = review.toString();

        // Assert
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("bookingId=1001"));
        assertTrue(toString.contains("guestId=2001"));
        assertTrue(toString.contains("cleanlinessRating=5"));
    }
} 