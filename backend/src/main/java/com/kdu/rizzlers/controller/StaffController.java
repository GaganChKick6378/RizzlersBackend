package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.dto.PasswordChangeRequestDTO;
import com.kdu.rizzlers.dto.StaffAbsenceDTO;
import com.kdu.rizzlers.dto.TaskAssignmentDTO;
import com.kdu.rizzlers.dto.UserResponseDTO;
import com.kdu.rizzlers.entity.HousekeepingUser;
import com.kdu.rizzlers.entity.StaffAbsence;
import com.kdu.rizzlers.service.HousekeepingService;
import com.kdu.rizzlers.service.HousekeepingUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controller for staff housekeeping operations
 */
@RestController
@RequestMapping("/housekeeping/staff")
@Tag(name = "Staff", description = "APIs for staff housekeeping operations")
public class StaffController {

    private final HousekeepingService housekeepingService;
    private final HousekeepingUserService userService;

    public StaffController(
            HousekeepingService housekeepingService,
            HousekeepingUserService userService) {
        this.housekeepingService = housekeepingService;
        this.userService = userService;
    }

    /**
     * Get current user profile
     */
    @GetMapping("/profile")
    @Operation(summary = "Get current user profile", description = "Retrieves the profile of the currently authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved user profile",
                content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResponseDTO> getCurrentUserProfile() {
        String username = getCurrentUsername();
        Optional<HousekeepingUser> userOpt = userService.getUserByUsername(username);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        HousekeepingUser user = userOpt.get();
        String staffName = null;
        if (user.getStaffId() != null && user.getStaff() != null) {
            staffName = user.getStaff().getStaffName();
        }
        
        UserResponseDTO response = UserResponseDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .staffId(user.getStaffId())
                .staffName(staffName)
                .isActive(user.getIsActive())
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Change current user's password
     */
    @PutMapping("/profile/password")
    @Operation(summary = "Change password", description = "Changes the password for the currently authenticated user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Password changed successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Void> changePassword(@Valid @RequestBody PasswordChangeRequestDTO request) {
        String username = getCurrentUsername();
        Optional<HousekeepingUser> userOpt = userService.getUserByUsername(username);
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        userService.changePassword(userOpt.get().getUserId(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }

    /**
     * Apply for sick leave
     */
    @PostMapping("/sick-leave")
    @Operation(summary = "Apply for sick leave", description = "Submit a sick leave request for a staff member")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sick leave application successful"),
        @ApiResponse(responseCode = "400", description = "Bad request - invalid user or not authorized")
    })
    public ResponseEntity<String> applyForSickLeave(@Valid @RequestBody StaffAbsenceDTO absenceDTO) {
        String username = getCurrentUsername();
        Optional<HousekeepingUser> userOpt = userService.getUserByUsername(username);
        
        // Ensure the user is applying for their own sick leave (if they are staff)
        if (userOpt.isEmpty() || userOpt.get().getStaffId() == null) {
            return ResponseEntity.badRequest().body("Invalid user or not associated with a staff member");
        }
        
        // If the user is not an admin, ensure they're applying for their own sick leave
        if (!isAdmin() && !userOpt.get().getStaffId().equals(absenceDTO.getStaffId())) {
            return ResponseEntity.badRequest().body("You can only apply for your own sick leave");
        }
        
        boolean success = housekeepingService.applyForSickLeave(absenceDTO);
        
        if (success) {
            return ResponseEntity.ok("Sick leave application successful");
        } else {
            return ResponseEntity.badRequest().body("Failed to apply for sick leave");
        }
    }

    /**
     * Get staff absences
     */
    @GetMapping("/sick-leave/{staffId}")
    @Operation(summary = "Get staff absences", description = "Retrieves absence records for a specific staff member")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved absences"),
        @ApiResponse(responseCode = "400", description = "Bad request - invalid user or not authorized")
    })
    public ResponseEntity<List<StaffAbsence>> getStaffAbsences(
            @Parameter(description = "ID of the staff member") @PathVariable Integer staffId) {
        String username = getCurrentUsername();
        Optional<HousekeepingUser> userOpt = userService.getUserByUsername(username);
        
        // Ensure the user is viewing their own absences (if they are staff)
        if (userOpt.isEmpty() || userOpt.get().getStaffId() == null) {
            return ResponseEntity.badRequest().build();
        }
        
        // If the user is not an admin, ensure they're viewing their own absences
        if (!isAdmin() && !userOpt.get().getStaffId().equals(staffId)) {
            return ResponseEntity.badRequest().build();
        }
        
        List<StaffAbsence> absences = housekeepingService.getStaffAbsences(staffId);
        return ResponseEntity.ok(absences);
    }

    /**
     * Get staff tasks for a specific date
     */
    @GetMapping("/tasks/{staffId}")
    @Operation(summary = "Get staff tasks", description = "Retrieves tasks assigned to a specific staff member for a given date")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved tasks"),
        @ApiResponse(responseCode = "400", description = "Bad request - invalid user or not authorized")
    })
    public ResponseEntity<List<TaskAssignmentDTO>> getStaffTasks(
            @Parameter(description = "ID of the staff member") @PathVariable Integer staffId,
            @Parameter(description = "Date to retrieve tasks for (default: current date)") @RequestParam(required = false) LocalDate date) {
        
        String username = getCurrentUsername();
        Optional<HousekeepingUser> userOpt = userService.getUserByUsername(username);
        
        // Ensure the user is viewing their own tasks (if they are staff)
        if (userOpt.isEmpty() || userOpt.get().getStaffId() == null) {
            return ResponseEntity.badRequest().build();
        }
        
        // If the user is not an admin, ensure they're viewing their own tasks
        if (!isAdmin() && !userOpt.get().getStaffId().equals(staffId)) {
            return ResponseEntity.badRequest().build();
        }
        
        LocalDate taskDate = date != null ? date : LocalDate.now();
        List<TaskAssignmentDTO> tasks = housekeepingService.getStaffTasksForDate(staffId, taskDate);
        
        return ResponseEntity.ok(tasks);
    }
    
    /**
     * Get the username of the currently authenticated user
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
    
    /**
     * Check if the current user has admin role
     */
    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }
} 