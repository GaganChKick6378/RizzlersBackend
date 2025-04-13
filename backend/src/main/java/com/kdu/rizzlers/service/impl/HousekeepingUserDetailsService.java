package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.entity.HousekeepingUser;
import com.kdu.rizzlers.repository.HousekeepingUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Service for loading user authentication details from the database
 */
@Service
@Slf4j
public class HousekeepingUserDetailsService implements UserDetailsService {

    private final HousekeepingUserRepository userRepository;

    public HousekeepingUserDetailsService(HousekeepingUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user details for username: {}", username);
        
        HousekeepingUser housekeepingUser = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });
        
        // Check if user is active
        if (!housekeepingUser.getIsActive()) {
            log.warn("Attempt to log in with deactivated account: {}", username);
            throw new UsernameNotFoundException("User account is deactivated: " + username);
        }
        
        // Convert our role to Spring Security role (adding the "ROLE_" prefix)
        String springSecurityRole = "ROLE_" + housekeepingUser.getRole().name();
        
        log.debug("User found: {}, role: {}", username, springSecurityRole);
        
        // Build Spring Security UserDetails
        return User.builder()
                .username(housekeepingUser.getUsername())
                .password(housekeepingUser.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(springSecurityRole)))
                .build();
    }
} 