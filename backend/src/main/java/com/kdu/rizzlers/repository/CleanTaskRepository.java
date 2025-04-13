package com.kdu.rizzlers.repository;

import com.kdu.rizzlers.entity.CleanTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for CleanTask entity
 */
@Repository
public interface CleanTaskRepository extends JpaRepository<CleanTask, Integer> {
    
    /**
     * Find tasks by property ID and date
     * 
     * @param propertyId the property ID
     * @param date the date
     * @return List of tasks for the property and date
     */
    List<CleanTask> findByPropertyIdAndDate(Integer propertyId, LocalDate date);
    
    /**
     * Find tasks by staff ID and date
     * 
     * @param staffId the staff ID
     * @param date the date
     * @return List of tasks for the staff and date
     */
    List<CleanTask> findByStaffIdAndDate(Integer staffId, LocalDate date);
    
    /**
     * Find clean tasks for a given room and date
     * 
     * @param externalRoomId the external room ID from GraphQL
     * @param date the date
     * @return list of clean tasks for the room and date
     */
    List<CleanTask> findByExternalRoomIdAndDate(String externalRoomId, LocalDate date);
} 