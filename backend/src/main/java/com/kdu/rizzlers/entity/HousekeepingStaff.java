package com.kdu.rizzlers.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;

/**
 * Entity representing housekeeping staff
 */
@Entity
@Table(name = "staff")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HousekeepingStaff {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staff_id")
    private Integer staffId;
    
    @Column(name = "staff_name", nullable = false)
    private String staffName;
    
    @Column(name = "phone")
    private String phone;
    
    @Column(name = "preferred_shift_id")
    private Integer preferredShiftId;
    
    @Column(name = "property_id", nullable = false)
    private Integer propertyId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "skill_level", nullable = false)
    private StaffSkillLevel skillLevel;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preferred_shift_id", referencedColumnName = "shift_id", insertable = false, updatable = false)
    private Shift preferredShift;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", referencedColumnName = "property_id", insertable = false, updatable = false)
    private PropertyPreferences propertyPreferences;
} 