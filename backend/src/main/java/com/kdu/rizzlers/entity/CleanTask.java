package com.kdu.rizzlers.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;

/**
 * Entity representing assigned cleaning tasks
 */
@Entity
@Table(name = "clean_tasks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CleanTask {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Integer taskId;
    
    @Column(name = "property_id", nullable = false)
    private Integer propertyId;
    
    @Column(name = "staff_id", nullable = false)
    private Integer staffId;
    
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    
    @Column(name = "task_type_id", nullable = false)
    private Integer taskTypeId;
    
    @Column(name = "date", nullable = false)
    private LocalDate date;
    
    @Column(name = "external_room_id", nullable = false)
    private String externalRoomId;
    
    @Column(name = "remark")
    private String remark;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", referencedColumnName = "property_id", insertable = false, updatable = false)
    private PropertyPreferences propertyPreferences;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", referencedColumnName = "staff_id", insertable = false, updatable = false)
    private HousekeepingStaff staff;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_type_id", referencedColumnName = "task_type_id", insertable = false, updatable = false)
    private CleanTaskType taskType;
} 