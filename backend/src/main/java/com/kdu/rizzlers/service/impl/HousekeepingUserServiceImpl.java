package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.entity.HousekeepingUser;
import com.kdu.rizzlers.entity.UserRole;
import com.kdu.rizzlers.repository.HousekeepingStaffRepository;
import com.kdu.rizzlers.repository.HousekeepingUserRepository;
import com.kdu.rizzlers.service.HousekeepingUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of HousekeepingUserService for user management
 */
@Service
@Slf4j
public class HousekeepingUserServiceImpl implements HousekeepingUserService {

    private final HousekeepingUserRepository userRepository;
    private final HousekeepingStaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;

    public HousekeepingUserServiceImpl(
            HousekeepingUserRepository userRepository,
            HousekeepingStaffRepository staffRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.staffRepository = staffRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public HousekeepingUser createUser(String username, String password, String email, UserRole role, Integer staffId) {
        // Check if username already exists
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
        
        // If staff ID is provided, check if it exists
        if (staffId != null) {
            if (!staffRepository.existsById(staffId)) {
                throw new IllegalArgumentException("Staff ID not found: " + staffId);
            }
            
            // Check if staff ID is already associated with a user
            if (userRepository.findByStaffId(staffId).isPresent()) {
                throw new IllegalArgumentException("Staff ID already associated with a user: " + staffId);
            }
        }
        
        // Encrypt password
        String encodedPassword = passwordEncoder.encode(password);
        
        // Create and save new user
        HousekeepingUser user = HousekeepingUser.builder()
                .username(username)
                .password(encodedPassword)
                .email(email)
                .role(role)
                .staffId(staffId)
                .isActive(true)
                .build();
        
        HousekeepingUser savedUser = userRepository.save(user);
        log.info("Created new user: {}, role: {}", username, role);
        
        return savedUser;
    }

    @Override
    public Optional<HousekeepingUser> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<HousekeepingUser> getUserById(Integer userId) {
        return userRepository.findById(userId);
    }

    @Override
    public List<HousekeepingUser> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public HousekeepingUser updateUserRole(Integer userId, UserRole role) {
        HousekeepingUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        user.setRole(role);
        HousekeepingUser updatedUser = userRepository.save(user);
        
        log.info("Updated role for user {}: {}", user.getUsername(), role);
        return updatedUser;
    }

    @Override
    @Transactional
    public HousekeepingUser changePassword(Integer userId, String newPassword) {
        HousekeepingUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        HousekeepingUser updatedUser = userRepository.save(user);
        
        log.info("Changed password for user: {}", user.getUsername());
        return updatedUser;
    }

    @Override
    @Transactional
    public void deleteUser(Integer userId) {
        HousekeepingUser user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        // Soft delete by setting isActive to false
        user.setIsActive(false);
        userRepository.save(user);
        
        log.info("Deactivated user: {}", user.getUsername());
    }

    @Override
    public Optional<HousekeepingUser> getUserByStaffId(Integer staffId) {
        return userRepository.findByStaffId(staffId);
    }
} 