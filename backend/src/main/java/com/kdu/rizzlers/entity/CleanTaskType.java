package com.kdu.rizzlers.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Duration;
import java.time.ZonedDateTime;

/**
 * Entity representing types of cleaning tasks
 */
@Entity
@Table(name = "clean_task_types")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CleanTaskType {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_type_id")
    private Integer taskTypeId;
    
    @Column(name = "type_name", nullable = false)
    private String typeName;
    
    // Store as String in the database but convert to Duration in the application
    @Column(name = "required_time", nullable = false)
    private String requiredTimeStr;
    
    @Transient
    private Duration requiredTime;
    
    // Convert string representation to Duration when entity is loaded
    @PostLoad
    private void postLoad() {
        if (requiredTimeStr != null && !requiredTimeStr.isEmpty()) {
            String[] parts = requiredTimeStr.split(":");
            if (parts.length == 3) {
                int hours = Integer.parseInt(parts[0]);
                int minutes = Integer.parseInt(parts[1]);
                int seconds = Integer.parseInt(parts[2]);
                this.requiredTime = Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds);
            }
        }
    }
    
    // Getter that ensures requiredTime is available
    public Duration getRequiredTime() {
        if (requiredTime == null) {
            postLoad();
        }
        return requiredTime;
    }
    
    // Setter that updates both the string and Duration
    public void setRequiredTime(Duration duration) {
        this.requiredTime = duration;
        if (duration != null) {
            long seconds = duration.getSeconds();
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            long secs = seconds % 60;
            this.requiredTimeStr = String.format("%02d:%02d:%02d", hours, minutes, secs);
        } else {
            this.requiredTimeStr = null;
        }
    }
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
} 