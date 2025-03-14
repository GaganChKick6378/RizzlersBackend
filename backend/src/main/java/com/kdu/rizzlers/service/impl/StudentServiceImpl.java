package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.dto.in.StudentRequest;
import com.kdu.rizzlers.dto.out.StudentResponse;
import com.kdu.rizzlers.entity.Student;
import com.kdu.rizzlers.exception.ResourceNotFoundException;
import com.kdu.rizzlers.repository.StudentRepository;
import com.kdu.rizzlers.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    @Override
    @Transactional
    public StudentResponse createStudent(StudentRequest request) {
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
        
        Student savedStudent = studentRepository.save(student);
        return mapToResponse(savedStudent);
    }

    @Override
    public StudentResponse getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));
        return mapToResponse(student);
    }

    @Override
    public StudentResponse getStudentByEnrollmentNumber(String enrollmentNumber) {
        Student student = studentRepository.findByEnrollmentNumberAndIsActive(enrollmentNumber, true)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "enrollmentNumber", enrollmentNumber));
        return mapToResponse(student);
    }

    @Override
    public List<StudentResponse> getAllStudents() {
        return studentRepository.findByIsActive(true)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<StudentResponse> getStudentsByDepartment(String department) {
        return studentRepository.findByDepartmentAndIsActive(department, true)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<StudentResponse> getStudentsByYearOfStudy(Integer yearOfStudy) {
        return studentRepository.findByYearOfStudyAndIsActive(yearOfStudy, true)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<StudentResponse> getStudentsByCgpaGreaterThanEqual(Double cgpa) {
        return studentRepository.findByCgpaGreaterThanEqualAndIsActive(cgpa, true)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public StudentResponse updateStudent(Long id, StudentRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));
        
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
        
        Student updatedStudent = studentRepository.save(student);
        return mapToResponse(updatedStudent);
    }

    @Override
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", "id", id));
        
        student.setIsActive(false);
        studentRepository.save(student);
    }
    
    private StudentResponse mapToResponse(Student student) {
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
} 