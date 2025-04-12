package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.TaskAssignmentDTO;
import com.kdu.rizzlers.dto.TaskGenerationDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for scheduling housekeeping tasks
 */
public interface TaskSchedulingService {
    
    /**
     * Generate cleaning tasks based on room statuses
     * 
     * @param propertyId the property ID
     * @param date the date to generate tasks for
     * @return List of generated tasks
     */
    List<TaskGenerationDTO> generateTasks(Integer propertyId, LocalDate date);
    
    /**
     * Assign tasks to available staff for a specific date
     * 
     * @param propertyId the property ID
     * @param tasks the tasks to assign
     * @param date the date
     * @return List of assigned tasks
     */
    List<TaskAssignmentDTO> assignTasks(Integer propertyId, List<TaskGenerationDTO> tasks, LocalDate date);
    
    /**
     * Calculate staff shortfall for a set of tasks
     * 
     * @param propertyId the property ID
     * @param date the date
     * @param unassignedTasks the unassigned tasks
     * @return estimated number of additional staff needed
     */
    int calculateStaffShortfall(Integer propertyId, LocalDate date, List<TaskGenerationDTO> unassignedTasks);
} 