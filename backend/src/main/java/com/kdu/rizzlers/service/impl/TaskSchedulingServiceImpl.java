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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of TaskSchedulingService for generating and assigning tasks.
 * Uses a priority-driven greedy algorithm with strict UTC time handling.
 */
@Service
@Slf4j
public class TaskSchedulingServiceImpl implements TaskSchedulingService {

    // Define the property's timezone (IST) and the system's timezone (UTC)
    private static final ZoneId PROPERTY_TIMEZONE = ZoneId.of("Asia/Kolkata");
    private static final ZoneId SYSTEM_TIMEZONE = ZoneId.of("UTC");

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
        log.info("Task Type Cache initialized with types: {}", taskTypeCache.keySet());
    }

    // Helper record for time ranges (implements Comparable for sorting)
    private record TimeRange(LocalTime start, LocalTime end) implements Comparable<TimeRange> {
        @Override
        public int compareTo(TimeRange other) {
            // Primarily sort by start time, then by end time if starts are equal
            int startComparison = this.start.compareTo(other.start);
            if (startComparison != 0) {
                return startComparison;
            }
            return this.end.compareTo(other.end);
        }
    }

     /** Helper record to store shift info with UTC times */
     private record ShiftUTC(Shift originalShift, LocalTime startTimeUTC, LocalTime endTimeUTC) {}

     /** Helper record to store a potential assignment option */
     private record AssignmentOption(int staffId, ShiftUTC shiftUTC, LocalTime startTimeUTC, LocalTime endTimeUTC) {}

    // Helper TimeWindow class for shift and task constraints
    private record TimeWindow(LocalTime start, LocalTime end) {
        public boolean isValid() {
            return !start.isAfter(end);
        }
        
        public boolean canFitTask(Duration taskDuration) {
            return !start.plus(taskDuration).isAfter(end);
        }
        
        public Duration duration() {
            return Duration.between(start, end);
        }
    }

    // Helper class to track staff assignments during backtracking
    private static class StaffAssignmentTracker {
        private final Map<Integer, List<TaskAssignmentDTO>> staffAssignments = new HashMap<>();
        private final Map<Integer, Integer> staffToShiftId = new HashMap<>(); // Staff ID -> Shift ID
        
        public void addAssignment(HousekeepingStaff staff, TaskAssignmentDTO task, Integer shiftId) {
            staffAssignments.computeIfAbsent(staff.getStaffId(), k -> new ArrayList<>()).add(task);
            staffToShiftId.putIfAbsent(staff.getStaffId(), shiftId);
        }
        
        public void removeAssignment(HousekeepingStaff staff, TaskAssignmentDTO task) {
            List<TaskAssignmentDTO> tasks = staffAssignments.get(staff.getStaffId());
            if (tasks != null) {
                tasks.remove(task);
                if (tasks.isEmpty()) {
                    staffToShiftId.remove(staff.getStaffId());
                }
            }
        }
        
        public List<TaskAssignmentDTO> getAssignments(Integer staffId) {
            return staffAssignments.getOrDefault(staffId, Collections.emptyList());
        }
        
        public Integer getAssignedShift(Integer staffId) {
            return staffToShiftId.get(staffId);
        }
        
        public boolean isStaffAssignedToShift(Integer staffId, Integer shiftId) {
            Integer assignedShift = staffToShiftId.get(staffId);
            return assignedShift != null && assignedShift.equals(shiftId);
        }
        
        public boolean canAssignToShift(Integer staffId, Integer shiftId) {
            Integer assignedShift = staffToShiftId.get(staffId);
            return assignedShift == null || assignedShift.equals(shiftId);
        }
        
        public Duration calculateStaffWorkload(Integer staffId) {
            return getAssignments(staffId).stream()
                .map(TaskAssignmentDTO::getDuration)
                .reduce(Duration.ZERO, Duration::plus);
        }
        
        public Map<Integer, List<TaskAssignmentDTO>> getAllAssignments() {
            return staffAssignments;
        }
    }

    @Override
    public List<TaskGenerationDTO> generateTasks(Integer propertyId, LocalDate date) {
        List<TaskGenerationDTO> tasks = new ArrayList<>();
        // Use a Set to track generated tasks and prevent duplicates (RoomId + TaskTypeName)
        Set<String> generatedTaskKeys = new HashSet<>();
        
        Optional<PropertyPreferences> propertyPreferencesOpt = propertyPreferencesRepository.findByPropertyId(propertyId);
        if (propertyPreferencesOpt.isEmpty()) {
            log.error("Property preferences not found for property ID: {}. Cannot generate tasks.", propertyId);
            return tasks;
        }
        
        PropertyPreferences preferences = propertyPreferencesOpt.get();
        LocalTime checkInTimeIST = preferences.getCheckInTime();  // e.g., 12:00 IST
        LocalTime checkOutTimeIST = preferences.getCheckOutTime(); // e.g., 10:00 IST

        // Convert preferences to UTC for internal calculations
        LocalTime checkInTimeUTC = convertToUTC(checkInTimeIST, date);
        LocalTime checkOutTimeUTC = convertToUTC(checkOutTimeIST, date);
        log.info("Property {} Preferences: Check-in: {} IST -> {} UTC, Check-out: {} IST -> {} UTC",
                 propertyId, checkInTimeIST, checkInTimeUTC, checkOutTimeIST, checkOutTimeUTC);

        List<Shift> shifts = shiftRepository.findByPropertyId(propertyId);
        if (shifts.isEmpty()) {
            log.error("No shifts defined for property ID: {}. Cannot generate tasks.", propertyId);
            return tasks;
        }
        
        // Convert shifts to UTC to find overall range
         List<ShiftUTC> shiftsUTC = shifts.stream()
             .map(shift -> new ShiftUTC(shift, convertToUTC(shift.getStartTime(), date), convertToUTC(shift.getEndTime(), date)))
             .toList(); // Use List instead of stream for min/max


        LocalTime earliestShiftStartUTC = shiftsUTC.stream()
                .map(ShiftUTC::startTimeUTC)
                .min(LocalTime::compareTo)
                .orElse(LocalTime.MIN); // Fallback
        LocalTime latestShiftEndUTC = shiftsUTC.stream()
                .map(ShiftUTC::endTimeUTC)
                .max(LocalTime::compareTo)
                .orElse(LocalTime.MAX); // Fallback
        log.info("Overall shift range for property {}: {} UTC - {} UTC", propertyId, earliestShiftStartUTC, latestShiftEndUTC);

        
        // 1. Get rooms checking in today
        List<RoomBookingDTO> checkInRooms = roomBookingService.getRoomsWithCheckInOnDate(propertyId, date);
        Set<Integer> checkInRoomIds = checkInRooms.stream().map(RoomBookingDTO::getRoomId).collect(Collectors.toSet());
        log.info("Rooms checking in today ({}): {}", checkInRooms.size(), checkInRoomIds);

        // 2. Get rooms checking out today
        List<RoomBookingDTO> checkOutRooms = roomBookingService.getRoomsWithCheckOutOnDate(propertyId, date);
        Set<Integer> checkOutRoomIds = checkOutRooms.stream().map(RoomBookingDTO::getRoomId).collect(Collectors.toSet());
        log.info("Rooms checking out today ({}): {}", checkOutRooms.size(), checkOutRoomIds);

        // 3. Identify immediate checkout rooms
        Set<Integer> immediateCheckoutRoomIds = new HashSet<>(checkInRoomIds);
        immediateCheckoutRoomIds.retainAll(checkOutRoomIds);
        log.info("Rooms needing immediate checkout cleaning ({}): {}", immediateCheckoutRoomIds.size(), immediateCheckoutRoomIds);

        // Helper lambda to add a task only if it's unique
        java.util.function.Consumer<TaskGenerationDTO> addTaskIfUnique = (task) -> {
            String key = task.getExternalRoomId() + "-" + task.getTaskTypeName();
            if (generatedTaskKeys.add(key)) {
                tasks.add(task);
            } else {
                log.warn("Skipping duplicate task generation for key: {}", key);
            }
        };

        // 4. Generate IMMEDIATE_CHECKOUT_CLEANING tasks
        for (RoomBookingDTO room : checkOutRooms) {
            if (immediateCheckoutRoomIds.contains(room.getRoomId())) {
                // Strict Window: After checkout, Before check-in (UTC)
                addTaskIfUnique.accept(TaskGenerationDTO.builder()
                        .externalRoomId(room.getRoomId().toString())
                        .roomNumber(room.getRoomNumber())
                        .windowStart(checkOutTimeUTC) // Constraint: Must start >= 04:30 UTC
                        .windowEnd(checkInTimeUTC)   // Constraint: Must end <= 06:30 UTC
                        .taskTypeName("IMMEDIATE_CHECKOUT_CLEANING")
                        .dbTaskTypeName("CHECKOUT_CLEANING") // DB Mapping
                        .priority(4) // Highest priority
                        .date(date)
                        .build());
            }
        }

        // 5. Generate EARLY_DAILY_CLEANUP tasks (Check-in today, not checkout today)
        for (RoomBookingDTO room : checkInRooms) {
            if (!immediateCheckoutRoomIds.contains(room.getRoomId())) {
                 // Strict Window: After first shift starts, Before check-in (UTC)
                addTaskIfUnique.accept(TaskGenerationDTO.builder()
                        .externalRoomId(room.getRoomId().toString())
                        .roomNumber(room.getRoomNumber())
                        .windowStart(earliestShiftStartUTC) // Constraint: Can start >= 08:00 UTC
                        .windowEnd(checkInTimeUTC)        // Constraint: Must end <= 06:30 UTC
                        .taskTypeName("EARLY_DAILY_CLEANUP")
                        .dbTaskTypeName("DAILY_CLEANING") // DB Mapping
                        .priority(3) // High priority
                        .date(date)
                        .build());
            }
        }

        // 6. Generate DELAYED_CHECKOUT_CLEANING tasks (Checkout today, not check-in today)
        for (RoomBookingDTO room : checkOutRooms) {
            if (!immediateCheckoutRoomIds.contains(room.getRoomId())) {
                // Window: After checkout, Before last shift ends (UTC)
                addTaskIfUnique.accept(TaskGenerationDTO.builder()
                        .externalRoomId(room.getRoomId().toString())
                        .roomNumber(room.getRoomNumber())
                        .windowStart(checkOutTimeUTC)       // Constraint: Must start >= 04:30 UTC
                        .windowEnd(latestShiftEndUTC)     // Constraint: Can end <= 16:00 UTC
                        .taskTypeName("DELAYED_CHECKOUT_CLEANING")
                        .dbTaskTypeName("CHECKOUT_CLEANING") // DB Mapping
                        .priority(2) // Medium priority
                        .date(date)
                        .build());
            }
        }
        
        // 7. Generate DAILY_CLEANUP tasks (Occupied, not checking in/out today)
        List<RoomBookingDTO> occupiedRooms = roomBookingService.getOccupiedRooms(propertyId, date);
        occupiedRooms.stream()
                .filter(room -> !checkOutRoomIds.contains(room.getRoomId()) && !checkInRoomIds.contains(room.getRoomId()))
                .forEach(room -> {
                    // Window: Within overall shift times (UTC)
                    addTaskIfUnique.accept(TaskGenerationDTO.builder()
                            .externalRoomId(room.getRoomId().toString())
                            .roomNumber(room.getRoomNumber())
                            .windowStart(earliestShiftStartUTC) // Constraint: Can start >= 08:00 UTC
                            .windowEnd(latestShiftEndUTC)       // Constraint: Can end <= 16:00 UTC
                            .taskTypeName("DAILY_CLEANUP")
                            .dbTaskTypeName("DAILY_CLEANING") // DB Mapping
                            .priority(1) // Low priority
                            .date(date)
                            .build());
                });
        
        log.info("Generated {} unique tasks for property {} on date {}. Task details (times in UTC):", tasks.size(), propertyId, date);
        tasks.forEach(task -> log.debug("  - Task: Room {}, Type: {}, Prio: {}, Win: {} - {}",
                task.getExternalRoomId(), task.getTaskTypeName(), task.getPriority(), task.getWindowStart(), task.getWindowEnd()));
        return tasks;
    }

    @Override
    public List<TaskAssignmentDTO> assignTasks(Integer propertyId, List<TaskGenerationDTO> tasks, LocalDate date) {
        if (tasks.isEmpty()) {
            log.info("No tasks generated for property {} on date {}, assignment skipped.", propertyId, date);
            return Collections.emptyList();
        }
        
        List<Shift> shiftsDB = shiftRepository.findByPropertyId(propertyId);
        if (shiftsDB.isEmpty()) {
            log.error("No shifts defined for property {}. Cannot assign tasks.", propertyId);
            return Collections.emptyList();
        }
        
        // Convert shifts to UTC for internal logic and sort chronologically
        List<ShiftUTC> shiftsUTC = shiftsDB.stream()
            .map(shift -> {
                LocalTime startUTC = convertToUTC(shift.getStartTime(), date);
                LocalTime endUTC = convertToUTC(shift.getEndTime(), date);
                log.debug("Shift {} converted: {} IST -> {} UTC, {} IST -> {} UTC", 
                    shift.getShiftName(), shift.getStartTime(), startUTC, shift.getEndTime(), endUTC);
                return new ShiftUTC(shift, startUTC, endUTC);
            })
            .sorted(Comparator.comparing(ShiftUTC::startTimeUTC))
            .collect(Collectors.toList());
        
        log.info("Processing task assignment with {} shifts (converted to UTC):", shiftsUTC.size());
        shiftsUTC.forEach(s -> log.info("  - Shift: {} (ID:{}) {} - {} UTC", 
            s.originalShift.getShiftName(), s.originalShift.getShiftId(), s.startTimeUTC, s.endTimeUTC));

        // Fetch available staff
        List<HousekeepingStaff> allStaff = staffRepository.findAvailableStaffByPropertyId(propertyId, date);
        if (allStaff.isEmpty()) {
            log.warn("No staff available for property {} on date {}. Cannot assign tasks.", propertyId, date);
            return Collections.emptyList();
        }
        
        // Sort tasks by priority (highest first)
        List<TaskGenerationDTO> sortedTasks = new ArrayList<>(tasks);
        sortedTasks.sort(Comparator.comparing(TaskGenerationDTO::getPriority).reversed()
                .thenComparing(TaskGenerationDTO::getWindowStart));
        
        log.info("Attempting to assign {} tasks using backtracking algorithm", sortedTasks.size());
        
        // Get property preferences for constraint calculations
        Optional<PropertyPreferences> prefsOpt = propertyPreferencesRepository.findByPropertyId(propertyId);
        PropertyPreferences prefs = prefsOpt.orElse(null);
        LocalTime checkOutTimeUTC = prefs != null ? convertToUTC(prefs.getCheckOutTime(), date) : null;
        LocalTime checkInTimeUTC = prefs != null ? convertToUTC(prefs.getCheckInTime(), date) : null;

        // Track staff assignments and shift assignments
        Map<Integer, List<TaskAssignmentDTO>> staffAssignments = new HashMap<>();
        Map<Integer, Integer> staffToShiftMap = new HashMap<>(); // Staff ID -> Shift ID
        
        // Initialize empty assignment lists for each staff
        allStaff.forEach(staff -> staffAssignments.put(staff.getStaffId(), new ArrayList<>()));
        
        // Backtracking assignment
        List<TaskAssignmentDTO> assignedTasksResult = new ArrayList<>();
        boolean success = backtrackAssign(sortedTasks, 0, allStaff, shiftsUTC, checkOutTimeUTC, 
            checkInTimeUTC, staffAssignments, staffToShiftMap, assignedTasksResult, date);
        
        if (!success) {
            log.warn("Could not assign all tasks using backtracking. Assigned {}/{} tasks.", 
                assignedTasksResult.size(), sortedTasks.size());
        } else {
            log.info("Initial assignment via backtracking successful for {} tasks.", assignedTasksResult.size());
        }

        // --- Workload Balancing Step ---
        log.info("Starting workload balancing...");
        balanceWorkload(staffAssignments, staffToShiftMap, shiftsUTC, allStaff, date);
        
        // Reconstruct the final list from potentially modified assignments
        List<TaskAssignmentDTO> finalAssignedTasks = staffAssignments.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());

        // Log assignment results after balancing
        log.info("Assignment complete after balancing. Assigned {} of {} tasks.", 
                finalAssignedTasks.size(), sortedTasks.size());
        
        // Log final workload distribution
        logFinalWorkloadDistribution(staffAssignments, staffToShiftMap, shiftsDB, allStaff);
        
        return finalAssignedTasks;
    }
    
    /**
     * Logs the final workload distribution after balancing.
     */
    private void logFinalWorkloadDistribution(
            Map<Integer, List<TaskAssignmentDTO>> staffAssignments,
            Map<Integer, Integer> staffToShiftMap,
            List<Shift> shiftsDB,
            List<HousekeepingStaff> allStaff) {
                
        StringBuilder workloadInfo = new StringBuilder("Final Staff workload distribution:\n");
        staffAssignments.forEach((staffId, staffTasks) -> {
            HousekeepingStaff staff = allStaff.stream()
                .filter(s -> s.getStaffId().equals(staffId))
                .findFirst()
                .orElse(null);
            
            if (staff != null && !staffTasks.isEmpty()) {
                Duration totalWorkload = staffTasks.stream()
                    .map(TaskAssignmentDTO::getDuration)
                    .reduce(Duration.ZERO, Duration::plus);
                
                Integer shiftId = staffToShiftMap.get(staffId);
                String shiftName = shiftsDB.stream()
                    .filter(s -> s.getShiftId().equals(shiftId))
                    .map(Shift::getShiftName)
                    .findFirst()
                    .orElse("Unknown");
                
                workloadInfo.append(String.format("Staff %d (%s): %d tasks, %d hours %d minutes in shift '%s'\n", 
                    staffId, staff.getStaffName(), staffTasks.size(), 
                    totalWorkload.toHours(), totalWorkload.toMinutesPart(),
                    shiftName));
            }
        });
        log.info(workloadInfo.toString());
    }

    /**
     * Attempts to balance workload among staff within the same shift.
     */
    private void balanceWorkload(
            Map<Integer, List<TaskAssignmentDTO>> staffAssignments,
            Map<Integer, Integer> staffToShiftMap,
            List<ShiftUTC> shiftsUTC,
            List<HousekeepingStaff> allStaff,
            LocalDate date) {

        final Duration IMBALANCE_THRESHOLD = Duration.ofMinutes(30);
        boolean balanced = false;
        int balancingIterations = 0;
        final int MAX_BALANCING_ITERATIONS = 10; // Prevent infinite loops

        while (!balanced && balancingIterations < MAX_BALANCING_ITERATIONS) {
            balanced = true; // Assume balanced until proven otherwise
            balancingIterations++;
            log.debug("Balancing iteration {}", balancingIterations);

            // Group staff by shift
            Map<Integer, List<Integer>> staffByShiftId = new HashMap<>();
            staffToShiftMap.forEach((staffId, shiftId) -> 
                staffByShiftId.computeIfAbsent(shiftId, k -> new ArrayList<>()).add(staffId));

            for (Map.Entry<Integer, List<Integer>> entry : staffByShiftId.entrySet()) {
                Integer shiftId = entry.getKey();
                List<Integer> staffInShift = entry.getValue();

                if (staffInShift.size() <= 1) {
                    continue; // Cannot balance single staff member
                }

                // Find the specific ShiftUTC object for this shift
                ShiftUTC currentShift = shiftsUTC.stream()
                    .filter(s -> s.originalShift.getShiftId().equals(shiftId))
                    .findFirst()
                    .orElse(null);
                
                if (currentShift == null) {
                    log.warn("Could not find ShiftUTC for shift ID {} during balancing.", shiftId);
                    continue;
                }

                // Calculate workloads for staff in this shift
                Map<Integer, Duration> workloads = new HashMap<>();
                staffInShift.forEach(staffId -> 
                    workloads.put(staffId, staffAssignments.get(staffId).stream()
                                        .map(TaskAssignmentDTO::getDuration)
                                        .reduce(Duration.ZERO, Duration::plus)));

                // Find most and least busy staff
                Integer mostBusyStaffId = Collections.max(workloads.entrySet(), Map.Entry.comparingByValue()).getKey();
                Integer leastBusyStaffId = Collections.min(workloads.entrySet(), Map.Entry.comparingByValue()).getKey();
                Duration maxWorkload = workloads.get(mostBusyStaffId);
                Duration minWorkload = workloads.get(leastBusyStaffId);
                Duration imbalance = maxWorkload.minus(minWorkload);

                log.trace("Shift {}: Most Busy: {} ({} min), Least Busy: {} ({} min), Imbalance: {} min", 
                        shiftId, mostBusyStaffId, maxWorkload.toMinutes(), 
                        leastBusyStaffId, minWorkload.toMinutes(), imbalance.toMinutes());

                // If imbalance is significant, try to move a task
                if (imbalance.compareTo(IMBALANCE_THRESHOLD) > 0) {
                    balanced = false; // Imbalance found, need another pass potentially
                    boolean moveMade = tryMoveTaskForBalancing(
                        mostBusyStaffId, 
                        leastBusyStaffId, 
                        staffAssignments, 
                        currentShift,
                        allStaff,
                        date);
                    
                    if (moveMade) {
                        // If a move was made, break from this shift check and restart the outer loop
                        // to re-evaluate workloads based on the change.
                        log.debug("Move made for shift {}, restarting balancing check.", shiftId);
                        break; 
                    }
                }
            }
        }
        if (balancingIterations >= MAX_BALANCING_ITERATIONS) {
            log.warn("Workload balancing reached max iterations ({}) without achieving full balance.", MAX_BALANCING_ITERATIONS);
        }
        log.info("Workload balancing finished.");
    }

    /**
     * Tries to move a single task from the most busy to the least busy staff to improve balance.
     * Returns true if a move was made, false otherwise.
     */
    private boolean tryMoveTaskForBalancing(
            Integer mostBusyStaffId,
            Integer leastBusyStaffId,
            Map<Integer, List<TaskAssignmentDTO>> staffAssignments,
            ShiftUTC shift,
            List<HousekeepingStaff> allStaff,
            LocalDate date) {

        List<TaskAssignmentDTO> busyStaffTasks = new ArrayList<>(staffAssignments.get(mostBusyStaffId));
        List<TaskAssignmentDTO> leastBusyStaffTasks = staffAssignments.get(leastBusyStaffId);

        // Sort tasks by duration (shortest first) - smaller tasks are easier to fit
        busyStaffTasks.sort(Comparator.comparing(TaskAssignmentDTO::getDuration));

        HousekeepingStaff leastBusyStaffDetails = allStaff.stream()
            .filter(s -> s.getStaffId().equals(leastBusyStaffId))
            .findFirst().orElse(null);
        
        if (leastBusyStaffDetails == null) return false; // Should not happen

        for (TaskAssignmentDTO taskToMove : busyStaffTasks) {
            Duration taskDuration = taskToMove.getDuration();
            
            // Define the window where the task must occur (original task constraints within shift)
            LocalTime taskMinStart = taskToMove.getOriginalWindowStart(); // Need to store original window on DTO?
            LocalTime taskMaxEnd = taskToMove.getOriginalWindowEnd();     // Or re-fetch/recalculate?
            
            // For simplicity, let's assume the task needs to fit within the SHIFT boundaries 
            // and respect the LEAST busy staff's current schedule.
            // A more robust implementation would re-evaluate the original task constraints.
            LocalTime windowStart = shift.startTimeUTC;
            LocalTime windowEnd = shift.endTimeUTC;

            // Check if the least busy staff has a slot for this task
            List<TimeRange> leastBusySchedule = leastBusyStaffTasks.stream()
                .map(t -> new TimeRange(t.getStartTime(), t.getStartTime().plus(t.getDuration())))
                .collect(Collectors.toList());
            
            LocalTime newStartTime = findEarliestStartTime(windowStart, windowEnd, taskDuration, leastBusySchedule);

            if (newStartTime != null) {
                // Found a potential move!
                log.info("Rebalancing: Moving task Room {} ({}, {} min) from Staff {} to Staff {}", 
                        taskToMove.getExternalRoomId(), taskToMove.getTaskTypeName(), taskDuration.toMinutes(),
                        mostBusyStaffId, leastBusyStaffId);

                // Remove from busy staff
                staffAssignments.get(mostBusyStaffId).remove(taskToMove);

                // Update the task DTO itself
                taskToMove.setStaffId(leastBusyStaffId);
                taskToMove.setStaffName(leastBusyStaffDetails.getStaffName());
                taskToMove.setStartTime(newStartTime);
                
                // Add to least busy staff
                leastBusyStaffTasks.add(taskToMove);
                Collections.sort(leastBusyStaffTasks, Comparator.comparing(TaskAssignmentDTO::getStartTime)); // Keep sorted

                return true; // Move successful
            }
        }
        return false; // No suitable task found to move
    }

    // --- Add accessors for original window start/end in TaskAssignmentDTO ---
    // This might require modifying the TaskAssignmentDTO class
    // Example (assuming TaskAssignmentDTO has these fields now):
    // private LocalTime getOriginalWindowStart(TaskAssignmentDTO task) { return task.getOriginalWindowStart(); }
    // private LocalTime getOriginalWindowEnd(TaskAssignmentDTO task) { return task.getOriginalWindowEnd(); }

    /**
     * Backtracking algorithm to assign tasks to staff
     */
    private boolean backtrackAssign(
            List<TaskGenerationDTO> tasks,
            int taskIndex,
            List<HousekeepingStaff> allStaff,
            List<ShiftUTC> shiftsUTC,
            LocalTime checkOutTimeUTC,
            LocalTime checkInTimeUTC,
            Map<Integer, List<TaskAssignmentDTO>> staffAssignments,
            Map<Integer, Integer> staffToShiftMap,
            List<TaskAssignmentDTO> assignedTasks,
            LocalDate date) {
        
        // Base case: all tasks assigned
        if (taskIndex >= tasks.size()) {
            return true;
        }
        
        TaskGenerationDTO currentTask = tasks.get(taskIndex);
        log.debug("Attempting to assign task {}: Room {}, Type: {}", 
                taskIndex, currentTask.getExternalRoomId(), currentTask.getTaskTypeName());
        
        // Get task type and duration
        CleanTaskType taskType = getTaskType(currentTask);
        if (taskType == null || taskType.getRequiredTime() == null) {
            log.error("Task type definition missing for {}. Skipping task.", currentTask.getTaskTypeName());
            // Skip this task and try to assign the rest
            return backtrackAssign(tasks, taskIndex + 1, allStaff, shiftsUTC, checkOutTimeUTC, 
                                 checkInTimeUTC, staffAssignments, staffToShiftMap, assignedTasks, date);
        }
        
        Duration taskDuration = taskType.getRequiredTime();
        
        // Calculate task-specific constraints
        LocalTime taskMinStartUTC = currentTask.getWindowStart();
        LocalTime taskMaxEndUTC = currentTask.getWindowEnd();
        
        // Apply checkout/check-in time constraints if available
        if (checkOutTimeUTC != null && checkInTimeUTC != null) {
            if ("IMMEDIATE_CHECKOUT_CLEANING".equals(currentTask.getTaskTypeName()) || 
                "DELAYED_CHECKOUT_CLEANING".equals(currentTask.getTaskTypeName())) {
                if (taskMinStartUTC.isBefore(checkOutTimeUTC)) {
                    taskMinStartUTC = checkOutTimeUTC; // Must start >= checkout time
                }
            }
            
            if ("EARLY_DAILY_CLEANUP".equals(currentTask.getTaskTypeName())) {
                if (taskMaxEndUTC.isAfter(checkInTimeUTC)) {
                    taskMaxEndUTC = checkInTimeUTC; // Must end <= check-in time
                }
            }
        }
        
        // Prioritize shifts based on task type
        List<ShiftUTC> prioritizedShifts = new ArrayList<>(shiftsUTC);
        if ("EARLY_DAILY_CLEANUP".equals(currentTask.getTaskTypeName())) {
            // Early tasks prefer earlier shifts
            prioritizedShifts.sort(Comparator.comparing(ShiftUTC::startTimeUTC));
        } else if ("DELAYED_CHECKOUT_CLEANING".equals(currentTask.getTaskTypeName())) {
            // Later tasks prefer later shifts
            prioritizedShifts.sort((s1, s2) -> s2.startTimeUTC().compareTo(s1.startTimeUTC()));
        }
        
        // Try each shift
        for (ShiftUTC shift : prioritizedShifts) {
            // Calculate the time window for this task in this shift
            LocalTime windowStart = taskMinStartUTC.isBefore(shift.startTimeUTC) ? 
                shift.startTimeUTC : taskMinStartUTC;
            
            LocalTime windowEnd = taskMaxEndUTC.isAfter(shift.endTimeUTC) ? 
                shift.endTimeUTC : taskMaxEndUTC;
            
            // Skip if window is invalid or too small for task
            if (windowStart.isAfter(windowEnd) || 
                windowStart.plus(taskDuration).isAfter(windowEnd)) {
                continue;
            }
            
            // Sort staff by workload and shift preference
            List<HousekeepingStaff> sortedStaff = new ArrayList<>(allStaff);
            sortedStaff.sort((s1, s2) -> {
                // First check if staff is already assigned to a different shift
                boolean s1InDifferentShift = staffToShiftMap.containsKey(s1.getStaffId()) && 
                                            !staffToShiftMap.get(s1.getStaffId()).equals(shift.originalShift.getShiftId());
                boolean s2InDifferentShift = staffToShiftMap.containsKey(s2.getStaffId()) && 
                                            !staffToShiftMap.get(s2.getStaffId()).equals(shift.originalShift.getShiftId());
                
                if (s1InDifferentShift && !s2InDifferentShift) return 1;
                if (!s1InDifferentShift && s2InDifferentShift) return -1;
                
                // Then consider shift preference
                boolean s1PreferredShift = s1.getPreferredShiftId() != null && 
                                          s1.getPreferredShiftId().equals(shift.originalShift.getShiftId());
                boolean s2PreferredShift = s2.getPreferredShiftId() != null && 
                                          s2.getPreferredShiftId().equals(shift.originalShift.getShiftId());
                
                if (s1PreferredShift && !s2PreferredShift) return -1;
                if (!s1PreferredShift && s2PreferredShift) return 1;
                
                // Finally sort by current workload (least busy first)
                Duration s1Workload = staffAssignments.get(s1.getStaffId()).stream()
                    .map(TaskAssignmentDTO::getDuration)
                    .reduce(Duration.ZERO, Duration::plus);
                Duration s2Workload = staffAssignments.get(s2.getStaffId()).stream()
                    .map(TaskAssignmentDTO::getDuration)
                    .reduce(Duration.ZERO, Duration::plus);
                
                return s1Workload.compareTo(s2Workload);
            });
            
            // Try each staff member
            for (HousekeepingStaff staff : sortedStaff) {
                // Skip if staff is already assigned to a different shift
                if (staffToShiftMap.containsKey(staff.getStaffId()) && 
                    !staffToShiftMap.get(staff.getStaffId()).equals(shift.originalShift.getShiftId())) {
                    continue;
                }
                
                // Check if staff can take this task (find a suitable time slot)
                List<TaskAssignmentDTO> currentAssignments = staffAssignments.get(staff.getStaffId());
                
                // Convert current assignments to TimeRange objects for searching
                List<TimeRange> busyTimeRanges = currentAssignments.stream()
                    .map(task -> new TimeRange(task.getStartTime(), 
                                             task.getStartTime().plus(task.getDuration())))
                    .collect(Collectors.toList());
            
                // Find earliest possible start time
                LocalTime earliestStart = findEarliestStartTime(windowStart, windowEnd, 
                                                              taskDuration, busyTimeRanges);
                
                if (earliestStart != null) {
                    // Create the task assignment
                    TaskAssignmentDTO assignment = TaskAssignmentDTO.builder()
                        .externalRoomId(currentTask.getExternalRoomId())
                        .roomNumber(currentTask.getRoomNumber())
                        .staffId(staff.getStaffId())
                        .staffName(staff.getStaffName())
                        .startTime(earliestStart)
                        .taskTypeName(currentTask.getTaskTypeName())
                        .dbTaskTypeName(currentTask.getDbTaskTypeName())
                        .duration(taskDuration)
                        .date(date)
                        // Populate original window for potential balancing
                        .originalWindowStart(currentTask.getWindowStart()) 
                        .originalWindowEnd(taskMaxEndUTC) // Use the potentially constrained end time
                        .build();
                    
                    // Temporarily make this assignment
                    currentAssignments.add(assignment);
                    staffToShiftMap.putIfAbsent(staff.getStaffId(), shift.originalShift.getShiftId());
                    assignedTasks.add(assignment);
                    
                    // Log the attempt
                    log.debug("Trying assignment: Room {} ({}) to Staff {} at {} UTC in Shift {}",
                              currentTask.getExternalRoomId(), currentTask.getTaskTypeName(),
                              staff.getStaffId(), earliestStart, shift.originalShift.getShiftName());
                    
                    // Recursively try to assign the next task
                    if (backtrackAssign(tasks, taskIndex + 1, allStaff, shiftsUTC, checkOutTimeUTC, 
                                      checkInTimeUTC, staffAssignments, staffToShiftMap, assignedTasks, date)) {
                        return true; // Success!
                    }
                    
                    // If we get here, this assignment didn't work out
                    log.debug("Backtracking from assignment for Room {} ({})",
                             currentTask.getExternalRoomId(), currentTask.getTaskTypeName());
                    
                    // Undo the assignment
                    currentAssignments.remove(assignment);
                    assignedTasks.remove(assignment);
                    
                    // If this was the only task for this staff, remove the shift assignment
                    if (currentAssignments.isEmpty()) {
                        staffToShiftMap.remove(staff.getStaffId());
                    }
                }
            }
        }
        
        // If we get here, we couldn't assign this task to anyone
        log.warn("UNASSIGNED Task: Room {} ({}), Window: {} - {} UTC. No suitable staff found.", 
               currentTask.getExternalRoomId(), currentTask.getTaskTypeName(), 
               taskMinStartUTC, taskMaxEndUTC);
        
        // Try to assign the next task even though this one failed
        return backtrackAssign(tasks, taskIndex + 1, allStaff, shiftsUTC, checkOutTimeUTC, 
                             checkInTimeUTC, staffAssignments, staffToShiftMap, assignedTasks, date);
    }
    
    /**
     * Find the earliest possible start time for a task given busy time ranges
     */
    private LocalTime findEarliestStartTime(
            LocalTime windowStart,
            LocalTime windowEnd,
            Duration taskDuration,
            List<TimeRange> busyTimeRanges) {
        
        // If no busy ranges, task can start at window start
        if (busyTimeRanges.isEmpty()) {
            return windowStart;
        }
        
        // Sort busy ranges by start time
        List<TimeRange> sortedBusy = new ArrayList<>(busyTimeRanges);
        Collections.sort(sortedBusy);
        
        // Check if task can start at window start before first busy period
        if (sortedBusy.isEmpty() || windowStart.plus(taskDuration).isBefore(sortedBusy.get(0).start()) || 
            windowStart.plus(taskDuration).equals(sortedBusy.get(0).start())) {
            return windowStart;
        }
        
        // Check for gaps between busy periods
        for (int i = 0; i < sortedBusy.size() - 1; i++) {
            LocalTime gapStart = sortedBusy.get(i).end();
            LocalTime gapEnd = sortedBusy.get(i + 1).start();
            
            // If gap start is before window start, use window start
            if (gapStart.isBefore(windowStart)) {
                gapStart = windowStart;
            }
            
            // If gap end is after window end, use window end
            if (gapEnd.isAfter(windowEnd)) {
                gapEnd = windowEnd;
            }
            
            // Check if task fits in this gap
            if (!gapStart.isAfter(gapEnd) && !gapStart.plus(taskDuration).isAfter(gapEnd)) {
                return gapStart;
            }
        }
        
        // Check if task can start after the last busy period
        if (!sortedBusy.isEmpty()) {
            LocalTime afterLastBusy = sortedBusy.get(sortedBusy.size() - 1).end();
            if (afterLastBusy.isBefore(windowEnd) && 
                !afterLastBusy.plus(taskDuration).isAfter(windowEnd)) {
                return afterLastBusy;
            }
        }
        
        return null; // No suitable time found
    }

    /** Helper method to get CleanTaskType from cache, handling missing types gracefully. */
    private CleanTaskType getTaskType(TaskGenerationDTO task) {
        String dbTaskTypeName = task.getDbTaskTypeName(); // Use DB name for cache lookup
        if (dbTaskTypeName == null || dbTaskTypeName.isBlank()) {
             log.error("Task for room {} has null or blank dbTaskTypeName ('{}'). Cannot determine duration.", task.getExternalRoomId(), task.getTaskTypeName());
             return null; // Cannot proceed without a type name
        }

        if (taskTypeCache.containsKey(dbTaskTypeName)) {
            return taskTypeCache.get(dbTaskTypeName);
        } else {
            // Attempt to fetch from DB if missed during startup (optional, adds DB hit)
            // Optional<CleanTaskType> fromDb = taskTypeRepository.findByTypeName(dbTaskTypeName);
            // if(fromDb.isPresent()) {
            //     taskTypeCache.put(dbTaskTypeName, fromDb.get()); // Update cache
            //     return fromDb.get();
            // }

            log.warn("Task type '{}' (DB name: '{}') not found in cache. Using default 30 min duration.",
                     task.getTaskTypeName(), dbTaskTypeName);
            // Return a default object, DO NOT CACHE IT as it's not a real type from DB
            return CleanTaskType.builder()
                    .typeName(dbTaskTypeName) // Keep original DB name if possible
                    .requiredTime(Duration.ofMinutes(30))
                    .build();
        }
    }

    /** Converts LocalTime from UTC to Property Timezone (IST) on a specific date */
    private LocalTime convertFromUTC(LocalTime timeUTC, LocalDate date) {
        if (timeUTC == null || date == null) {
            log.error("Cannot convert null time/date from UTC.");
            return null; // Or throw exception
        }
        try {
            ZonedDateTime zonedDateTimeUTC = ZonedDateTime.of(date, timeUTC, SYSTEM_TIMEZONE);
            // Use withZoneSameInstant to get the equivalent time point in property timezone
            ZonedDateTime zonedDateTimeIST = zonedDateTimeUTC.withZoneSameInstant(PROPERTY_TIMEZONE);
            return zonedDateTimeIST.toLocalTime();
        } catch (Exception e) {
            log.error("Error converting time {} UTC on {} to IST: {}", timeUTC, date, e.getMessage(), e);
            return timeUTC; // Fallback - potentially problematic, depends on error handling strategy
        }
    }

    /** Converts LocalTime from Property Timezone (IST) to System Timezone (UTC) on a specific date */
    private LocalTime convertToUTC(LocalTime timeIST, LocalDate date) {
        if (timeIST == null || date == null) {
            log.error("Cannot convert null time/date to UTC.");
            return null; // Or throw exception
        }
        try {
            ZonedDateTime zonedDateTimeIST = ZonedDateTime.of(date, timeIST, PROPERTY_TIMEZONE);
            // Use withZoneSameInstant to get the equivalent time point in UTC
            ZonedDateTime zonedDateTimeUTC = zonedDateTimeIST.withZoneSameInstant(SYSTEM_TIMEZONE);
            return zonedDateTimeUTC.toLocalTime();
        } catch (Exception e) {
            log.error("Error converting time {} IST on {} to UTC: {}", timeIST, date, e.getMessage(), e);
            return timeIST; // Fallback - potentially problematic, depends on error handling strategy
        }
    }


    // --- calculateStaffShortfall remains largely the same ---
    @Override
    public int calculateStaffShortfall(Integer propertyId, LocalDate date, List<TaskGenerationDTO> unassignedTasks) {
        if (unassignedTasks.isEmpty()) {
            return 0;
        }
        
         // Calculate total unassigned minutes using cached/default task types
         long totalUnassignedMinutes = 0;
         List<String> problematicTasks = new ArrayList<>(); // Keep track of tasks with issues
        
        for (TaskGenerationDTO task : unassignedTasks) {
             CleanTaskType taskType = getTaskType(task); // Use the robust helper
             if (taskType != null && taskType.getRequiredTime() != null && !taskType.getRequiredTime().isNegative() && !taskType.getRequiredTime().isZero()) {
            totalUnassignedMinutes += taskType.getRequiredTime().toMinutes();
             } else {
                  String taskInfo = String.format("Room %s (%s)", task.getExternalRoomId(), task.getDbTaskTypeName());
                  problematicTasks.add(taskInfo);
                  log.warn("Could not determine valid duration for unassigned task: {}. Skipping for shortfall calculation.", taskInfo);
             }
         }

         if (totalUnassignedMinutes == 0) {
              if (!problematicTasks.isEmpty()) {
                   log.warn("Staff shortfall calculation resulted in 0 minutes, but some tasks had duration issues: {}", problematicTasks);
              }
              return 0;
         }

         // Get average shift duration for the property
        List<Shift> shifts = shiftRepository.findByPropertyId(propertyId);
        if (shifts.isEmpty()) {
             log.warn("No shifts defined for property {}, using default 4 hour shift for shortfall calculation.", propertyId);
             // Use double for division
             return (int) Math.ceil(totalUnassignedMinutes / (4.0 * 60.0));
        }
        
        double averageShiftMinutes = shifts.stream()
                 .mapToLong(shift -> {
                     try {
                         // Ensure times are not null before calculating duration
                         if (shift.getStartTime() != null && shift.getEndTime() != null) {
                             Duration d = Duration.between(shift.getStartTime(), shift.getEndTime());
                              // Handle shifts crossing midnight if necessary, though less common for cleaning shifts
                              if (d.isNegative()) {
                                   d = d.plusDays(1); // Add 24 hours if end time is before start time
                              }
                              return d.toMinutes();
                         } else {
                              log.warn("Shift {} has null start/end time, cannot calculate duration.", shift.getShiftId());
                              return 0L;
                         }
                     } catch (Exception e) {
                          log.warn("Error calculating duration for shift {}: {}", shift.getShiftId(), e.getMessage());
                          return 0L; // Treat as invalid duration
                     }
                 })
                 .filter(d -> d > 0) // Only consider valid, positive durations
                 .average()
                 .orElse(4 * 60); // Default to 4 hours (240 mins) if no valid shifts or calculation fails

          if (averageShiftMinutes <= 0) {
              log.warn("Average shift duration calculation resulted in <= 0, using default 4 hours for shortfall.");
              averageShiftMinutes = 4 * 60.0;
          }

         // Calculate number of additional staff needed (use double division)
         int neededStaff = (int) Math.ceil(totalUnassignedMinutes / averageShiftMinutes);
         log.info("Calculated staff shortfall: {} minutes unassigned / avg {} min/shift = {} staff needed.",
                  totalUnassignedMinutes, String.format("%.2f", averageShiftMinutes), neededStaff);
         return neededStaff;
     }

} // End of class 