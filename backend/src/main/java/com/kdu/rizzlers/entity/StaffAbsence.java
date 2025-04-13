package com.kdu.rizzlers.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.ZonedDateTime;

/**
 * Entity representing staff absences (sick leave)
 */
@Entity
@Table(name = "absent")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffAbsence {
    
    @EmbeddedId
    private StaffAbsenceId id;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", referencedColumnName = "staff_id", insertable = false, updatable = false)
    private HousekeepingStaff staff;
    
    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StaffAbsenceId implements java.io.Serializable {
        
        @Column(name = "staff_id")
        private Integer staffId;
        
        @Column(name = "date")
        private LocalDate date;
    }
} 