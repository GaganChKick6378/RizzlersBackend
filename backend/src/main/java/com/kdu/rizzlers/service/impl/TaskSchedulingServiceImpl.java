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
        List<TaskAssignmentDTO> assignedTasks = new ArrayList<>();
        if (tasks.isEmpty()) {
            log.info("No tasks generated for property {} on date {}, assignment skipped.", propertyId, date);
            return assignedTasks;
        }
        
        List<Shift> shiftsDB = shiftRepository.findByPropertyId(propertyId);
        if (shiftsDB.isEmpty()) {
            log.error("No shifts defined for property {}. Cannot assign tasks.", propertyId);
            return assignedTasks;
        }
        
        // Convert shifts to UTC for internal logic and sort chronologically
        List<ShiftUTC> shiftsUTC = shiftsDB.stream()
            .map(shift -> {
                LocalTime startUTC = convertToUTC(shift.getStartTime(), date);
                LocalTime endUTC = convertToUTC(shift.getEndTime(), date);
                // Log the conversion for debugging
                log.debug("Shift {} converted: {} IST -> {} UTC, {} IST -> {} UTC", 
                    shift.getShiftName(), shift.getStartTime(), startUTC, shift.getEndTime(), endUTC);
                return new ShiftUTC(shift, startUTC, endUTC);
            })
            .sorted(Comparator.comparing(ShiftUTC::startTimeUTC))
                .collect(Collectors.toList());
        
        log.info("Processing task assignment with {} shifts (converted to UTC):", shiftsUTC.size());
        shiftsUTC.forEach(s -> log.info("  - Shift: {} (ID:{}) {} - {} UTC", s.originalShift.getShiftName(), s.originalShift.getShiftId(), s.startTimeUTC, s.endTimeUTC));

        // --- Staff Availability Setup ---
        Map<Integer, List<TimeRange>> staffScheduleUTC = new HashMap<>(); // Staff ID -> Sorted List of occupied UTC TimeRanges
        Map<Integer, List<Integer>> staffByShift = new HashMap<>(); // Shift ID -> List of Staff IDs working that shift

        // Fetch staff for *all* shifts first to initialize the schedule map
        List<HousekeepingStaff> allStaff = staffRepository.findAvailableStaffByPropertyId(propertyId, date);
        if (allStaff.isEmpty()) {
             log.warn("No staff available for property {} on date {}. Cannot assign tasks.", propertyId, date);
             return assignedTasks;
        }
        
        // Initialize staff schedules
        allStaff.forEach(staff -> {
            staffScheduleUTC.put(staff.getStaffId(), new ArrayList<>());
            // No need to manage staff.getShifts() here anymore
        });

        // Populate staff per shift - CHANGE: Consider ALL available staff for EACH shift initially.
        // The time constraints within the loop will handle actual availability.
        List<Integer> allAvailableStaffIds = allStaff.stream().map(HousekeepingStaff::getStaffId).collect(Collectors.toList());
        for (ShiftUTC shiftUTC : shiftsUTC) {
            // Assign ALL available staff IDs to this shift's potential pool.
            staffByShift.put(shiftUTC.originalShift.getShiftId(), new ArrayList<>(allAvailableStaffIds)); 
            log.info("Shift {} (ID:{}) considering {} potential staff: {}", 
                     shiftUTC.originalShift.getShiftName(), shiftUTC.originalShift.getShiftId(), 
                     allAvailableStaffIds.size(), allAvailableStaffIds);
        }


        // --- Task Prioritization ---
        // Sort tasks by Priority (desc), then Window Start (asc)
        tasks.sort(Comparator.comparing(TaskGenerationDTO::getPriority).reversed()
                .thenComparing(TaskGenerationDTO::getWindowStart));

        log.info("Attempting to assign {} tasks, sorted by priority (times in UTC):", tasks.size());

        // --- Greedy Assignment Loop ---
        Set<String> assignedRoomIds = new HashSet<>(); // Track assigned rooms

        for (TaskGenerationDTO task : tasks) {
            if (assignedRoomIds.contains(task.getExternalRoomId())) {
                log.debug("Skipping task for room {} (already assigned).", task.getExternalRoomId());
                continue; // Skip if room already handled by a higher priority task
            }

            log.debug("Attempting assignment for Task: Room {}, Type: {}, Prio: {}, Window: {} - {}",
                    task.getExternalRoomId(), task.getTaskTypeName(), task.getPriority(), task.getWindowStart(), task.getWindowEnd());

            Optional<AssignmentOption> bestOptionForThisTask = Optional.empty();
            CleanTaskType taskType = getTaskType(task); // Use helper to handle missing types
            if (taskType == null || taskType.getRequiredTime() == null) {
                 log.error("Task type definition or duration missing for '{}' (DB: '{}'). Skipping task for room {}.",
                           task.getTaskTypeName(), task.getDbTaskTypeName(), task.getExternalRoomId());
                continue;
            }
            Duration taskDuration = taskType.getRequiredTime();

            // Determine task-specific constraints (min start, max end) in UTC
            LocalTime taskMinStartUTC = task.getWindowStart();
            LocalTime taskMaxEndUTC = task.getWindowEnd();

            // Apply checkout/check-in time constraints
             Optional<PropertyPreferences> prefsOpt = propertyPreferencesRepository.findByPropertyId(propertyId);
             if (prefsOpt.isPresent()) {
                 PropertyPreferences prefs = prefsOpt.get();
                 LocalTime checkOutTimeUTC = convertToUTC(prefs.getCheckOutTime(), date);
                 LocalTime checkInTimeUTC = convertToUTC(prefs.getCheckInTime(), date);

                 if ("IMMEDIATE_CHECKOUT_CLEANING".equals(task.getTaskTypeName()) || "DELAYED_CHECKOUT_CLEANING".equals(task.getTaskTypeName())) {
                      if (taskMinStartUTC.isBefore(checkOutTimeUTC)) {
                          taskMinStartUTC = checkOutTimeUTC; // Must start >= checkout time
                          log.trace("  Constraint: {} Min start enforced: >= {} UTC (Checkout Time)", task.getTaskTypeName(), taskMinStartUTC);
                      }
                 }
                  if ("EARLY_DAILY_CLEANUP".equals(task.getTaskTypeName())) {
                      if (taskMaxEndUTC.isAfter(checkInTimeUTC)) {
                          taskMaxEndUTC = checkInTimeUTC; // Must end <= check-in time
                           log.trace("  Constraint: {} Max end enforced: <= {} UTC (Check-in Time)", task.getTaskTypeName(), taskMaxEndUTC);
                      }
                  }
             } else {
                  log.error("Cannot retrieve property preferences to enforce checkout/check-in time constraints!");
                  // Decide how to proceed - maybe skip tasks requiring these constraints?
                  // For now, we'll proceed without enforcing these specific constraints if prefs are missing.
             }


            // Iterate through shifts chronologically
            for (ShiftUTC shiftUTC : shiftsUTC) {
                List<Integer> potentialStaffIds = staffByShift.getOrDefault(shiftUTC.originalShift.getShiftId(), Collections.emptyList());
                
                // Debug log to verify available staff
                log.debug("Available staff for shift {}: {}", shiftUTC.originalShift.getShiftName(), potentialStaffIds);

                // Skip to next shift if no staff is available
                if (potentialStaffIds.isEmpty()) {
                    log.debug("No staff available for shift {}. Skipping to next shift.", shiftUTC.originalShift.getShiftName());
                    continue;
                }

                // Iterate through staff available in this shift
                for (Integer staffId : potentialStaffIds) {
                    List<TimeRange> schedule = staffScheduleUTC.get(staffId); // Get staff's current schedule

                    // Find the earliest possible start time for this staff+task+shift combo
                    LocalTime earliestPossibleStart = shiftUTC.startTimeUTC; // Cannot start before shift starts

                    // Consider staff's last task end time
                    if (!schedule.isEmpty()) {
                        // Find the latest end time among all tasks assigned to this staff so far
                        LocalTime latestEndTime = schedule.stream().map(TimeRange::end).max(LocalTime::compareTo).orElse(shiftUTC.startTimeUTC);
                        if (earliestPossibleStart.isBefore(latestEndTime)) {
                            earliestPossibleStart = latestEndTime;
                        }
                    }

                    // Apply the task's calculated minimum start time constraint
                    if (earliestPossibleStart.isBefore(taskMinStartUTC)) {
                        earliestPossibleStart = taskMinStartUTC;
                    }

                    // Calculate potential end time
                    LocalTime potentialEndTime = earliestPossibleStart.plus(taskDuration);

                    // --- Check Validity ---
                    // Add debug logging to trace the validation checks
                    log.trace("Validating slot for Staff {}: Task time {}-{} in Shift {}-{} for task window {}-{}", 
                        staffId, earliestPossibleStart, potentialEndTime, 
                        shiftUTC.startTimeUTC, shiftUTC.endTimeUTC,
                        task.getWindowStart(), taskMaxEndUTC);

                    // 1. Does the task fit within the SHIFT window?
                    boolean fitsInShift = !earliestPossibleStart.isBefore(shiftUTC.startTimeUTC) && !potentialEndTime.isAfter(shiftUTC.endTimeUTC);
                    if (!fitsInShift) {
                        log.trace("  Staff {}: Task time {}-{} doesn't fit in Shift {}-{}.", 
                            staffId, earliestPossibleStart, potentialEndTime, shiftUTC.startTimeUTC, shiftUTC.endTimeUTC);
                        continue; // Cannot fit in this shift
                    }

                    // 2. Does the task fit within the TASK's constrained window?
                    boolean fitsInTaskWindow = !earliestPossibleStart.isBefore(task.getWindowStart()) && !potentialEndTime.isAfter(taskMaxEndUTC);
                    if (!fitsInTaskWindow) {
                        // Note: We compare against taskMaxEndUTC which includes check-in constraint if applicable
                        log.trace("  Staff {}: Task time {}-{} doesn't fit in Task Window {}-{}.", 
                            staffId, earliestPossibleStart, potentialEndTime, task.getWindowStart(), taskMaxEndUTC);
                        continue; // Cannot fit in task window
                    }

                    // 3. Does this potential slot overlap with the staff's EXISTING schedule?
                    boolean overlaps = false;
                    for (TimeRange existingSlot : schedule) {
                        // Overlap definition: newStart < existingEnd AND newEnd > existingStart
                        if (earliestPossibleStart.isBefore(existingSlot.end()) && potentialEndTime.isAfter(existingSlot.start())) {
                            overlaps = true;
                            log.trace("  Staff {}: Potential slot {}-{} overlaps with existing {}-{}.", 
                                staffId, earliestPossibleStart, potentialEndTime, existingSlot.start(), existingSlot.end());
                            break;
                        }
                    }
                    if (overlaps) {
                        continue; // Found overlap, try next staff/shift
                    }

                    // --- Found a valid slot for this staff ---
                    log.debug("  Valid slot found for Staff {}: {} - {} UTC in Shift {}", 
                        staffId, earliestPossibleStart, potentialEndTime, shiftUTC.originalShift.getShiftName());

                    // Is this option better (earlier start) than the current best found *for this task*?
                    if (bestOptionForThisTask.isEmpty() || earliestPossibleStart.isBefore(bestOptionForThisTask.get().startTimeUTC)) {
                        bestOptionForThisTask = Optional.of(new AssignmentOption(staffId, shiftUTC, earliestPossibleStart, potentialEndTime));
                        log.debug("    -> New best option for this task.");
                    }

                } // End staff loop

                // Optimization: If the best option found so far starts within the current shift,
                // we don't need to check later shifts because we want the earliest possible assignment.
                if (bestOptionForThisTask.isPresent() && !bestOptionForThisTask.get().startTimeUTC.isAfter(shiftUTC.endTimeUTC)) {
                    log.debug("  Best option found starting within current shift ({}), stopping shift search for this task.", 
                        shiftUTC.originalShift.getShiftName());
                    break; // Move to assignment phase for this task
                }

            } // End shift loop

            // --- Assign the task if a best option was found ---
            if (bestOptionForThisTask.isPresent()) {
                AssignmentOption assignment = bestOptionForThisTask.get();
                
                // Double-check that the assignment still fits within shift boundaries
                boolean stillValid = !assignment.startTimeUTC.isBefore(assignment.shiftUTC.startTimeUTC) && 
                                    !assignment.endTimeUTC.isAfter(assignment.shiftUTC.endTimeUTC);
                                    
                if (!stillValid) {
                    log.warn("Assignment validation failed! Task for room {} would occur outside of shift hours.", task.getExternalRoomId());
                    continue; // Skip this assignment as it's no longer valid
                }
                
                // Find staff details from the list fetched earlier
                HousekeepingStaff assignedStaffDetails = allStaff.stream()
                    .filter(s -> s.getStaffId().equals(assignment.staffId))
                    .findFirst()
                    .orElse(null);

                if (assignedStaffDetails != null) {
                    TaskAssignmentDTO dto = TaskAssignmentDTO.builder()
                            .externalRoomId(task.getExternalRoomId())
                            .roomNumber(task.getRoomNumber())
                            .staffId(assignedStaffDetails.getStaffId())
                            .staffName(assignedStaffDetails.getStaffName())
                            .startTime(assignment.startTimeUTC) // Store assignment time in UTC
                            .taskTypeName(task.getTaskTypeName())
                            .dbTaskTypeName(task.getDbTaskTypeName())
                            .duration(taskDuration)
                            .date(date)
                            .build();
                    assignedTasks.add(dto);

                    // Update staff schedule by adding the new TimeRange
                    List<TimeRange> schedule = staffScheduleUTC.get(assignment.staffId);
                    schedule.add(new TimeRange(assignment.startTimeUTC, assignment.endTimeUTC));
                    Collections.sort(schedule); // Keep schedule sorted

                    assignedRoomIds.add(task.getExternalRoomId()); // Mark room as assigned

                    // Log the assignment in a clearer format showing both UTC and property timezone
                    LocalTime startTimeIST = convertFromUTC(assignment.startTimeUTC, date);
                    LocalTime endTimeIST = convertFromUTC(assignment.endTimeUTC, date);
                    
                    log.info("ASSIGNED Task: Room {} ({}) to Staff {} ({}) at {} UTC / {} IST (Duration: {} min) in Shift {}",
                            task.getExternalRoomId(), task.getTaskTypeName(),
                            assignment.staffId, assignedStaffDetails.getStaffName(),
                            assignment.startTimeUTC, startTimeIST, taskDuration.toMinutes(),
                            assignment.shiftUTC.originalShift.getShiftName());
                } else {
                    log.error("Consistency Error: Could not find staff details for ID {} during assignment despite being available earlier.", assignment.staffId);
                    // Consider how to handle this - skip assignment? Log and continue?
                }
            } else {
                // No suitable slot found across all shifts/staff for this task
                log.warn("UNASSIGNED Task: Room {} ({}), Prio: {}, Window: {} - {} UTC. No suitable slot/staff found.",
                        task.getExternalRoomId(), task.getTaskTypeName(), task.getPriority(), task.getWindowStart(), taskMaxEndUTC); // Use constrained end

                 // Log specific reasons if possible
                 if (taskDuration.isZero() || taskDuration.isNegative()) {
                     log.warn("  -> Reason: Task duration is zero or negative.");
                 } else if (taskMaxEndUTC.isBefore(taskMinStartUTC) || taskMaxEndUTC.isBefore(task.getWindowStart())) {
                     log.warn("  -> Reason: Calculated maximum end time ({}) is before minimum start time ({}). Invalid task window/constraints.", taskMaxEndUTC, taskMinStartUTC);
                 } else if (!shiftsUTC.isEmpty()) {
                    if (taskMaxEndUTC.isBefore(shiftsUTC.get(0).startTimeUTC)) {
                        log.warn("  -> Reason: Task window (ends {}) is entirely before the first shift starts ({} UTC). Shift timing conflict.", taskMaxEndUTC, shiftsUTC.get(0).startTimeUTC);
                    } else if (taskMinStartUTC.isAfter(shiftsUTC.get(shiftsUTC.size()-1).endTimeUTC)) {
                        log.warn("  -> Reason: Required task start time (>= {}) is after the last shift ends ({} UTC). Shift timing conflict.", taskMinStartUTC, shiftsUTC.get(shiftsUTC.size()-1).endTimeUTC);
                    } else {
                        // Generic reason if no specific conflict identified
                        log.warn("  -> Reason: Could not find an available staff member with a free time slot within the task window and their assigned shift(s). Check staff availability and task durations.");
                    }
                 } else {
                     log.warn("  -> Reason: No shifts available for assignment.");
                 }
            }

        } // End task loop

        log.info("Assignment process complete for property {}. Assigned {} / {} tasks.", propertyId, assignedTasks.size(), tasks.size());
        Set<String> allRoomIds = tasks.stream().map(TaskGenerationDTO::getExternalRoomId).collect(Collectors.toSet());
        Set<String> unassignedRoomIds = new HashSet<>(allRoomIds);
        unassignedRoomIds.removeAll(assignedRoomIds);
        if (!unassignedRoomIds.isEmpty()) {
            log.warn("Unassigned rooms ({}): {}", unassignedRoomIds.size(), unassignedRoomIds);
        }

        return assignedTasks;
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