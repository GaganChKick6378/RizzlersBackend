package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.dto.StaffAbsenceDTO;
import com.kdu.rizzlers.dto.TaskAssignmentDTO;
import com.kdu.rizzlers.dto.TaskGenerationDTO;
import com.kdu.rizzlers.entity.CleanTask;
import com.kdu.rizzlers.entity.CleanTaskType;
import com.kdu.rizzlers.entity.HousekeepingStaff;
import com.kdu.rizzlers.entity.PropertyPreferences;
import com.kdu.rizzlers.entity.Shift;
import com.kdu.rizzlers.entity.StaffAbsence;
import com.kdu.rizzlers.entity.StaffAbsence.StaffAbsenceId;
import com.kdu.rizzlers.repository.CleanTaskRepository;
import com.kdu.rizzlers.repository.CleanTaskTypeRepository;
import com.kdu.rizzlers.repository.HousekeepingStaffRepository;
import com.kdu.rizzlers.repository.PropertyPreferencesRepository;
import com.kdu.rizzlers.repository.ShiftRepository;
import com.kdu.rizzlers.repository.StaffAbsenceRepository;
import com.kdu.rizzlers.service.EmailService;
import com.kdu.rizzlers.service.HousekeepingService;
import com.kdu.rizzlers.service.TaskSchedulingService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of HousekeepingService for staff and task management
 */
@Service
@Slf4j
public class HousekeepingServiceImpl implements HousekeepingService {

    private final HousekeepingStaffRepository staffRepository;
    private final StaffAbsenceRepository absenceRepository;
    private final CleanTaskRepository taskRepository;
    private final CleanTaskTypeRepository taskTypeRepository;
    private final PropertyPreferencesRepository propertyPreferencesRepository;
    private final ShiftRepository shiftRepository;
    private final TaskSchedulingService taskSchedulingService;
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    
    @Value("${spring.mail.username}")
    private String senderEmail;
    
    @Value("${housekeeping.admin.email}")
    private String adminEmail;
    
    public HousekeepingServiceImpl(
            HousekeepingStaffRepository staffRepository,
            StaffAbsenceRepository absenceRepository,
            CleanTaskRepository taskRepository,
            CleanTaskTypeRepository taskTypeRepository,
            PropertyPreferencesRepository propertyPreferencesRepository,
            ShiftRepository shiftRepository,
            TaskSchedulingService taskSchedulingService,
            JavaMailSender mailSender,
            TemplateEngine templateEngine) {
        this.staffRepository = staffRepository;
        this.absenceRepository = absenceRepository;
        this.taskRepository = taskRepository;
        this.taskTypeRepository = taskTypeRepository;
        this.propertyPreferencesRepository = propertyPreferencesRepository;
        this.shiftRepository = shiftRepository;
        this.taskSchedulingService = taskSchedulingService;
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Override
    @Transactional
    public boolean applyForSickLeave(StaffAbsenceDTO absenceDTO) {
        // Validate that the staff exists
        Optional<HousekeepingStaff> staffOpt = staffRepository.findById(absenceDTO.getStaffId());
        if (staffOpt.isEmpty()) {
            log.error("Staff with ID {} not found", absenceDTO.getStaffId());
            return false;
        }
        
        // Create the absence ID
        StaffAbsenceId absenceId = new StaffAbsenceId(absenceDTO.getStaffId(), absenceDTO.getDate());
        
        // Check if absence already exists
        if (absenceRepository.existsById(absenceId)) {
            log.warn("Absence already exists for staff {} on date {}", absenceDTO.getStaffId(), absenceDTO.getDate());
            return false;
        }
        
        // Create and save the absence
        StaffAbsence absence = StaffAbsence.builder()
                .id(absenceId)
                .build();
        
        absenceRepository.save(absence);
        
        log.info("Sick leave recorded for staff {} on date {}", absenceDTO.getStaffId(), absenceDTO.getDate());
        return true;
    }

    @Override
    public List<StaffAbsence> getStaffAbsences(Integer staffId) {
        return absenceRepository.findByStaffId(staffId);
    }

    @Override
    public List<StaffAbsence> getAbsencesForPropertyAndDate(Integer propertyId, LocalDate date) {
        return absenceRepository.findByPropertyIdAndDate(propertyId, date);
    }

    @Override
    @Transactional
    public List<CleanTask> generateAndAssignTasks(Integer propertyId, LocalDate date) {
        // Generate tasks
        List<TaskGenerationDTO> generatedTasks = taskSchedulingService.generateTasks(propertyId, date);
        
        if (generatedTasks.isEmpty()) {
            log.info("No tasks generated for property {} on date {}", propertyId, date);
            return new ArrayList<>();
        }
        
        // Assign tasks
        List<TaskAssignmentDTO> assignedTasks = taskSchedulingService.assignTasks(propertyId, generatedTasks, date);
        
        // Create a copy of generatedTasks to find unassigned tasks
        List<TaskGenerationDTO> unassignedTasks = new ArrayList<>();
        
        // Only count tasks as unassigned if they weren't actually assigned
        // Compare external room IDs to identify which tasks weren't assigned
        Map<String, Boolean> assignedRoomMap = assignedTasks.stream()
                .collect(Collectors.toMap(
                        TaskAssignmentDTO::getExternalRoomId,
                        task -> true,
                        (existing, replacement) -> true)); // In case of duplicates, keep any
        
        for (TaskGenerationDTO task : generatedTasks) {
            if (!assignedRoomMap.containsKey(task.getExternalRoomId())) {
                unassignedTasks.add(task);
            }
        }
        
        // Calculate staff shortfall only for actually unassigned tasks
        int staffShortfall = taskSchedulingService.calculateStaffShortfall(propertyId, date, unassignedTasks);
        if (staffShortfall > 0) {
            log.warn("Staff shortfall for property {} on date {}: {} additional staff needed", 
                    propertyId, date, staffShortfall);
            
            // Calculate total unassigned minutes for detailed notification
            int totalUnassignedMinutes = 0;
            for (TaskGenerationDTO task : unassignedTasks) {
                Optional<CleanTaskType> taskTypeOpt = taskTypeRepository.findByTypeName(task.getDbTaskTypeName());
                if (taskTypeOpt.isPresent()) {
                    totalUnassignedMinutes += taskTypeOpt.get().getRequiredTime().toMinutes();
                } else {
                    // Default to 30 minutes if task type not found
                    totalUnassignedMinutes += 30;
                }
            }
            
            // Calculate average shift duration
            List<Shift> shifts = shiftRepository.findByPropertyId(propertyId);
            double averageShiftMinutes = shifts.stream()
                    .mapToLong(shift -> Duration.between(shift.getStartTime(), shift.getEndTime()).toMinutes())
                    .average()
                    .orElse(8 * 60); // Default to 8 hours
            
            // Send notification email with detailed information
            sendStaffShortfallNotification(
                    propertyId, 
                    date, 
                    staffShortfall, 
                    unassignedTasks, 
                    totalUnassignedMinutes, 
                    averageShiftMinutes);
        }
        
        // Save assigned tasks to database
        List<CleanTask> savedTasks = new ArrayList<>();
        
        for (TaskAssignmentDTO assignedTask : assignedTasks) {
            // Find task type ID
            Optional<CleanTaskType> taskTypeOpt = taskTypeRepository.findByTypeName(assignedTask.getDbTaskTypeName());
            if (taskTypeOpt.isEmpty()) {
                log.warn("Task type {} not found, skipping task", assignedTask.getDbTaskTypeName());
                continue;
            }
            
            CleanTask task = CleanTask.builder()
                    .propertyId(propertyId)
                    .staffId(assignedTask.getStaffId())
                    .startTime(assignedTask.getStartTime())
                    .taskTypeId(taskTypeOpt.get().getTaskTypeId())
                    .date(assignedTask.getDate())
                    .externalRoomId(assignedTask.getExternalRoomId())
                    .remark(assignedTask.getTaskTypeName())
                    .build();
            
            savedTasks.add(taskRepository.save(task));
        }
        
        log.info("Saved {} tasks for property {} on date {}", savedTasks.size(), propertyId, date);
        return savedTasks;
    }

    @Override
    public List<TaskAssignmentDTO> getStaffTasksForDate(Integer staffId, LocalDate date) {
        List<CleanTask> tasks = taskRepository.findByStaffIdAndDate(staffId, date);
        return mapTasksToAssignmentDTOs(tasks);
    }

    @Override
    public List<TaskAssignmentDTO> getPropertyTasksForDate(Integer propertyId, LocalDate date) {
        List<CleanTask> tasks = taskRepository.findByPropertyIdAndDate(propertyId, date);
        return mapTasksToAssignmentDTOs(tasks);
    }

    @Override
    @Transactional
    public CleanTaskType createTaskType(CleanTaskType taskType) {
        return taskTypeRepository.save(taskType);
    }

    @Override
    @Transactional
    public HousekeepingStaff createStaffMember(HousekeepingStaff staff) {
        return staffRepository.save(staff);
    }

    @Override
    @Transactional
    public Shift createShift(Shift shift) {
        return shiftRepository.save(shift);
    }

    @Override
    @Transactional
    public PropertyPreferences savePropertyPreferences(PropertyPreferences preferences) {
        return propertyPreferencesRepository.save(preferences);
    }

    @Override
    public List<HousekeepingStaff> getStaffByProperty(Integer propertyId) {
        return staffRepository.findByPropertyId(propertyId);
    }

    @Override
    public List<Shift> getShiftsByProperty(Integer propertyId) {
        return shiftRepository.findByPropertyId(propertyId);
    }

    @Override
    public List<CleanTaskType> getAllTaskTypes() {
        return taskTypeRepository.findAll();
    }
    
    /**
     * Scheduled method that runs daily to generate and assign tasks based on room bookings.
     * This creates cleaning tasks for all properties and automatically assigns them to available staff.
     */
    @Scheduled(cron = "0 54 16 * * *") // Run at 6:00 AM every day
    @Transactional
    public void generateAndAssignDailyTasks() {
        log.info("Starting daily task generation and assignment for all properties...");
        
        // Get all properties
        List<PropertyPreferences> properties = propertyPreferencesRepository.findAll();
        LocalDate today = LocalDate.now();
        
        for (PropertyPreferences property : properties) {
            try {
                // Generate tasks for today only
                log.info("Generating tasks for property {} for today ({})", property.getPropertyId(), today);
                List<CleanTask> tasksToday = generateAndAssignTasks(property.getPropertyId(), today);
                log.info("Generated and assigned {} tasks for property {} for today", tasksToday.size(), property.getPropertyId());
            } catch (Exception e) {
                log.error("Error generating tasks for property {}: {}", property.getPropertyId(), e.getMessage(), e);
            }
        }
        
        log.info("Daily task generation complete");
    }
    
    /**
     * Scheduled method that runs in the afternoon to update task assignments 
     * based on any changes in room status during the day.
     */
    @Scheduled(cron = "0 0 14 * * *") // Run at 2:00 PM every day
    @Transactional
    public void updateDailyTaskAssignments() {
        log.info("Starting afternoon update of task assignments for all properties...");
        
        // Get all properties
        List<PropertyPreferences> properties = propertyPreferencesRepository.findAll();
        LocalDate today = LocalDate.now();
        
        for (PropertyPreferences property : properties) {
            try {
                // Regenerate and update tasks for today to handle any changes
                log.info("Updating tasks for property {} for today ({})", property.getPropertyId(), today);
                
                // Remove existing tasks that haven't been completed yet
                // This logic would need to be implemented based on your task completion tracking
                
                // Generate new tasks
                List<CleanTask> updatedTasks = generateAndAssignTasks(property.getPropertyId(), today);
                log.info("Updated {} tasks for property {}", updatedTasks.size(), property.getPropertyId());
            } catch (Exception e) {
                log.error("Error updating tasks for property {}: {}", property.getPropertyId(), e.getMessage(), e);
            }
        }
        
        log.info("Afternoon task update complete");
    }
    
    /**
     * Map database task entities to DTOs for the API
     */
    private List<TaskAssignmentDTO> mapTasksToAssignmentDTOs(List<CleanTask> tasks) {
        return tasks.stream().map(task -> {
            // Get staff name
            String staffName = staffRepository.findById(task.getStaffId())
                    .map(HousekeepingStaff::getStaffName)
                    .orElse("Unknown");
            
            // Get task type name and duration
            Optional<CleanTaskType> taskTypeOpt = taskTypeRepository.findById(task.getTaskTypeId());
            String taskTypeName = taskTypeOpt.map(CleanTaskType::getTypeName).orElse("Unknown");
            
            return TaskAssignmentDTO.builder()
                    .taskId(task.getTaskId())
                    .externalRoomId(task.getExternalRoomId())
                    .staffId(task.getStaffId())
                    .staffName(staffName)
                    .startTime(task.getStartTime())
                    .taskTypeName(taskTypeName)
                    .duration(taskTypeOpt.map(CleanTaskType::getRequiredTime).orElse(null))
                    .date(task.getDate())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public boolean sendStaffShortfallNotification(
            Integer propertyId,
            LocalDate date,
            int staffShortfall,
            List<TaskGenerationDTO> unassignedTasks,
            int totalUnassignedMinutes,
            double averageShiftMinutes) {
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            // Set email properties
            helper.setFrom(senderEmail);
            helper.setTo(adminEmail);
            helper.setSubject("ALERT: Staff Shortfall for Property " + propertyId + " on " + date);
            
            // Prepare the context for the template
            Context context = new Context();
            context.setVariable("propertyId", propertyId);
            context.setVariable("date", date);
            context.setVariable("staffShortfall", staffShortfall);
            context.setVariable("unassignedTasks", unassignedTasks);
            context.setVariable("totalUnassignedMinutes", totalUnassignedMinutes);
            context.setVariable("averageShiftMinutes", Math.round(averageShiftMinutes));
            
            // Format times for better readability in the template
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            unassignedTasks.forEach(task -> {
                task.setWindowStart(LocalTime.parse(task.getWindowStart().format(timeFormatter)));
                task.setWindowEnd(LocalTime.parse(task.getWindowEnd().format(timeFormatter)));
            });
            
            // Process the template
            String emailContent = templateEngine.process("staff-shortfall-alert", context);
            helper.setText(emailContent, true);
            
            // Send the email
            mailSender.send(message);
            log.info("Staff shortfall notification sent to admin for property {} on date {}", propertyId, date);
            return true;
        } catch (MessagingException e) {
            log.error("Failed to send staff shortfall notification: {}", e.getMessage(), e);
            return false;
        } catch (Exception e) {
            log.error("Unexpected error sending staff shortfall notification: {}", e.getMessage(), e);
            return false;
        }
    }
}