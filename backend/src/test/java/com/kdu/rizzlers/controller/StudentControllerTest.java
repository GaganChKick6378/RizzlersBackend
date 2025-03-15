package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.dto.out.StudentResponse;
import com.kdu.rizzlers.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class StudentControllerTest {

    @Mock
    private StudentService studentService;

    @InjectMocks
    private StudentController studentController;

    private StudentResponse mockStudentResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize mock response
        mockStudentResponse = StudentResponse.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .fullName("John Doe")
                .email("john.doe@example.com")
                .enrollmentNumber("KDU12345")
                .yearOfStudy(2)
                .cgpa(new BigDecimal("8.5"))
                .department("Computer Science")
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .mobileNumber("9876543210")
                .address("123 Main St")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getStudentById_ShouldReturnStudent_WhenValidIdProvided() {
        // Arrange
        Long studentId = 1L;
        when(studentService.getStudentById(studentId)).thenReturn(mockStudentResponse);

        // Act
        ResponseEntity<StudentResponse> response = studentController.getStudentById(studentId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        StudentResponse studentResponse = response.getBody();
        assertNotNull(studentResponse);
        assertEquals(studentId, studentResponse.getId());
        assertEquals("John", studentResponse.getFirstName());
        assertEquals("Doe", studentResponse.getLastName());
        assertEquals("John Doe", studentResponse.getFullName());
        assertEquals("john.doe@example.com", studentResponse.getEmail());
        assertEquals("KDU12345", studentResponse.getEnrollmentNumber());
        assertEquals(2, studentResponse.getYearOfStudy());
        assertEquals(new BigDecimal("8.5"), studentResponse.getCgpa());
        assertEquals("Computer Science", studentResponse.getDepartment());
    }
} 