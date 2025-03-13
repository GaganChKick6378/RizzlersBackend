-- Drop existing tables if they exist to ensure clean state
DROP TABLE IF EXISTS students;

-- Create students table
CREATE TABLE students (
    id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    enrollment_number VARCHAR(12) NOT NULL UNIQUE,
    year_of_study INT,
    cgpa DECIMAL(4, 2),
    department VARCHAR(100),
    date_of_birth DATE,
    mobile_number VARCHAR(10),
    address TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Create indexes
CREATE INDEX idx_students_email ON students(email);
CREATE INDEX idx_students_enrollment_number ON students(enrollment_number);
CREATE INDEX idx_students_department ON students(department);
CREATE INDEX idx_students_year_of_study ON students(year_of_study);
CREATE INDEX idx_students_cgpa ON students(cgpa);
CREATE INDEX idx_students_is_active ON students(is_active); 