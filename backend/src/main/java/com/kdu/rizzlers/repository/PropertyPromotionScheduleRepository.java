package com.kdu.rizzlers.repository;

import com.kdu.rizzlers.entity.PropertyPromotionSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PropertyPromotionScheduleRepository extends JpaRepository<PropertyPromotionSchedule, Long> {
    List<PropertyPromotionSchedule> findByPropertyId(Integer propertyId);
    List<PropertyPromotionSchedule> findByPromotionId(Integer promotionId);
    
    @Query("SELECT p FROM PropertyPromotionSchedule p WHERE p.propertyId = :propertyId " +
           "AND ((p.startDate <= :endDate AND p.endDate >= :startDate))")
    List<PropertyPromotionSchedule> findActivePromotionsForPropertyBetweenDates(
            @Param("propertyId") Integer propertyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
} 