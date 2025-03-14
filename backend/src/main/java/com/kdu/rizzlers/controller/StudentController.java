package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.dto.in.StudentRequest;
import com.kdu.rizzlers.dto.out.StudentResponse;
import com.kdu.rizzlers.exception.ResourceNotFoundException;
import com.kdu.rizzlers.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for managing Student resources.
 */
@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {
    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);

    @Autowired
    public StudentService studentService;

    /**
     * Creates a new student.
     *
     * @param request student information
     * @return the created student
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StudentResponse> createStudent(
            @Valid @RequestBody StudentRequest request) {
        logger.info("REST request to create a new student");
        StudentResponse createdStudent = studentService.createStudent(request);
        return new ResponseEntity<>(createdStudent, HttpStatus.CREATED);
    }

    /**
     * Retrieves a student by ID.
     *
     * @param id student ID
     * @return the student
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StudentResponse> getStudentById(
            @PathVariable Long id) {
        logger.info("REST request to get student with ID: {}", id);
        StudentResponse student = studentService.getStudentById(id);
        return ResponseEntity.ok(student);
    }

    /**
     * Retrieves a student by enrollment number.
     *
     * @param enrollmentNumber student enrollment number
     * @return the student
     */
    @GetMapping(value = "/enrollment/{enrollmentNumber}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StudentResponse> getStudentByEnrollmentNumber(
            @PathVariable String enrollmentNumber) {
        logger.info("REST request to get student with enrollment number: {}", enrollmentNumber);
        StudentResponse student = studentService.getStudentByEnrollmentNumber(enrollmentNumber);
        return ResponseEntity.ok(student);
    }

    /**
     * Retrieves all active students.
     *
     * @return list of students
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<StudentResponse>> getAllStudents() {
        logger.info("REST request to get all students");
        List<StudentResponse> students = studentService.getAllStudents();
        return ResponseEntity.ok(students);
    }

    /**
     * Retrieves students by department.
     *
     * @param department department name
     * @return list of students in the specified department
     */
    @GetMapping(value = "/department/{department}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<StudentResponse>> getStudentsByDepartment(
            @PathVariable String department) {
        logger.info("REST request to get students in department: {}", department);
        List<StudentResponse> students = studentService.getStudentsByDepartment(department);
        return ResponseEntity.ok(students);
    }

    /**
     * Retrieves students by year of study.
     *
     * @param yearOfStudy year of study
     * @return list of students in the specified year
     */
    @GetMapping(value = "/year/{yearOfStudy}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<StudentResponse>> getStudentsByYearOfStudy(
            @PathVariable Integer yearOfStudy) {
        logger.info("REST request to get students in year: {}", yearOfStudy);
        List<StudentResponse> students = studentService.getStudentsByYearOfStudy(yearOfStudy);
        return ResponseEntity.ok(students);
    }

    /**
     * Retrieves students with CGPA greater than or equal to specified value.
     *
     * @param cgpa minimum CGPA value
     * @return list of students with CGPA ≥ the specified value
     */
    @GetMapping(value = "/cgpa/{cgpa}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<StudentResponse>> getStudentsByCgpaGreaterThanEqual(
            @PathVariable Double cgpa) {
        logger.info("REST request to get students with CGPA >= {}", cgpa);
        List<StudentResponse> students = studentService.getStudentsByCgpaGreaterThanEqual(cgpa);
        return ResponseEntity.ok(students);
    }

    /**
     * Updates an existing student.
     *
     * @param id student ID
     * @param request updated student information
     * @return the updated student
     */
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<StudentResponse> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentRequest request) {
        logger.info("REST request to update student with ID: {}", id);
        StudentResponse updatedStudent = studentService.updateStudent(id, request);
        return ResponseEntity.ok(updatedStudent);
    }

    /**
     * Deletes a student.
     *
     * @param id student ID
     * @return no content response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(
            @PathVariable Long id) {
        logger.info("REST request to delete student with ID: {}", id);
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Exception handler for ResourceNotFoundException.
     *
     * @param ex the exception
     * @return error response with 404 status
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Not Found");
        errorResponse.put("message", ex.getMessage());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
    
    /**
     * Exception handler for IllegalArgumentException.
     *
     * @param ex the exception
     * @return error response with 400 status
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Bad Request");
        errorResponse.put("message", ex.getMessage());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
} 