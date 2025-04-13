package com.kdu.rizzlers.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalTime;
import java.time.ZonedDateTime;

/**
 * Entity representing property preferences for housekeeping scheduling
 */
@Entity
@Table(name = "property_preferences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyPreferences {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "property_id")
    private Integer propertyId;
    
    @Column(name = "timezone", nullable = false)
    private String timezone;
    
    @Column(name = "check_out_time", nullable = false)
    private LocalTime checkOutTime;
    
    @Column(name = "check_in_time", nullable = false)
    private LocalTime checkInTime;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
} 