package com.kdu.rizzlers.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "email_otps")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailOtp {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(name = "email", nullable = false)
    private String email;
    
    @Column(name = "otp", nullable = false, length = 6)
    private String otp;
    
    @Column(name = "expiry_time", nullable = false)
    private ZonedDateTime expiryTime;
    
    @Column(name = "verified", nullable = false)
    private Boolean verified = false;
    
    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount = 0;
    
    @Column(name = "last_attempt_time")
    private ZonedDateTime lastAttemptTime;
    
    @Column(name = "session_expiry_time")
    private ZonedDateTime sessionExpiryTime;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
} 