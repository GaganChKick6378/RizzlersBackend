package com.kdu.rizzlers.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.ZonedDateTime;

/**
 * Entity representing a review invitation sent to a guest after checkout
 */
@Entity
@Table(name = "review_invitations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewInvitation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "booking_id", nullable = false)
    private Integer bookingId;
    
    @Column(name = "guest_id", nullable = false)
    private Integer guestId;
    
    @Column(name = "guest_email", nullable = false)
    private String guestEmail;
    
    @Column(name = "token", nullable = false, unique = true)
    private String token;
    
    @Column(name = "sent_at", nullable = false)
    private ZonedDateTime sentAt;
    
    @Column(name = "expires_at", nullable = false)
    private ZonedDateTime expiresAt;
    
    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted;
} 