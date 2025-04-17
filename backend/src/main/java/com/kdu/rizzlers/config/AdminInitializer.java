package com.kdu.rizzlers.config;

import com.kdu.rizzlers.entity.UserRole;
import com.kdu.rizzlers.service.HousekeepingUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.kdu.rizzlers.config.XRayTraced;

/**
 * Initializes the admin user at application startup
 */
@Configuration
@Slf4j
public class AdminInitializer {

    @Value("${housekeeping.admin.username:admin}")
    private String adminUsername;

    @Value("${housekeeping.admin.password:admin123}")
    private String adminPassword;

    @Value("${housekeeping.admin.email:admin@example.com}")
    private String adminEmail;
    
    private final HousekeepingUserService userService;
    
    public AdminInitializer(HousekeepingUserService userService) {
        this.userService = userService;
    }

    @Bean
    public CommandLineRunner initializeAdmin() {
        return args -> {
            initializeAdminUser();
        };
    }
    
    @XRayTraced("AdminInitialization")
    public void initializeAdminUser() {
        // Check if admin user already exists
        if (userService.getUserByUsername(adminUsername).isEmpty()) {
            log.info("Creating default admin user: {}", adminUsername);
            
            // Create admin user
            userService.createUser(
                    adminUsername,
                    adminPassword,
                    adminEmail,
                    UserRole.ADMIN,
                    null);
            
            log.info("Default admin user created successfully");
        } else {
            log.info("Admin user already exists, skipping initialization");
        }
    }
} 