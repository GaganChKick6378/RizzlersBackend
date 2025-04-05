package com.kdu.rizzlers.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.ZonedDateTime;

/**
 * Entity representing a booking lock to prevent concurrent bookings
 * of the same room for the same date range.
 */
@Entity
@Table(name = "booking_locks", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"room_id", "start_date", "end_date"}, name = "unique_booking_lock")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingLock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Integer roomId;

    @Column(name = "property_id", nullable = false)
    private Integer propertyId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "lock_timestamp", nullable = false)
    private ZonedDateTime lockTimestamp;

    @Column(name = "lock_expiry", nullable = false)
    private ZonedDateTime lockExpiry;

    @Column(name = "lock_owner")
    private String lockOwner;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private BookingLockStatus status;
    
    @Column(name = "booking_id")
    private Integer bookingId;
    
    @Column(name = "status_updated_at")
    private ZonedDateTime statusUpdatedAt;
    
    @Column(name = "last_error_message", length = 500)
    private String lastErrorMessage;
    
    @Column(name = "server_id", length = 100)
    private String serverId;

    /**
     * Checks if the lock is expired
     * @return true if the lock has expired
     */
    @Transient
    public boolean isExpired() {
        return ZonedDateTime.now().isAfter(lockExpiry);
    }

    /**
     * Enum representing the possible states of a booking lock
     */
    public enum BookingLockStatus {
        PENDING,
        CONFIRMED,
        RELEASED,
        EXPIRED,
        FAILED
    }
} 