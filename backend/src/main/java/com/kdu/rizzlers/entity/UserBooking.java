package com.kdu.rizzlers.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Entity representing user booking details stored in RDS
 */
@Entity
@Table(name = "user_booking")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Integer bookingId;

    @Column(name = "property_id")
    private Integer propertyId;

    @Column(name = "room_type_id")
    private Integer roomTypeId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "guest_id")
    private Integer guestId;
    
    @Column(name = "nightly_rate", precision = 10, scale = 2)
    private BigDecimal nightlyRate;
    
    @Column(name = "average_nightly_price", precision = 10, scale = 2)
    private BigDecimal averageNightlyPrice;
    
    @Column(name = "subtotal", precision = 10, scale = 2)
    private BigDecimal subtotal;
    
    @Column(name = "taxes_and_fees", precision = 10, scale = 2)
    private BigDecimal taxesAndFees;
    
    @Column(name = "total_for_stay", precision = 10, scale = 2)
    private BigDecimal totalForStay;
    
    @Column(name = "promotion_id")
    private Integer promotionId;
    
    @Column(name = "special_offers", nullable = false)
    private Boolean specialOffers;
    
    @Column(name = "agreed_to_terms", nullable = false)
    private Boolean agreedToTerms;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;
} 