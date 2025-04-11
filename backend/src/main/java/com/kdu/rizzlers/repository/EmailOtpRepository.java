package com.kdu.rizzlers.repository;

import com.kdu.rizzlers.entity.EmailOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailOtpRepository extends JpaRepository<EmailOtp, UUID> {
    
    /**
     * Find the latest OTP for an email
     * @param email the email address
     * @return the latest OTP entity if found
     */
    @Query("SELECT e FROM EmailOtp e WHERE e.email = :email ORDER BY e.createdAt DESC")
    Optional<EmailOtp> findLatestByEmail(@Param("email") String email);
    
    /**
     * Find OTP by email and OTP code
     * @param email the email address
     * @param otp the OTP code
     * @return the OTP entity if found
     */
    Optional<EmailOtp> findByEmailAndOtp(String email, String otp);
    
    /**
     * Clear expired OTPs
     * @param currentTime the current time to compare with expiry_time
     * @return the number of records deleted
     */
    @Modifying
    @Query("DELETE FROM EmailOtp e WHERE e.expiryTime < :currentTime")
    int clearExpiredOtps(@Param("currentTime") ZonedDateTime currentTime);
    
    /**
     * Delete all OTPs for a given email
     * @param email the email address
     * @return the number of records deleted
     */
    @Modifying
    int deleteByEmail(String email);
} 