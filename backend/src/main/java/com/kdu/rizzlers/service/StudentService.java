package com.kdu.rizzlers.service;

import com.kdu.rizzlers.dto.in.StudentRequest;
import com.kdu.rizzlers.dto.out.StudentResponse;

import java.util.List;

public interface StudentService {
    StudentResponse createStudent(StudentRequest request);
    StudentResponse getStudentById(Long id);
    StudentResponse getStudentByEnrollmentNumber(String enrollmentNumber);
    List<StudentResponse> getAllStudents();
    List<StudentResponse> getStudentsByDepartment(String department);
    List<StudentResponse> getStudentsByYearOfStudy(Integer yearOfStudy);
    List<StudentResponse> getStudentsByCgpaGreaterThanEqual(Double cgpa);
    StudentResponse updateStudent(Long id, StudentRequest request);
    void deleteStudent(Long id);
} 