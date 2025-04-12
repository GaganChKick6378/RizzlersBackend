package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.StaffAbsenceDTO;
import com.kdu.rizzlers.dto.TaskAssignmentDTO;
import com.kdu.rizzlers.dto.TaskGenerationDTO;
import com.kdu.rizzlers.entity.CleanTask;
import com.kdu.rizzlers.entity.CleanTaskType;
import com.kdu.rizzlers.entity.HousekeepingStaff;
import com.kdu.rizzlers.entity.PropertyPreferences;
import com.kdu.rizzlers.entity.Shift;
import com.kdu.rizzlers.entity.StaffAbsence;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for housekeeping operations
 */
public interface HousekeepingService {
    
    /**
     * Apply for a sick leave (staff absence)
     * 
     * @param absenceDTO the absence request
     * @return true if absence created successfully
     */
    boolean applyForSickLeave(StaffAbsenceDTO absenceDTO);
    
    /**
     * Get all absences for a staff member
     * 
     * @param staffId the staff ID
     * @return List of absences
     */
    List<StaffAbsence> getStaffAbsences(Integer staffId);
    
    /**
     * Get all staff absences for a specific date and property
     * 
     * @param propertyId the property ID
     * @param date the date
     * @return List of absences
     */
    List<StaffAbsence> getAbsencesForPropertyAndDate(Integer propertyId, LocalDate date);
    
    /**
     * Generate and assign tasks for a specific date
     * 
     * @param propertyId the property ID
     * @param date the date
     * @return List of assigned tasks
     */
    List<CleanTask> generateAndAssignTasks(Integer propertyId, LocalDate date);
    
    /**
     * Get all assigned tasks for a staff member on a specific date
     * 
     * @param staffId the staff ID
     * @param date the date
     * @return List of assigned tasks
     */
    List<TaskAssignmentDTO> getStaffTasksForDate(Integer staffId, LocalDate date);
    
    /**
     * Get all cleaning tasks for a property on a specific date
     * 
     * @param propertyId the property ID
     * @param date the date
     * @return List of assigned tasks
     */
    List<TaskAssignmentDTO> getPropertyTasksForDate(Integer propertyId, LocalDate date);
    
    /**
     * Create a new cleaning task type
     * 
     * @param taskType the task type to create
     * @return the created task type
     */
    CleanTaskType createTaskType(CleanTaskType taskType);
    
    /**
     * Create a new staff member
     * 
     * @param staff the staff member to create
     * @return the created staff member
     */
    HousekeepingStaff createStaffMember(HousekeepingStaff staff);
    
    /**
     * Create a new shift
     * 
     * @param shift the shift to create
     * @return the created shift
     */
    Shift createShift(Shift shift);
    
    /**
     * Create or update property preferences
     * 
     * @param preferences the property preferences
     * @return the saved property preferences
     */
    PropertyPreferences savePropertyPreferences(PropertyPreferences preferences);
    
    /**
     * Get all staff for a property
     * 
     * @param propertyId the property ID
     * @return List of staff
     */
    List<HousekeepingStaff> getStaffByProperty(Integer propertyId);
    
    /**
     * Get all shifts for a property
     * 
     * @param propertyId the property ID
     * @return List of shifts
     */
    List<Shift> getShiftsByProperty(Integer propertyId);
    
    /**
     * Get all task types
     * 
     * @return List of task types
     */
    List<CleanTaskType> getAllTaskTypes();
    
    /**
     * Send a staff shortfall notification to the admin
     * 
     * @param propertyId the property ID
     * @param date the date
     * @param staffShortfall the number of additional staff needed
     * @param unassignedTasks the list of unassigned tasks
     * @param totalUnassignedMinutes the total minutes of unassigned tasks
     * @param averageShiftMinutes the average shift duration in minutes
     * @return true if notification was sent successfully
     */
    boolean sendStaffShortfallNotification(
            Integer propertyId,
            LocalDate date,
            int staffShortfall,
            List<TaskGenerationDTO> unassignedTasks,
            int totalUnassignedMinutes,
            double averageShiftMinutes);
} 