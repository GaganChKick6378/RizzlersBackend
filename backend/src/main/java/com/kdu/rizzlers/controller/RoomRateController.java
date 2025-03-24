package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.dto.out.DailyRoomRateDTO;
import com.kdu.rizzlers.entity.PropertyPromotionSchedule;
import com.kdu.rizzlers.service.RoomRateService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/room-rates")
@RequiredArgsConstructor
public class RoomRateController {

    private final RoomRateService roomRateService;

    /**
     * Get active promotions for a property during a specific date range
     * 
     * @param propertyId The property ID
     * @param startDate Start date of the range
     * @param endDate End date of the range
     * @return List of property promotions for the date range
     */
    @GetMapping("/promotions")
    public ResponseEntity<List<PropertyPromotionSchedule>> getActivePromotions(
            @RequestParam Integer propertyId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<PropertyPromotionSchedule> promotions = roomRateService.getActivePromotions(
                propertyId, startDate, endDate);
        
        return ResponseEntity.ok(promotions);
    }
    
    /**
     * Get all promotions for a property
     * 
     * @param propertyId The property ID
     * @return List of all property promotions
     */
    @GetMapping("/all-promotions")
    public ResponseEntity<List<PropertyPromotionSchedule>> getAllPromotions(
            @RequestParam Integer propertyId) {
        
        List<PropertyPromotionSchedule> promotions = roomRateService.getAllPromotions(propertyId);
        
        return ResponseEntity.ok(promotions);
    }

    /**
     * Get ALL minimum daily room rates for a property, including promotion information,
     * without filtering by date range
     * 
     * @param tenantId The tenant ID
     * @param propertyId The property ID
     * @return List of daily room rates with promotion information
     */
    @GetMapping("/daily-rates")
    public ResponseEntity<List<DailyRoomRateDTO>> getDailyRatesWithPromotions(
            @RequestParam Integer tenantId,
            @RequestParam Integer propertyId) {
        
        List<DailyRoomRateDTO> dailyRates = roomRateService.getDailyRatesWithPromotions(
                tenantId, propertyId);
        
        return ResponseEntity.ok(dailyRates);
    }
} 