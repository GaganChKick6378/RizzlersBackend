package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.dto.in.StudentRequest;
import com.kdu.rizzlers.dto.out.StudentResponse;
import com.kdu.rizzlers.entity.Student;
import com.kdu.rizzlers.exception.ResourceNotFoundException;
import com.kdu.rizzlers.repository.StudentRepository;
import com.kdu.rizzlers.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {
    @Autowired
    public StudentRepository studentRepository;
    private static final Logger logger = LoggerFactory.getLogger(StudentServiceImpl.class);

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
        try {
            List<Student> students = studentRepository.findByIsActive(true);
            return students.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Database connection failed, returning demo data", e);
            return getDemoStudentData();
        }
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

    private List<StudentResponse> getDemoStudentData() {
        List<StudentResponse> demoStudents = new ArrayList<>();
        
        StudentResponse student1 = new StudentResponse();
        student1.setId(1L);
        student1.setFirstName("John");
        student1.setLastName("Doe");
        student1.setEmail("john.doe@example.com");
        student1.setEnrollmentNumber("E2023001");
        student1.setYearOfStudy(2);
        student1.setCgpa(BigDecimal.valueOf(8.5));
        student1.setDepartment("Computer Science");
        
        StudentResponse student2 = new StudentResponse();
        student2.setId(2L);
        student2.setFirstName("Jane");
        student2.setLastName("Smith");
        student2.setEmail("jane.smith@example.com");
        student2.setEnrollmentNumber("E2023002");
        student2.setYearOfStudy(3);
        student2.setCgpa(BigDecimal.valueOf(9.2));
        student2.setDepartment("Electronics");
        
        demoStudents.add(student1);
        demoStudents.add(student2);
        
        return demoStudents;
    }
} 