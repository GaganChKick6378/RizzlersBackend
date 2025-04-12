package com.kdu.rizzlers.service;

import com.kdu.rizzlers.entity.HousekeepingUser;
import com.kdu.rizzlers.entity.UserRole;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for housekeeping user management
 */
public interface HousekeepingUserService {
    
    /**
     * Create a new user
     * 
     * @param username the username
     * @param password the raw password (will be encrypted)
     * @param email the email address
     * @param role the user role
     * @param staffId the associated staff ID (optional)
     * @return the created user
     */
    HousekeepingUser createUser(String username, String password, String email, UserRole role, Integer staffId);
    
    /**
     * Get user by username
     * 
     * @param username the username
     * @return Optional of HousekeepingUser if found, otherwise empty
     */
    Optional<HousekeepingUser> getUserByUsername(String username);
    
    /**
     * Get user by ID
     * 
     * @param userId the user ID
     * @return Optional of HousekeepingUser if found, otherwise empty
     */
    Optional<HousekeepingUser> getUserById(Integer userId);
    
    /**
     * Get all users
     * 
     * @return List of all users
     */
    List<HousekeepingUser> getAllUsers();
    
    /**
     * Update user role
     * 
     * @param userId the user ID
     * @param role the new role
     * @return the updated user
     */
    HousekeepingUser updateUserRole(Integer userId, UserRole role);
    
    /**
     * Change user password
     * 
     * @param userId the user ID
     * @param newPassword the new raw password (will be encrypted)
     * @return the updated user
     */
    HousekeepingUser changePassword(Integer userId, String newPassword);
    
    /**
     * Delete user
     * 
     * @param userId the user ID
     */
    void deleteUser(Integer userId);
    
    /**
     * Get user by staff ID
     * 
     * @param staffId the staff ID
     * @return Optional of HousekeepingUser if found, otherwise empty
     */
    Optional<HousekeepingUser> getUserByStaffId(Integer staffId);
} 