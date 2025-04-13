package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.dto.RoleUpdateRequestDTO;
import com.kdu.rizzlers.dto.UserCreateRequestDTO;
import com.kdu.rizzlers.dto.UserResponseDTO;
import com.kdu.rizzlers.entity.CleanTaskType;
import com.kdu.rizzlers.entity.HousekeepingStaff;
import com.kdu.rizzlers.entity.HousekeepingUser;
import com.kdu.rizzlers.entity.PropertyPreferences;
import com.kdu.rizzlers.entity.Shift;
import com.kdu.rizzlers.entity.StaffAbsence;
import com.kdu.rizzlers.entity.StaffSkillLevel;
import com.kdu.rizzlers.repository.HousekeepingStaffRepository;
import com.kdu.rizzlers.service.HousekeepingService;
import com.kdu.rizzlers.service.HousekeepingUserService;
import org.hibernate.Hibernate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for admin-only housekeeping operations
 */
@RestController
@RequestMapping("/housekeeping/admin")
@Slf4j
public class AdminController {

    private final HousekeepingService housekeepingService;
    private final HousekeepingUserService userService;
    private final HousekeepingStaffRepository staffRepository;

    public AdminController(
            HousekeepingService housekeepingService,
            HousekeepingUserService userService,
            HousekeepingStaffRepository staffRepository) {
        this.housekeepingService = housekeepingService;
        this.userService = userService;
        this.staffRepository = staffRepository;
    }

    /**
     * Create a new user
     */
    @PostMapping("/users")
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserCreateRequestDTO request) {
        HousekeepingUser user = userService.createUser(
                request.getUsername(),
                request.getPassword(),
                request.getEmail(),
                request.getRole(),
                request.getStaffId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToUserResponseDTO(user));
    }

    /**
     * Get all users
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<UserResponseDTO> users = userService.getAllUsers().stream()
                .map(this::mapToUserResponseDTO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(users);
    }

    /**
     * Update user role
     */
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<UserResponseDTO> updateUserRole(
            @PathVariable Integer userId,
            @Valid @RequestBody RoleUpdateRequestDTO request) {
        
        HousekeepingUser user = userService.updateUserRole(userId, request.getRole());
        return ResponseEntity.ok(mapToUserResponseDTO(user));
    }

    /**
     * Delete a user
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Create a new staff member
     */
    @PostMapping("/staff")
    public ResponseEntity<?> createStaffMember(@Valid @RequestBody HousekeepingStaff staff) {
        try {
            // Validation
            if (staff.getStaffName() == null || staff.getStaffName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Staff name is required"));
            }
            
            if (staff.getPropertyId() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Property ID is required"));
            }
            
            if (staff.getSkillLevel() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Skill level is required"));
            }
            
            // Ensure the skill level is a valid enum value
            try {
                StaffSkillLevel skillLevel = staff.getSkillLevel();
                // If we get here, the enum value is valid
                log.info("Using skill level: {}", skillLevel);
            } catch (IllegalArgumentException e) {
                // The enum value is invalid
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid skill level",
                    "message", "Valid values are: " + Arrays.toString(StaffSkillLevel.values())
                ));
            }
            
            // Create staff member
            HousekeepingStaff createdStaff = housekeepingService.createStaffMember(staff);
            
            // Convert to simplified Map to avoid serialization issues with lazy-loaded associations
            Map<String, Object> staffResponse = new HashMap<>();
            staffResponse.put("staffId", createdStaff.getStaffId());
            staffResponse.put("staffName", createdStaff.getStaffName());
            staffResponse.put("phone", createdStaff.getPhone());
            staffResponse.put("preferredShiftId", createdStaff.getPreferredShiftId());
            staffResponse.put("propertyId", createdStaff.getPropertyId());
            staffResponse.put("skillLevel", createdStaff.getSkillLevel().name());
            staffResponse.put("createdAt", createdStaff.getCreatedAt());
            staffResponse.put("updatedAt", createdStaff.getUpdatedAt());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(staffResponse);
        } catch (Exception e) {
            log.error("Error creating staff member: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Failed to create staff member",
                            "message", e.getMessage()
                    ));
        }
    }

    /**
     * Get all staff by property
     */
    @GetMapping("/staff/property/{propertyId}")
    public ResponseEntity<?> getStaffByProperty(@PathVariable Integer propertyId) {
        try {
            log.info("Fetching staff for property ID: {}", propertyId);
            List<HousekeepingStaff> staffList = housekeepingService.getStaffByProperty(propertyId);
            
            // Create a simplified response to avoid serialization issues with lazy-loaded associations
            List<Map<String, Object>> response = staffList.stream()
                .map(staff -> {
                    Map<String, Object> staffDto = new HashMap<>();
                    staffDto.put("staffId", staff.getStaffId());
                    staffDto.put("staffName", staff.getStaffName());
                    staffDto.put("phone", staff.getPhone());
                    staffDto.put("preferredShiftId", staff.getPreferredShiftId());
                    staffDto.put("propertyId", staff.getPropertyId());
                    staffDto.put("skillLevel", staff.getSkillLevel().name());
                    staffDto.put("createdAt", staff.getCreatedAt());
                    staffDto.put("updatedAt", staff.getUpdatedAt());
                    return staffDto;
                })
                .collect(Collectors.toList());
            
            log.info("Retrieved {} staff members for property ID: {}", response.size(), propertyId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving staff for property {}: {}", propertyId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "error", "Failed to retrieve staff",
                    "message", e.getMessage()
                ));
        }
    }

    /**
     * Create a new shift
     */
    @PostMapping("/shifts")
    public ResponseEntity<Shift> createShift(@Valid @RequestBody Shift shift) {
        Shift createdShift = housekeepingService.createShift(shift);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdShift);
    }

    /**
     * Get all shifts by property
     */
    @GetMapping("/shifts/property/{propertyId}")
    public ResponseEntity<List<Shift>> getShiftsByProperty(@PathVariable Integer propertyId) {
        List<Shift> shifts = housekeepingService.getShiftsByProperty(propertyId);
        return ResponseEntity.ok(shifts);
    }

    /**
     * Create a new task type
     */
    @PostMapping("/task-types")
    public ResponseEntity<CleanTaskType> createTaskType(@Valid @RequestBody CleanTaskType taskType) {
        CleanTaskType createdTaskType = housekeepingService.createTaskType(taskType);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTaskType);
    }

    /**
     * Get all task types
     */
    @GetMapping("/task-types")
    public ResponseEntity<List<CleanTaskType>> getAllTaskTypes() {
        List<CleanTaskType> taskTypes = housekeepingService.getAllTaskTypes();
        return ResponseEntity.ok(taskTypes);
    }

    /**
     * Save property preferences
     */
    @PostMapping("/property-preferences")
    public ResponseEntity<PropertyPreferences> savePropertyPreferences(
            @Valid @RequestBody PropertyPreferences preferences) {
        
        PropertyPreferences savedPreferences = housekeepingService.savePropertyPreferences(preferences);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPreferences);
    }

    /**
     * Generate and assign tasks for a property and date
     */
    @PostMapping("/tasks/generate")
    public ResponseEntity<List<String>> generateTasks(
            @RequestParam Integer propertyId,
            @RequestParam(required = false) LocalDate date) {
        
        LocalDate taskDate = date != null ? date : LocalDate.now();
        housekeepingService.generateAndAssignTasks(propertyId, taskDate);
        
        return ResponseEntity.ok(List.of("Tasks generated successfully for property " + propertyId + " on " + taskDate));
    }

    /**
     * Get all staff absences for a property and date
     */
    @GetMapping("/absences")
    public ResponseEntity<?> getAbsencesForPropertyAndDate(
            @RequestParam Integer propertyId,
            @RequestParam(required = false) LocalDate date) {
        try {
            LocalDate absenceDate = date != null ? date : LocalDate.now();
            List<StaffAbsence> absences = housekeepingService.getAbsencesForPropertyAndDate(propertyId, absenceDate);
            
            // Convert to a simplified representation to avoid lazy loading issues
            List<Map<String, Object>> result = absences.stream()
                .map(absence -> {
                    Map<String, Object> dto = new HashMap<>();
                    dto.put("staffId", absence.getId().getStaffId());
                    dto.put("date", absence.getId().getDate());
                    dto.put("createdAt", absence.getCreatedAt());
                    dto.put("updatedAt", absence.getUpdatedAt());
                    
                    // Try to get staff name if initialized
                    if (absence.getStaff() != null && Hibernate.isInitialized(absence.getStaff())) {
                        dto.put("staffName", absence.getStaff().getStaffName());
                    }
                    
                    return dto;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error fetching absences for property {} on date {}: {}", 
                     propertyId, date, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                   .body(Map.of("error", "Failed to fetch absences", 
                                "message", e.getMessage()));
        }
    }
    
    /**
     * Map HousekeepingUser entity to UserResponseDTO
     */
    private UserResponseDTO mapToUserResponseDTO(HousekeepingUser user) {
        String staffName = null;
        
        // Safe handling of lazy-loaded staff entity
        try {
            if (user.getStaffId() != null && user.getStaff() != null) {
                // Check if proxy is initialized before calling getStaffName()
                if (Hibernate.isInitialized(user.getStaff())) {
                    staffName = user.getStaff().getStaffName();
                } else {
                    // Alternatively fetch staff name directly from repository if needed
                    staffName = staffRepository.findById(user.getStaffId())
                        .map(HousekeepingStaff::getStaffName)
                        .orElse(null);
                }
            }
        } catch (Exception e) {
            // Log error but continue without failing
            System.err.println("Error fetching staff name: " + e.getMessage());
        }
        
        return UserResponseDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .staffId(user.getStaffId())
                .staffName(staffName)
                .isActive(user.getIsActive())
                .build();
    }
} 