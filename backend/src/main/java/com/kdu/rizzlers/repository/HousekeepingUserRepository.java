package com.kdu.rizzlers.repository;

import com.kdu.rizzlers.entity.HousekeepingUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for HousekeepingUser entity
 */
@Repository
public interface HousekeepingUserRepository extends JpaRepository<HousekeepingUser, Integer> {
    
    /**
     * Find user by username
     * 
     * @param username the username
     * @return Optional of HousekeepingUser if found, otherwise empty
     */
    Optional<HousekeepingUser> findByUsername(String username);
    
    /**
     * Find user by staff ID
     * 
     * @param staffId the staff ID
     * @return Optional of HousekeepingUser if found, otherwise empty
     */
    Optional<HousekeepingUser> findByStaffId(Integer staffId);
    
    /**
     * Check if a username already exists
     * 
     * @param username the username
     * @return true if the username exists
     */
    boolean existsByUsername(String username);
} 