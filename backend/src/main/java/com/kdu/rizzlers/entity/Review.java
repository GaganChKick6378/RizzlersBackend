package com.kdu.rizzlers.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.ZonedDateTime;

/**
 * Entity representing a guest review for a stay
 */
@Entity
@Table(name = "reviews_user")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "invitation_id", nullable = false)
    private Long invitationId;
    
    @Column(name = "booking_id", nullable = false)
    private Integer bookingId;
    
    @Column(name = "guest_id", nullable = false)
    private Integer guestId;
    
    @Column(name = "property_id", nullable = false)
    private Integer propertyId;
    
    @Column(name = "room_type_id", nullable = false)
    private Integer roomTypeId;
    
    @Column(name = "cleanliness_rating", nullable = false)
    private Integer cleanlinessRating;
    
    @Column(name = "staff_service_rating", nullable = false)
    private Integer staffServiceRating;
    
    @Column(name = "comfort_rating", nullable = false)
    private Integer comfortRating;
    
    @Column(name = "location_rating", nullable = false)
    private Integer locationRating;
    
    @Column(name = "value_rating", nullable = false)
    private Integer valueRating;
    
    @Column(name = "overall_rating", nullable = false)
    private Integer overallRating;
    
    @Column(name = "comment")
    private String comment;
    
    @Column(name = "images", columnDefinition = "text[]")
    private String[] images;
    
    @Column(name = "submitted_at")
    private ZonedDateTime submittedAt;
} 