package com.kdu.rizzlers.repository;

import com.kdu.rizzlers.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    Optional<User> findByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.guestId IS NOT NULL")
    Optional<User> findByEmailWithGuestId(@Param("email") String email);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.guestId = :guestId")
    Optional<User> findByGuestId(@Param("guestId") Integer guestId);
    
    /**
     * Find users by a list of guest IDs
     * @param guestIds the list of guest IDs
     * @return List of users
     */
    @Query("SELECT u FROM User u WHERE u.guestId IN :guestIds")
    List<User> findByGuestIdIn(@Param("guestIds") List<Integer> guestIds);
    
    /**
     * Clear expired OTPs from the users table
     * @param currentTime the current time to compare with otp_expiry
     * @return the number of records updated
     */
    @Modifying
    @Query("UPDATE User u SET u.otp = null WHERE u.otp IS NOT NULL AND u.otpExpiry < :currentTime")
    int clearExpiredOtps(@Param("currentTime") ZonedDateTime currentTime);
} 