package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.dto.RoomBookingDTO;
import com.kdu.rizzlers.dto.TaskAssignmentDTO;
import com.kdu.rizzlers.dto.TaskGenerationDTO;
import com.kdu.rizzlers.entity.CleanTaskType;
import com.kdu.rizzlers.entity.HousekeepingStaff;
import com.kdu.rizzlers.entity.PropertyPreferences;
import com.kdu.rizzlers.entity.Shift;
import com.kdu.rizzlers.repository.CleanTaskTypeRepository;
import com.kdu.rizzlers.repository.HousekeepingStaffRepository;
import com.kdu.rizzlers.repository.PropertyPreferencesRepository;
import com.kdu.rizzlers.repository.ShiftRepository;
import com.kdu.rizzlers.service.RoomBookingService;
import com.kdu.rizzlers.service.TaskSchedulingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of TaskSchedulingService for generating and assigning tasks
 */
@Service
@Slf4j
public class TaskSchedulingServiceImpl implements TaskSchedulingService {

    private final RoomBookingService roomBookingService;
    private final PropertyPreferencesRepository propertyPreferencesRepository;
    private final CleanTaskTypeRepository taskTypeRepository;
    private final HousekeepingStaffRepository staffRepository;
    private final ShiftRepository shiftRepository;
    
    // Cache task types to reduce database queries
    private Map<String, CleanTaskType> taskTypeCache = new HashMap<>();
    
    public TaskSchedulingServiceImpl(
            RoomBookingService roomBookingService,
            PropertyPreferencesRepository propertyPreferencesRepository,
            CleanTaskTypeRepository taskTypeRepository,
            HousekeepingStaffRepository staffRepository,
            ShiftRepository shiftRepository) {
        this.roomBookingService = roomBookingService;
        this.propertyPreferencesRepository = propertyPreferencesRepository;
        this.taskTypeRepository = taskTypeRepository;
        this.staffRepository = staffRepository;
        this.shiftRepository = shiftRepository;
    }
    
    @PostConstruct
    public void init() {
        // Load task types into cache at startup
        taskTypeRepository.findAll().forEach(type -> taskTypeCache.put(type.getTypeName(), type));
    }

    @Override
    public List<TaskGenerationDTO> generateTasks(Integer propertyId, LocalDate date) {
        List<TaskGenerationDTO> tasks = new ArrayList<>();
        
        Optional<PropertyPreferences> propertyPreferencesOpt = propertyPreferencesRepository.findByPropertyId(propertyId);
        if (propertyPreferencesOpt.isEmpty()) {
            log.error("Property preferences not found for property ID: {}", propertyId);
            return tasks;
        }
        
        PropertyPreferences preferences = propertyPreferencesOpt.get();
        LocalTime checkInTime = preferences.getCheckInTime();  // 12:00 PM
        LocalTime checkOutTime = preferences.getCheckOutTime(); // 10:00 AM
        
        // Get shifts for this property
        List<Shift> shifts = shiftRepository.findByPropertyId(propertyId);
        if (shifts.isEmpty()) {
            log.error("No shifts defined for property ID: {}", propertyId);
            return tasks;
        }
        
        // Get earliest and latest shift times
        LocalTime earliestShiftStart = shifts.stream()
                .map(Shift::getStartTime)
                .min(LocalTime::compareTo)
                .orElseThrow(() -> new IllegalStateException("No shifts found for property " + propertyId));
                
        LocalTime latestShiftEnd = shifts.stream()
                .map(Shift::getEndTime)
                .max(LocalTime::compareTo)
                .orElseThrow(() -> new IllegalStateException("No shifts found for property " + propertyId));
        
        // 1. Get rooms checking in today (need early daily cleanup before check-in)
        List<RoomBookingDTO> checkInRooms = roomBookingService.getRoomsWithCheckInOnDate(propertyId, date);
        
        // Create a set of room IDs checking in for fast lookup
        List<Integer> checkInRoomIds = checkInRooms.stream()
                .map(RoomBookingDTO::getRoomId)
                .collect(Collectors.toList());
        
        // 2. Get rooms checking out today (need checkout cleaning)
        List<RoomBookingDTO> checkOutRooms = roomBookingService.getRoomsWithCheckOutOnDate(propertyId, date);
        
        // Create a set of room IDs checking out for fast lookup
        List<Integer> checkOutRoomIds = checkOutRooms.stream()
                .map(RoomBookingDTO::getRoomId)
                .collect(Collectors.toList());
        
        // 3. Add early daily cleanup tasks for rooms checking in
        checkInRooms.stream()
                .filter(room -> !checkOutRoomIds.contains(room.getRoomId()))  // Skip rooms that are checking out on the same day
                .forEach(room -> {
                    tasks.add(TaskGenerationDTO.builder()
                            .externalRoomId(room.getRoomId().toString())
                            .roomNumber(room.getRoomNumber())
                            .windowStart(earliestShiftStart) // Can start at beginning of first shift
                            .windowEnd(checkInTime)         // Must finish before check-in time
                            .taskTypeName("EARLY_DAILY_CLEANUP") // Service-level type
                            .dbTaskTypeName("DAILY_CLEANING")   // Database mapping
                            .priority(3) // High priority but less than immediate checkout
                            .date(date)
                            .build());
                });
        
        checkOutRooms.forEach(room -> {
            if (checkInRoomIds.contains(room.getRoomId())) {
                // Immediate checkout cleaning (maps to CHECKOUT_CLEANING in DB)
                // Must be done STRICTLY between checkout time (10 AM) and check-in time (12 PM)
                tasks.add(TaskGenerationDTO.builder()
                        .externalRoomId(room.getRoomId().toString())
                        .roomNumber(room.getRoomNumber())
                        .windowStart(checkOutTime)    // Start after checkout (10 AM)
                        .windowEnd(checkInTime)       // Must finish before check-in (12 PM)
                        .taskTypeName("IMMEDIATE_CHECKOUT_CLEANING") // Service-level type
                        .dbTaskTypeName("CHECKOUT_CLEANING")        // Database mapping
                        .priority(4) // Highest priority
                        .date(date)
                        .build());
            } else {
                // Delayed checkout cleaning (maps to CHECKOUT_CLEANING in DB)
                // Can be done anytime after checkout until end of last shift
                tasks.add(TaskGenerationDTO.builder()
                        .externalRoomId(room.getRoomId().toString())
                        .roomNumber(room.getRoomNumber())
                        .windowStart(checkOutTime)    // Start after checkout (10 AM)
                        .windowEnd(latestShiftEnd)    // Can finish by end of last shift
                        .taskTypeName("DELAYED_CHECKOUT_CLEANING") // Service-level type
                        .dbTaskTypeName("CHECKOUT_CLEANING")      // Database mapping
                        .priority(2) // Medium priority
                        .date(date)
                        .build());
            }
        });
        
        // 4. Get currently occupied rooms (need daily cleaning)
        List<RoomBookingDTO> occupiedRooms = roomBookingService.getOccupiedRooms(propertyId, date);
        
        // Filter out rooms that are checking out today (already handled) or checking in today (already handled)
        // For daily cleaning, we can use the full day but prioritize around mid-day
        // This allows efficient staff utilization
        occupiedRooms.stream()
                .filter(room -> !checkOutRoomIds.contains(room.getRoomId()) && !checkInRoomIds.contains(room.getRoomId()))
                .forEach(room -> {
                    // Daily cleanup for occupied rooms (maps to DAILY_CLEANING in DB)
                    tasks.add(TaskGenerationDTO.builder()
                            .externalRoomId(room.getRoomId().toString())
                            .roomNumber(room.getRoomNumber())
                            .windowStart(earliestShiftStart)  // Can start at beginning of first shift
                            .windowEnd(latestShiftEnd)        // Can finish by end of last shift
                            .taskTypeName("DAILY_CLEANUP")     // Service-level type
                            .dbTaskTypeName("DAILY_CLEANING") // Database mapping
                            .priority(1) // Low priority
                            .date(date)
                            .build());
                });
        
        log.info("Generated {} tasks for property {} on date {}", tasks.size(), propertyId, date);
        return tasks;
    }

    @Override
    public List<TaskAssignmentDTO> assignTasks(Integer propertyId, List<TaskGenerationDTO> tasks, LocalDate date) {
        List<TaskAssignmentDTO> assignedTasks = new ArrayList<>();
        
        if (tasks.isEmpty()) {
            log.info("No tasks to assign for property {} on date {}", propertyId, date);
            return assignedTasks;
        }
        
        // Load all shifts for this property
        List<Shift> shifts = shiftRepository.findByPropertyId(propertyId);
        if (shifts.isEmpty()) {
            log.error("No shifts defined for property {}", propertyId);
            return assignedTasks;
        }
        
        // Sort tasks by priority (highest first)
        List<TaskGenerationDTO> sortedTasks = tasks.stream()
                .sorted(Comparator.comparing(TaskGenerationDTO::getPriority).reversed())
                .collect(Collectors.toList());
        
        // Process each shift
        for (Shift shift : shifts) {
            // Get staff available for this shift
            List<HousekeepingStaff> availableStaff = staffRepository.findAvailableStaffByPropertyIdAndShiftId(
                    propertyId, shift.getShiftId(), date);
            
            if (availableStaff.isEmpty()) {
                log.warn("No available staff for shift {} on date {}", shift.getShiftName(), date);
                continue;
            }
            
            // Each staff will have a list of assigned tasks and their end times
            Map<Integer, List<LocalTime>> staffEndTimes = new HashMap<>();
            availableStaff.forEach(staff -> staffEndTimes.put(staff.getStaffId(), new ArrayList<>()));
            
            // Filter tasks that can be done in this shift
            List<TaskGenerationDTO> shiftTasks = sortedTasks.stream()
                    .filter(task -> isTaskWithinShift(task, shift))
                    .collect(Collectors.toList());
            
            for (TaskGenerationDTO task : shiftTasks) {
                // Find the staff member with the earliest availability
                Optional<Map.Entry<Integer, List<LocalTime>>> optimalStaffEntry = findOptimalStaff(
                        staffEndTimes, availableStaff, task, shift);
                
                if (optimalStaffEntry.isPresent()) {
                    Map.Entry<Integer, List<LocalTime>> entry = optimalStaffEntry.get();
                    Integer staffId = entry.getKey();
                    List<LocalTime> endTimes = entry.getValue();
                    
                    // Get the staff member
                    HousekeepingStaff staff = availableStaff.stream()
                            .filter(s -> s.getStaffId().equals(staffId))
                            .findFirst()
                            .get();
                    
                    // Calculate start time
                    LocalTime startTime = endTimes.isEmpty() ? 
                            shift.getStartTime() : 
                            endTimes.get(endTimes.size() - 1);
                    
                    // Make sure start time is not before the task window
                    if (startTime.isBefore(task.getWindowStart())) {
                        startTime = task.getWindowStart();
                    }
                    
                    // Get the task duration
                    CleanTaskType taskType = getTaskType(task);
                    Duration duration = taskType.getRequiredTime();
                    
                    // Calculate end time
                    LocalTime endTime = startTime.plus(duration);
                    
                    // Ensure task can be completed before shift ends and within task window
                    if (endTime.isAfter(shift.getEndTime()) || endTime.isAfter(task.getWindowEnd())) {
                        continue; // Skip this task if it would go beyond shift end or task window
                    }
                    
                    // Assign the task
                    TaskAssignmentDTO assignedTask = TaskAssignmentDTO.builder()
                            .externalRoomId(task.getExternalRoomId())
                            .roomNumber(task.getRoomNumber())
                            .staffId(staffId)
                            .staffName(staff.getStaffName())
                            .startTime(startTime)
                            .taskTypeName(task.getTaskTypeName())
                            .dbTaskTypeName(task.getDbTaskTypeName())
                            .duration(duration)
                            .date(date)
                            .build();
                    
                    assignedTasks.add(assignedTask);
                    
                    // Update the staff's end times
                    endTimes.add(endTime);
                    
                    // Remove the task from the list of tasks
                    sortedTasks.remove(task);
                }
            }
        }
        
        log.info("Assigned {} tasks for property {} on date {}", assignedTasks.size(), propertyId, date);
        return assignedTasks;
    }

    @Override
    public int calculateStaffShortfall(Integer propertyId, LocalDate date, List<TaskGenerationDTO> unassignedTasks) {
        if (unassignedTasks.isEmpty()) {
            return 0;
        }
        
        // Calculate total unassigned minutes
        int totalUnassignedMinutes = 0;
        
        for (TaskGenerationDTO task : unassignedTasks) {
            CleanTaskType taskType = getTaskType(task);
            totalUnassignedMinutes += taskType.getRequiredTime().toMinutes();
        }
        
        // Get average shift duration
        List<Shift> shifts = shiftRepository.findByPropertyId(propertyId);
        if (shifts.isEmpty()) {
            // Default to 8 hours if no shifts defined
            log.warn("No shifts defined for property {}, using default 8 hour shift", propertyId);
            return (int) Math.ceil(totalUnassignedMinutes / (8.0 * 60));
        }
        
        double averageShiftMinutes = shifts.stream()
                .mapToLong(shift -> Duration.between(shift.getStartTime(), shift.getEndTime()).toMinutes())
                .average()
                .orElse(8 * 60); // Default to 8 hours
        
        // Calculate number of additional staff needed
        return (int) Math.ceil(totalUnassignedMinutes / averageShiftMinutes);
    }
    
    /**
     * Check if a task can be performed within a shift's time window
     */
    private boolean isTaskWithinShift(TaskGenerationDTO task, Shift shift) {
        // Task can be performed in this shift if at least part of its window overlaps with the shift
        return !(task.getWindowEnd().isBefore(shift.getStartTime()) || 
                task.getWindowStart().isAfter(shift.getEndTime()));
    }
    
    /**
     * Find the optimal staff member to assign a task to
     */
    private Optional<Map.Entry<Integer, List<LocalTime>>> findOptimalStaff(
            Map<Integer, List<LocalTime>> staffEndTimes,
            List<HousekeepingStaff> availableStaff,
            TaskGenerationDTO task,
            Shift shift) {
        
        // Get task duration
        CleanTaskType taskType = getTaskType(task);
        Duration taskDuration = taskType.getRequiredTime();
        
        // Find the staff with the earliest availability who can complete the task within the task window and shift
        return staffEndTimes.entrySet().stream()
                .filter(entry -> {
                    // Staff's ID
                    Integer staffId = entry.getKey();
                    
                    // Find the staff's skill level
                    Optional<HousekeepingStaff> staffOpt = availableStaff.stream()
                            .filter(s -> s.getStaffId().equals(staffId))
                            .findFirst();
                    
                    if (staffOpt.isEmpty()) {
                        return false;
                    }
                    
                    // Staff's last end time or shift start time if no tasks yet
                    List<LocalTime> endTimes = entry.getValue();
                    LocalTime startTime = endTimes.isEmpty() ? 
                            shift.getStartTime() : 
                            endTimes.get(endTimes.size() - 1);
                    
                    // Make sure start time is not before the task window
                    if (startTime.isBefore(task.getWindowStart())) {
                        startTime = task.getWindowStart();
                    }
                    
                    // Calculate when the task would end
                    LocalTime endTime = startTime.plus(taskDuration);
                    
                    // Check if task can be completed within the task window and shift
                    return !endTime.isAfter(task.getWindowEnd()) && !endTime.isAfter(shift.getEndTime());
                })
                .min(Comparator.comparing(entry -> {
                    // Compare based on the earliest available time
                    List<LocalTime> endTimes = entry.getValue();
                    LocalTime availableTime = endTimes.isEmpty() ? shift.getStartTime() : endTimes.get(endTimes.size() - 1);
                    
                    // Ensure we don't start before the task's window start
                    return availableTime.isBefore(task.getWindowStart()) ? task.getWindowStart() : availableTime;
                }));
    }
    
    /**
     * Get a task type from the cache or database using the DB-level task type name
     */
    private CleanTaskType getTaskType(TaskGenerationDTO task) {
        String dbTaskTypeName = task.getDbTaskTypeName();
        
        if (taskTypeCache.containsKey(dbTaskTypeName)) {
            return taskTypeCache.get(dbTaskTypeName);
        }
        
        // If not in cache, try to get from database
        Optional<CleanTaskType> taskTypeOpt = taskTypeRepository.findByTypeName(dbTaskTypeName);
        if (taskTypeOpt.isPresent()) {
            CleanTaskType taskType = taskTypeOpt.get();
            taskTypeCache.put(dbTaskTypeName, taskType);
            return taskType;
        }
        
        // If not found, create a default task type with 30 minutes duration
        log.warn("Task type {} not found in database, using default duration of 30 minutes", dbTaskTypeName);
        return CleanTaskType.builder()
                .typeName(dbTaskTypeName)
                .requiredTime(Duration.ofMinutes(30))
                .build();
    }
    
    /**
     * Get a task type from the cache or database by name
     */
    private CleanTaskType getTaskTypeByName(String typeName) {
        if (taskTypeCache.containsKey(typeName)) {
            return taskTypeCache.get(typeName);
        }
        
        // If not in cache, try to get from database
        Optional<CleanTaskType> taskTypeOpt = taskTypeRepository.findByTypeName(typeName);
        if (taskTypeOpt.isPresent()) {
            CleanTaskType taskType = taskTypeOpt.get();
            taskTypeCache.put(typeName, taskType);
            return taskType;
        }
        
        // If not found, create a default task type with 30 minutes duration
        log.warn("Task type {} not found, using default duration of 30 minutes", typeName);
        return CleanTaskType.builder()
                .typeName(typeName)
                .requiredTime(Duration.ofMinutes(30))
                .build();
    }
} 