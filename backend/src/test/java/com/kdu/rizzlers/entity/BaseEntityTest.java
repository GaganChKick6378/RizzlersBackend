package com.kdu.rizzlers.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class BaseEntityTest {

    // Concrete implementation of BaseEntity for testing
    private static class TestEntity extends BaseEntity {
        private Long id;
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
    }

    @Test
    @DisplayName("Should set created and updated timestamps on creation")
    void testPrePersist() {
        // Arrange
        TestEntity entity = new TestEntity();
        
        // Act
        entity.onCreate();
        
        // Assert
        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());
        
        // Timestamps should be very close to each other
        long diffInSeconds = ChronoUnit.SECONDS.between(entity.getCreatedAt(), entity.getUpdatedAt());
        assertTrue(diffInSeconds <= 1, "Created and updated timestamps should be very close");
    }

    @Test
    @DisplayName("Should update only the updated timestamp on update")
    void testPreUpdate() {
        // Arrange
        TestEntity entity = new TestEntity();
        entity.setCreatedAt(LocalDateTime.now().minusDays(1));
        LocalDateTime originalCreatedAt = entity.getCreatedAt();
        
        // Act
        entity.onUpdate();
        
        // Assert
        assertEquals(originalCreatedAt, entity.getCreatedAt(), "Created timestamp should not change");
        assertNotNull(entity.getUpdatedAt());
        
        // Updated timestamp should be more recent than created timestamp
        assertTrue(entity.getUpdatedAt().isAfter(entity.getCreatedAt()), 
                "Updated timestamp should be after created timestamp");
    }

    @Test
    @DisplayName("Should set default active status to true")
    void testDefaultActiveStatus() {
        // Arrange & Act
        TestEntity entity = new TestEntity();
        
        // Assert
        assertTrue(entity.getIsActive(), "New entity should be active by default");
    }

    @Test
    @DisplayName("Should allow changing active status")
    void testChangeActiveStatus() {
        // Arrange
        TestEntity entity = new TestEntity();
        assertTrue(entity.getIsActive(), "New entity should be active by default");
        
        // Act
        entity.setIsActive(false);
        
        // Assert
        assertFalse(entity.getIsActive(), "Entity should be inactive after setting to false");
    }

    @Test
    @DisplayName("Should allow setting and getting timestamps")
    void testSetGetTimestamps() {
        // Arrange
        TestEntity entity = new TestEntity();
        LocalDateTime createdAt = LocalDateTime.now().minusDays(2);
        LocalDateTime updatedAt = LocalDateTime.now().minusDays(1);
        
        // Act
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(updatedAt);
        
        // Assert
        assertEquals(createdAt, entity.getCreatedAt());
        assertEquals(updatedAt, entity.getUpdatedAt());
    }
} 