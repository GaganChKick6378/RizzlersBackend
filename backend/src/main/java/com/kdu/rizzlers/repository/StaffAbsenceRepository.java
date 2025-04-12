package com.kdu.rizzlers.repository;

import com.kdu.rizzlers.entity.StaffAbsence;
import com.kdu.rizzlers.entity.StaffAbsence.StaffAbsenceId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for StaffAbsence entity
 */
@Repository
public interface StaffAbsenceRepository extends JpaRepository<StaffAbsence, StaffAbsenceId> {
    
    /**
     * Find absences by staff ID
     * 
     * @param staffId the staff ID
     * @return List of absences for the staff
     */
    @Query("SELECT a FROM StaffAbsence a WHERE a.id.staffId = :staffId")
    List<StaffAbsence> findByStaffId(@Param("staffId") Integer staffId);
    
    /**
     * Find absences by date
     * 
     * @param date the date
     * @return List of absences for the date
     */
    @Query("SELECT a FROM StaffAbsence a WHERE a.id.date = :date")
    List<StaffAbsence> findByDate(@Param("date") LocalDate date);
    
    /**
     * Find absences by property ID and date
     * 
     * @param propertyId the property ID
     * @param date the date
     * @return List of absences for the property and date
     */
    @Query("SELECT a FROM StaffAbsence a JOIN a.staff s WHERE s.propertyId = :propertyId AND a.id.date = :date")
    List<StaffAbsence> findByPropertyIdAndDate(@Param("propertyId") Integer propertyId, @Param("date") LocalDate date);
    
    /**
     * Find absences by date range
     * 
     * @param startDate the start date
     * @param endDate the end date
     * @return List of absences for the date range
     */
    @Query("SELECT a FROM StaffAbsence a WHERE a.id.date BETWEEN :startDate AND :endDate")
    List<StaffAbsence> findByDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
} 