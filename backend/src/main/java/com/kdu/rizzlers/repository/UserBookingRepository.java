package com.kdu.rizzlers.repository;

import com.kdu.rizzlers.entity.UserBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserBookingRepository extends JpaRepository<UserBooking, Long> {
    
    Optional<UserBooking> findByBookingId(Integer bookingId);
    
    List<UserBooking> findByGuestId(Integer guestId);
    
    List<UserBooking> findByUserId(UUID userId);
    
    List<UserBooking> findByPropertyId(Integer propertyId);
} 