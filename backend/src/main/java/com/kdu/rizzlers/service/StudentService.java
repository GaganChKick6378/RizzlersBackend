package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.in.StudentRequest;
import com.kdu.rizzlers.dto.out.StudentResponse;

import java.util.List;

/**
 * Service interface for managing student operations.
 */
public interface StudentService {
    /**
     * Creates a new student.
     *
     * @param request Student information
     * @return Created student response
     */
    StudentResponse createStudent(StudentRequest request);

    /**
     * Retrieves a student by ID.
     *
     * @param id Student ID
     * @return Student response
     */
    StudentResponse getStudentById(Long id);

    /**
     * Retrieves a student by enrollment number.
     *
     * @param enrollmentNumber Student enrollment number
     * @return Student response
     */
    StudentResponse getStudentByEnrollmentNumber(String enrollmentNumber);

    /**
     * Retrieves all active students.
     *
     * @return List of student responses
     */
    List<StudentResponse> getAllStudents();

    /**
     * Retrieves students by department.
     *
     * @param department Department name
     * @return List of student responses
     */
    List<StudentResponse> getStudentsByDepartment(String department);

    /**
     * Retrieves students by year of study.
     *
     * @param yearOfStudy Year of study
     * @return List of student responses
     */
    List<StudentResponse> getStudentsByYearOfStudy(Integer yearOfStudy);

    /**
     * Retrieves students with CGPA greater than or equal to specified value.
     *
     * @param cgpa Minimum CGPA value
     * @return List of student responses
     */
    List<StudentResponse> getStudentsByCgpaGreaterThanEqual(Double cgpa);

    /**
     * Updates an existing student.
     *
     * @param id Student ID
     * @param request Updated student information
     * @return Updated student response
     */
    StudentResponse updateStudent(Long id, StudentRequest request);

    /**
     * Soft deletes a student by setting isActive to false.
     *
     * @param id Student ID
     */
    void deleteStudent(Long id);
} 