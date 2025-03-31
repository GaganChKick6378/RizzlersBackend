package com.kdu.rizzlers.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class RoomTypeImageTest {

    @Test
    @DisplayName("Should create a room type image with builder pattern")
    void testBuilder() {
        // Arrange
        Long id = 1L;
        Integer tenantId = 100;
        Integer roomTypeId = 200;
        Integer propertyId = 300;
        String imageUrlsJson = "[\"image1.jpg\",\"image2.jpg\"]";
        Integer displayOrder = 1;

        // Act
        RoomTypeImage roomTypeImage = RoomTypeImage.builder()
                .id(id)
                .tenantId(tenantId)
                .roomTypeId(roomTypeId)
                .propertyId(propertyId)
                .imageUrlsJson(imageUrlsJson)
                .displayOrder(displayOrder)
                .build();

        // Assert
        assertEquals(id, roomTypeImage.getId());
        assertEquals(tenantId, roomTypeImage.getTenantId());
        assertEquals(roomTypeId, roomTypeImage.getRoomTypeId());
        assertEquals(propertyId, roomTypeImage.getPropertyId());
        assertEquals(imageUrlsJson, roomTypeImage.getImageUrlsJson());
        assertEquals(displayOrder, roomTypeImage.getDisplayOrder());
    }

    @Test
    @DisplayName("Should convert JSON to image URLs array on PostLoad")
    void testPostLoad() {
        // Arrange
        RoomTypeImage roomTypeImage = new RoomTypeImage();
        roomTypeImage.setImageUrlsJson("[\"image1.jpg\",\"image2.jpg\"]");

        // Act
        ReflectionTestUtils.invokeMethod(roomTypeImage, "onLoad");

        // Assert
        assertNotNull(roomTypeImage.getImageUrls());
        assertEquals(2, roomTypeImage.getImageUrls().length);
        assertEquals("image1.jpg", roomTypeImage.getImageUrls()[0]);
        assertEquals("image2.jpg", roomTypeImage.getImageUrls()[1]);
    }

    @Test
    @DisplayName("Should handle empty JSON array on PostLoad")
    void testPostLoadWithEmptyJson() {
        // Arrange
        RoomTypeImage roomTypeImage = new RoomTypeImage();
        roomTypeImage.setImageUrlsJson("[]");

        // Act
        ReflectionTestUtils.invokeMethod(roomTypeImage, "onLoad");

        // Assert
        assertNotNull(roomTypeImage.getImageUrls());
        assertEquals(0, roomTypeImage.getImageUrls().length);
    }

    @Test
    @DisplayName("Should handle null JSON on PostLoad")
    void testPostLoadWithNullJson() {
        // Arrange
        RoomTypeImage roomTypeImage = new RoomTypeImage();
        roomTypeImage.setImageUrlsJson(null);

        // Act
        ReflectionTestUtils.invokeMethod(roomTypeImage, "onLoad");

        // Assert
        assertNotNull(roomTypeImage.getImageUrls());
        assertEquals(0, roomTypeImage.getImageUrls().length);
    }

    @Test
    @DisplayName("Should convert image URLs array to JSON on PrePersist")
    void testPrePersist() {
        // Arrange
        RoomTypeImage roomTypeImage = new RoomTypeImage();
        roomTypeImage.setImageUrls(new String[]{"image1.jpg", "image2.jpg"});

        // Act
        ReflectionTestUtils.invokeMethod(roomTypeImage, "beforeSave");

        // Assert
        assertNotNull(roomTypeImage.getImageUrlsJson());
        assertTrue(roomTypeImage.getImageUrlsJson().contains("image1.jpg"));
        assertTrue(roomTypeImage.getImageUrlsJson().contains("image2.jpg"));
    }

    @Test
    @DisplayName("Should handle empty image URLs array on PrePersist")
    void testPrePersistWithEmptyArray() {
        // Arrange
        RoomTypeImage roomTypeImage = new RoomTypeImage();
        roomTypeImage.setImageUrls(new String[0]);

        // Act
        ReflectionTestUtils.invokeMethod(roomTypeImage, "beforeSave");

        // Assert
        assertEquals("[]", roomTypeImage.getImageUrlsJson());
    }

    @Test
    @DisplayName("Should handle null image URLs array on PrePersist")
    void testPrePersistWithNullArray() {
        // Arrange
        RoomTypeImage roomTypeImage = new RoomTypeImage();
        roomTypeImage.setImageUrls(null);
        
        // Act
        ReflectionTestUtils.invokeMethod(roomTypeImage, "beforeSave");

        // Assert
        assertEquals("[]", roomTypeImage.getImageUrlsJson());
    }

    @Test
    @DisplayName("Should set default display order for new room type image")
    void testDefaultDisplayOrder() {
        // Act
        RoomTypeImage roomTypeImage = new RoomTypeImage();

        // Assert
        assertEquals(0, roomTypeImage.getDisplayOrder());
    }

    @Test
    @DisplayName("Should verify equals and hashCode methods")
    void testEqualsAndHashCode() {
        // Arrange
        RoomTypeImage image1 = RoomTypeImage.builder()
                .id(1L)
                .tenantId(100)
                .roomTypeId(200)
                .propertyId(300)
                .build();

        RoomTypeImage image2 = RoomTypeImage.builder()
                .id(1L)
                .tenantId(100)
                .roomTypeId(200)
                .propertyId(300)
                .build();

        RoomTypeImage image3 = RoomTypeImage.builder()
                .id(2L)
                .tenantId(100)
                .roomTypeId(200)
                .propertyId(300)
                .build();

        // Act & Assert
        assertEquals(image1, image2);
        assertEquals(image1.hashCode(), image2.hashCode());
        assertNotEquals(image1, image3);
        assertNotEquals(image1.hashCode(), image3.hashCode());
    }
} 