package com.kdu.rizzlers.constants;

public final class StudentConstants {

    // API Endpoints
    public static final String STUDENTS_API_BASE_PATH = "/api/students";
    public static final String STUDENTS_BY_ID_PATH = "/{id}";
    public static final String STUDENTS_BY_ENROLLMENT_PATH = "/enrollment/{enrollmentNumber}";
    public static final String STUDENTS_BY_DEPARTMENT_PATH = "/department/{department}";
    public static final String STUDENTS_BY_YEAR_PATH = "/year/{yearOfStudy}";
    public static final String STUDENTS_BY_CGPA_PATH = "/cgpa/{cgpa}";
    
    // Error Messages
    public static final String STUDENT_NOT_FOUND_BY_ID = "Student not found with id: ";
    public static final String STUDENT_NOT_FOUND_BY_ENROLLMENT = "Student not found with enrollment number: ";
    public static final String STUDENT_DUPLICATE_ENROLLMENT = "Student with enrollment number %s already exists";
    public static final String STUDENT_DUPLICATE_EMAIL = "Student with email %s already exists";
    
    // Validation Messages
    public static final String FIRST_NAME_REQUIRED = "First name is required";
    public static final String LAST_NAME_REQUIRED = "Last name is required";
    public static final String EMAIL_REQUIRED = "Email is required";
    public static final String ENROLLMENT_REQUIRED = "Enrollment number is required";
    public static final String YEAR_OF_STUDY_REQUIRED = "Year of study is required";
    public static final String DEPARTMENT_REQUIRED = "Department is required";
    
    private StudentConstants() {
        // Private constructor to prevent instantiation
    }
} 