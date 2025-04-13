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
import com.kdu.rizzlers.service.HousekeepingService;
import com.kdu.rizzlers.service.HousekeepingUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for admin-only housekeeping operations
 */
@RestController
@RequestMapping("/housekeeping/admin")
public class AdminController {

    private final HousekeepingService housekeepingService;
    private final HousekeepingUserService userService;

    public AdminController(
            HousekeepingService housekeepingService,
            HousekeepingUserService userService) {
        this.housekeepingService = housekeepingService;
        this.userService = userService;
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
    public ResponseEntity<HousekeepingStaff> createStaffMember(@Valid @RequestBody HousekeepingStaff staff) {
        HousekeepingStaff createdStaff = housekeepingService.createStaffMember(staff);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStaff);
    }

    /**
     * Get all staff by property
     */
    @GetMapping("/staff/property/{propertyId}")
    public ResponseEntity<List<HousekeepingStaff>> getStaffByProperty(@PathVariable Integer propertyId) {
        List<HousekeepingStaff> staff = housekeepingService.getStaffByProperty(propertyId);
        return ResponseEntity.ok(staff);
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
    public ResponseEntity<List<StaffAbsence>> getAbsencesForPropertyAndDate(
            @RequestParam Integer propertyId,
            @RequestParam(required = false) LocalDate date) {
        
        LocalDate absenceDate = date != null ? date : LocalDate.now();
        List<StaffAbsence> absences = housekeepingService.getAbsencesForPropertyAndDate(propertyId, absenceDate);
        
        return ResponseEntity.ok(absences);
    }
    
    /**
     * Map HousekeepingUser entity to UserResponseDTO
     */
    private UserResponseDTO mapToUserResponseDTO(HousekeepingUser user) {
        String staffName = null;
        if (user.getStaffId() != null && user.getStaff() != null) {
            staffName = user.getStaff().getStaffName();
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