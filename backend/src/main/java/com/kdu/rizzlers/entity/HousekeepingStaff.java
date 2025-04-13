package com.kdu.rizzlers.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

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
    
    /**
     * List of shifts assigned to this staff member.
     * This is a helper field that is always populated by the service layer, not directly from DB.
     */
    @Transient
    private List<Shift> shifts = new ArrayList<>();
    
    /**
     * Helper method to get shifts assigned to this staff member.
     * When preferred shift is set, that shift is included by default.
     * Additional shifts can be added as needed by the application.
     * 
     * @return List of shifts assigned to this staff
     */
    public List<Shift> getShifts() {
        // Always return the list, even if empty
        return shifts;
    }
} 