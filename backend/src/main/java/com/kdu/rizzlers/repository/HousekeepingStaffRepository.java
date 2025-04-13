package com.kdu.rizzlers.repository;

import com.kdu.rizzlers.entity.HousekeepingStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for HousekeepingStaff entity
 */
@Repository
public interface HousekeepingStaffRepository extends JpaRepository<HousekeepingStaff, Integer> {
    
    /**
     * Find staff by property ID
     * 
     * @param propertyId the property ID
     * @return List of staff for the property
     */
    List<HousekeepingStaff> findByPropertyId(Integer propertyId);
    
    /**
     * Find staff by property ID and preferred shift ID
     * 
     * @param propertyId the property ID
     * @param shiftId the shift ID
     * @return List of staff for the property and shift
     */
    List<HousekeepingStaff> findByPropertyIdAndPreferredShiftId(Integer propertyId, Integer shiftId);
    
    /**
     * Find available staff (not absent) by property ID
     * 
     * @param propertyId the property ID
     * @param date the date to check
     * @return List of available staff
     */
    @Query("SELECT s FROM HousekeepingStaff s WHERE s.propertyId = :propertyId " +
           "AND NOT EXISTS (SELECT a FROM StaffAbsence a WHERE a.id.staffId = s.staffId AND a.id.date = :date)")
    List<HousekeepingStaff> findAvailableStaffByPropertyId(@Param("propertyId") Integer propertyId, 
                                                           @Param("date") LocalDate date);
    
    /**
     * Find available staff by property ID and preferred shift ID
     * 
     * @param propertyId the property ID
     * @param shiftId the shift ID
     * @param date the date to check
     * @return List of available staff for the property and shift
     */
    @Query("SELECT s FROM HousekeepingStaff s WHERE s.propertyId = :propertyId AND s.preferredShiftId = :shiftId " +
           "AND NOT EXISTS (SELECT a FROM StaffAbsence a WHERE a.id.staffId = s.staffId AND a.id.date = :date)")
    List<HousekeepingStaff> findAvailableStaffByPropertyIdAndShiftId(@Param("propertyId") Integer propertyId,
                                                                     @Param("shiftId") Integer shiftId,
                                                                     @Param("date") LocalDate date);
} 