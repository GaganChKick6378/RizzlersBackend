package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.dto.in.StudentRequest;
import com.kdu.rizzlers.dto.out.StudentResponse;
import com.kdu.rizzlers.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/students")
public class StudentController {
    @Autowired
    public StudentService studentService;

    @PostMapping
    public ResponseEntity<StudentResponse> createStudent(@Valid @RequestBody StudentRequest request) {
        return new ResponseEntity<>(studentService.createStudent(request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentResponse> getStudentById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    @GetMapping("/enrollment/{enrollmentNumber}")
    public ResponseEntity<StudentResponse> getStudentByEnrollmentNumber(@PathVariable String enrollmentNumber) {
        return ResponseEntity.ok(studentService.getStudentByEnrollmentNumber(enrollmentNumber));
    }

    @GetMapping
    public ResponseEntity<List<StudentResponse>> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @GetMapping("/department/{department}")
    public ResponseEntity<List<StudentResponse>> getStudentsByDepartment(@PathVariable String department) {
        return ResponseEntity.ok(studentService.getStudentsByDepartment(department));
    }

    @GetMapping("/year/{yearOfStudy}")
    public ResponseEntity<List<StudentResponse>> getStudentsByYearOfStudy(@PathVariable Integer yearOfStudy) {
        return ResponseEntity.ok(studentService.getStudentsByYearOfStudy(yearOfStudy));
    }

    @GetMapping("/cgpa/{cgpa}")
    public ResponseEntity<List<StudentResponse>> getStudentsByCgpaGreaterThanEqual(@PathVariable Double cgpa) {
        return ResponseEntity.ok(studentService.getStudentsByCgpaGreaterThanEqual(cgpa));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentResponse> updateStudent(@PathVariable Long id, @Valid @RequestBody StudentRequest request) {
        return ResponseEntity.ok(studentService.updateStudent(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }
} 