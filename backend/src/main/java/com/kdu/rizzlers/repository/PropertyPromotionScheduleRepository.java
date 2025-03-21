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

    /**
     * Find active promotions for a specific property where the date range overlaps with the given period
     */
    @Query("SELECT p FROM PropertyPromotionSchedule p WHERE p.propertyId = :propertyId " +
           "AND p.isActive = true " +
           "AND ((p.startDate BETWEEN :startDate AND :endDate) " +
           "OR (p.endDate BETWEEN :startDate AND :endDate) " +
           "OR (:startDate BETWEEN p.startDate AND p.endDate) " +
           "OR (:endDate BETWEEN p.startDate AND p.endDate))")
    List<PropertyPromotionSchedule> findActivePromotionsForPropertyInPeriod(
            Integer propertyId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Find all active promotions for a property
     */
    List<PropertyPromotionSchedule> findByPropertyIdAndIsActiveTrue(Integer propertyId);
    
    /**
     * Find all promotions for a property, regardless of active status
     */
    default List<PropertyPromotionSchedule> findAllByPropertyId(Integer propertyId) {
        return findByPropertyId(propertyId);
    }
} 