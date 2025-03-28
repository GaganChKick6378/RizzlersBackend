package com.kdu.rizzlers.repository;

import com.kdu.rizzlers.entity.PropertyPromotionSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

    @Query("SELECT p FROM PropertyPromotionSchedule p WHERE p.propertyId = :propertyId AND p.isActive = true " +
           "AND p.startDate <= :date AND p.endDate >= :date")
    List<PropertyPromotionSchedule> findActivePromotionsByPropertyIdAndDate(
            @Param("propertyId") Integer propertyId, 
            @Param("date") LocalDate date);
    
    /**
     * Find a promotion by its promo code
     * 
     * @param promoCode The promo code to search for
     * @return The promotion entity if found
     */
    Optional<PropertyPromotionSchedule> findByPromoCode(String promoCode);
    
    /**
     * Find an active and visible promotion by its promo code and within a valid date range
     * 
     * @param promoCode The promo code to validate
     * @param startDateCheck Current date to check against start date
     * @param endDateCheck Current date to check against end date
     * @return The promotion entity if valid
     */
    Optional<PropertyPromotionSchedule> findByPromoCodeAndIsActiveTrueAndIsVisibleTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            String promoCode, 
            LocalDate startDateCheck, 
            LocalDate endDateCheck);
            
    /**
     * Find an active promotion by its promo code and within a valid date range, regardless of visibility
     * 
     * @param promoCode The promo code to validate
     * @param startDateCheck Current date to check against start date
     * @param endDateCheck Current date to check against end date
     * @return The promotion entity if valid
     */
    Optional<PropertyPromotionSchedule> findByPromoCodeAndIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            String promoCode, 
            LocalDate startDateCheck, 
            LocalDate endDateCheck);
            
    /**
     * Find all active and visible promotions within a valid date range
     *
     * @param date Current date to check against start and end dates
     * @return List of active, visible promotions
     */
    List<PropertyPromotionSchedule> findByIsActiveTrueAndIsVisibleTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            LocalDate date,
            LocalDate dateEnd);
} 