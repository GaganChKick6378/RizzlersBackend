package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.dto.in.StudentRequest;
import com.kdu.rizzlers.dto.out.StudentResponse;
import com.kdu.rizzlers.entity.Student;
import com.kdu.rizzlers.exception.ResourceNotFoundException;
import com.kdu.rizzlers.repository.StudentRepository;
import com.kdu.rizzlers.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of the StudentService interface.
 */
@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {
    private static final Logger logger = LoggerFactory.getLogger(StudentServiceImpl.class);

    private final StudentRepository studentRepository;

    @Override
    @Transactional
    public StudentResponse createStudent(StudentRequest request) {
        logger.info("Creating new student with enrollment number: {}", request.getEnrollmentNumber());
        
        // Check if student with same enrollment number already exists
        Optional<Student> existingStudentByEnrollment = studentRepository.findByEnrollmentNumberAndIsActive(
                request.getEnrollmentNumber(), true);
        if (existingStudentByEnrollment.isPresent()) {
            logger.error("Student with enrollment number {} already exists", request.getEnrollmentNumber());
            throw new IllegalArgumentException("Student with this enrollment number already exists");
        }

        // Check if student with same email already exists
        Optional<Student> existingStudentByEmail = studentRepository.findByEmailAndIsActive(
                request.getEmail(), true);
        if (existingStudentByEmail.isPresent()) {
            logger.error("Student with email {} already exists", request.getEmail());
            throw new IllegalArgumentException("Student with this email already exists");
        }

        // Create and save new student
        Student student = mapRequestToEntity(request);
        Student savedStudent = studentRepository.save(student);
        logger.info("Successfully created student with ID: {}", savedStudent.getId());
        
        return mapEntityToResponse(savedStudent);
    }

    @Override
    public StudentResponse getStudentById(Long id) {
        logger.info("Getting student with ID: {}", id);
        Student student = findStudentById(id);
        return mapEntityToResponse(student);
    }

    @Override
    public StudentResponse getStudentByEnrollmentNumber(String enrollmentNumber) {
        logger.info("Getting student with enrollment number: {}", enrollmentNumber);
        Student student = studentRepository.findByEnrollmentNumberAndIsActive(enrollmentNumber, true)
                .orElseThrow(() -> {
                    logger.error("Student with enrollment number {} not found", enrollmentNumber);
                    return new ResourceNotFoundException("Student", "enrollmentNumber", enrollmentNumber);
                });
        return mapEntityToResponse(student);
    }

    @Override
    public List<StudentResponse> getAllStudents() {
        logger.info("Getting all active students");
        try {
            List<Student> activeStudents = studentRepository.findByIsActive(true);
            logger.info("Found {} active students", activeStudents.size());
            return activeStudents.stream()
                    .map(this::mapEntityToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error retrieving students from database", e);
            logger.info("Returning demo data due to database error");
            return getDemoStudentData();
        }
    }

    @Override
    public List<StudentResponse> getStudentsByDepartment(String department) {
        logger.info("Getting students in department: {}", department);
        List<Student> students = studentRepository.findByDepartmentAndIsActive(department, true);
        logger.info("Found {} students in department {}", students.size(), department);
        return students.stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<StudentResponse> getStudentsByYearOfStudy(Integer yearOfStudy) {
        logger.info("Getting students in year of study: {}", yearOfStudy);
        List<Student> students = studentRepository.findByYearOfStudyAndIsActive(yearOfStudy, true);
        logger.info("Found {} students in year {}", students.size(), yearOfStudy);
        return students.stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<StudentResponse> getStudentsByCgpaGreaterThanEqual(Double cgpa) {
        logger.info("Getting students with CGPA >= {}", cgpa);
        List<Student> students = studentRepository.findByCgpaGreaterThanEqualAndIsActive(cgpa, true);
        logger.info("Found {} students with CGPA >= {}", students.size(), cgpa);
        return students.stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public StudentResponse updateStudent(Long id, StudentRequest request) {
        logger.info("Updating student with ID: {}", id);
        Student student = findStudentById(id);
        
        // Check if updated email is already used by another student
        if (!student.getEmail().equals(request.getEmail())) {
            studentRepository.findByEmailAndIsActive(request.getEmail(), true)
                    .ifPresent(s -> {
                        if (!s.getId().equals(id)) {
                            logger.error("Email {} is already in use by another student", request.getEmail());
                            throw new IllegalArgumentException("Email is already in use by another student");
                        }
                    });
        }
        
        // Check if updated enrollment number is already used by another student
        if (!student.getEnrollmentNumber().equals(request.getEnrollmentNumber())) {
            studentRepository.findByEnrollmentNumberAndIsActive(request.getEnrollmentNumber(), true)
                    .ifPresent(s -> {
                        if (!s.getId().equals(id)) {
                            logger.error("Enrollment number {} is already in use by another student", 
                                    request.getEnrollmentNumber());
                            throw new IllegalArgumentException("Enrollment number is already in use by another student");
                        }
                    });
        }
        
        // Update student properties
        updateStudentFromRequest(student, request);
        
        Student updatedStudent = studentRepository.save(student);
        logger.info("Successfully updated student with ID: {}", id);
        
        return mapEntityToResponse(updatedStudent);
    }

    @Override
    @Transactional
    public void deleteStudent(Long id) {
        logger.info("Soft deleting student with ID: {}", id);
        Student student = findStudentById(id);
        
        student.setIsActive(false);
        studentRepository.save(student);
        logger.info("Successfully soft deleted student with ID: {}", id);
    }
    
    /**
     * Finds a student by ID and ensures it is active.
     *
     * @param id Student ID
     * @return The found student entity
     * @throws ResourceNotFoundException if student is not found or not active
     */
    private Student findStudentById(Long id) {
        return studentRepository.findById(id)
                .filter(Student::getIsActive)
                .orElseThrow(() -> {
                    logger.error("Student with ID {} not found or not active", id);
                    return new ResourceNotFoundException("Student", "id", id);
                });
    }
    
    /**
     * Maps a student entity to a response DTO.
     *
     * @param student Student entity
     * @return StudentResponse DTO
     */
    private StudentResponse mapEntityToResponse(Student student) {
        return StudentResponse.builder()
                .id(student.getId())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .fullName(student.getFirstName() + " " + student.getLastName())
                .email(student.getEmail())
                .enrollmentNumber(student.getEnrollmentNumber())
                .yearOfStudy(student.getYearOfStudy())
                .cgpa(student.getCgpa())
                .department(student.getDepartment())
                .dateOfBirth(student.getDateOfBirth())
                .mobileNumber(student.getMobileNumber())
                .address(student.getAddress())
                .createdAt(student.getCreatedAt())
                .updatedAt(student.getUpdatedAt())
                .build();
    }
    
    /**
     * Maps a request DTO to a student entity.
     *
     * @param request StudentRequest DTO
     * @return Student entity
     */
    private Student mapRequestToEntity(StudentRequest request) {
        Student student = Student.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .enrollmentNumber(request.getEnrollmentNumber())
                .yearOfStudy(request.getYearOfStudy())
                .cgpa(request.getCgpa())
                .department(request.getDepartment())
                .dateOfBirth(request.getDateOfBirth())
                .mobileNumber(request.getMobileNumber())
                .address(request.getAddress())
                .build();
        
        student.setIsActive(true);
        return student;
    }
    
    /**
     * Updates a student entity from a request DTO.
     *
     * @param student Student entity to update
     * @param request StudentRequest DTO with updated values
     */
    private void updateStudentFromRequest(Student student, StudentRequest request) {
        student.setFirstName(request.getFirstName());
        student.setLastName(request.getLastName());
        student.setEmail(request.getEmail());
        student.setEnrollmentNumber(request.getEnrollmentNumber());
        student.setYearOfStudy(request.getYearOfStudy());
        student.setCgpa(request.getCgpa());
        student.setDepartment(request.getDepartment());
        student.setDateOfBirth(request.getDateOfBirth());
        student.setMobileNumber(request.getMobileNumber());
        student.setAddress(request.getAddress());
        student.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Creates demo student data for fallback when database is unavailable.
     *
     * @return List of demo student responses
     */
    private List<StudentResponse> getDemoStudentData() {
        List<StudentResponse> demoStudents = new ArrayList<>();
        
        StudentResponse student1 = new StudentResponse();
        student1.setId(1L);
        student1.setFirstName("John");
        student1.setLastName("Doe");
        student1.setFullName("John Doe");
        student1.setEmail("john.doe@example.com");
        student1.setEnrollmentNumber("CS20210001");
        student1.setYearOfStudy(2);
        student1.setCgpa(BigDecimal.valueOf(8.5));
        student1.setDepartment("Computer Science");
        student1.setCreatedAt(LocalDateTime.now());
        
        StudentResponse student2 = new StudentResponse();
        student2.setId(2L);
        student2.setFirstName("Jane");
        student2.setLastName("Smith");
        student2.setFullName("Jane Smith");
        student2.setEmail("jane.smith@example.com");
        student2.setEnrollmentNumber("EE20210001");
        student2.setYearOfStudy(3);
        student2.setCgpa(BigDecimal.valueOf(9.2));
        student2.setDepartment("Electrical Engineering");
        student2.setCreatedAt(LocalDateTime.now());
        
        demoStudents.add(student1);
        demoStudents.add(student2);
        
        return demoStudents;
    }
} 