package com.kdu.rizzlers.repository;

import com.kdu.rizzlers.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByIsActive(Boolean isActive);
    Optional<Student> findByEnrollmentNumberAndIsActive(String enrollmentNumber, Boolean isActive);
    Optional<Student> findByEmailAndIsActive(String email, Boolean isActive);
    List<Student> findByDepartmentAndIsActive(String department, Boolean isActive);
    List<Student> findByYearOfStudyAndIsActive(Integer yearOfStudy, Boolean isActive);
    List<Student> findByCgpaGreaterThanEqualAndIsActive(Double cgpa, Boolean isActive);
} 