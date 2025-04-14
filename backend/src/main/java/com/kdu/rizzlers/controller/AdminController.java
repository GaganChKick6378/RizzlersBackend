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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Admin", description = "APIs for admin-only housekeeping operations")
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
    @Operation(summary = "Create new user", description = "Creates a new user with specified role and optional staff association")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created successfully",
                content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
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
    @Operation(summary = "Get all users", description = "Retrieves a list of all users in the system")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved users list",
                content = @Content(schema = @Schema(implementation = UserResponseDTO.class)))
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
    @Operation(summary = "Update user role", description = "Updates the role of a specific user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Role updated successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResponseDTO> updateUserRole(
            @Parameter(description = "ID of the user to update") @PathVariable Integer userId,
            @Valid @RequestBody RoleUpdateRequestDTO request) {
        
        HousekeepingUser user = userService.updateUserRole(userId, request.getRole());
        return ResponseEntity.ok(mapToUserResponseDTO(user));
    }

    /**
     * Delete a user
     */
    @DeleteMapping("/users/{userId}")
    @Operation(summary = "Delete user", description = "Deletes a user from the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User deleted successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID of the user to delete") @PathVariable Integer userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Create a new staff member
     */
    @PostMapping("/staff")
    @Operation(summary = "Create staff member", description = "Creates a new housekeeping staff member")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Staff member created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
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
    @Operation(summary = "Get staff by property", description = "Retrieves all staff members assigned to a specific property")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved staff list"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getStaffByProperty(
            @Parameter(description = "ID of the property") @PathVariable Integer propertyId) {
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
    @Operation(summary = "Create shift", description = "Creates a new work shift")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Shift created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<Shift> createShift(@Valid @RequestBody Shift shift) {
        Shift createdShift = housekeepingService.createShift(shift);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdShift);
    }

    /**
     * Get all shifts by property
     */
    @GetMapping("/shifts/property/{propertyId}")
    @Operation(summary = "Get shifts by property", description = "Retrieves all shifts associated with a specific property")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved shifts")
    public ResponseEntity<List<Shift>> getShiftsByProperty(
            @Parameter(description = "ID of the property") @PathVariable Integer propertyId) {
        List<Shift> shifts = housekeepingService.getShiftsByProperty(propertyId);
        return ResponseEntity.ok(shifts);
    }

    /**
     * Get all task types
     */
    @GetMapping("/task-types")
    @Operation(summary = "Get all task types", description = "Retrieves all available cleaning task types")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved task types")
    public ResponseEntity<List<CleanTaskType>> getAllTaskTypes() {
        List<CleanTaskType> taskTypes = housekeepingService.getAllTaskTypes();
        return ResponseEntity.ok(taskTypes);
    }

    /**
     * Generate and assign tasks for a property and date
     */
    @PostMapping("/tasks/generate")
    @Operation(summary = "Generate tasks", description = "Generates and assigns cleaning tasks for a property on a specific date")
    @ApiResponse(responseCode = "200", description = "Tasks generated successfully")
    public ResponseEntity<List<String>> generateTasks(
            @Parameter(description = "ID of the property") @RequestParam Integer propertyId,
            @Parameter(description = "Date to generate tasks for (default: current date)") @RequestParam(required = false) LocalDate date) {
        
        LocalDate taskDate = date != null ? date : LocalDate.now();
        housekeepingService.generateAndAssignTasks(propertyId, taskDate);
        
        return ResponseEntity.ok(List.of("Tasks generated successfully for property " + propertyId + " on " + taskDate));
    }

    /**
     * Get all staff absences for a property and date
     */
    @GetMapping("/absences")
    @Operation(summary = "Get absences by property and date", description = "Retrieves all staff absences for a specific property on a specific date")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved absences"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getAbsencesForPropertyAndDate(
            @Parameter(description = "ID of the property") @RequestParam Integer propertyId,
            @Parameter(description = "Date to retrieve absences for (default: current date)") @RequestParam(required = false) LocalDate date) {
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