package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.dto.out.DailyRoomRateDTO;
import com.kdu.rizzlers.entity.PropertyPromotionSchedule;
import com.kdu.rizzlers.service.RoomRateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/room-rates")
@RequiredArgsConstructor
@Tag(name = "Room Rates", description = "APIs for room rate and promotion information")
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
    @Operation(summary = "Get active promotions", 
              description = "Retrieves active promotions for a property during a specific date range")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved active promotions",
                   content = @Content(schema = @Schema(implementation = PropertyPromotionSchedule.class)))
    })
    public ResponseEntity<List<PropertyPromotionSchedule>> getActivePromotions(
            @Parameter(description = "Property ID", required = true)
            @RequestParam Integer propertyId,
            
            @Parameter(description = "Start date of the range (YYYY-MM-DD)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            
            @Parameter(description = "End date of the range (YYYY-MM-DD)", required = true)
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
    @Operation(summary = "Get all property promotions", 
              description = "Retrieves all promotions for a property")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all promotions",
                   content = @Content(schema = @Schema(implementation = PropertyPromotionSchedule.class)))
    })
    public ResponseEntity<List<PropertyPromotionSchedule>> getAllPromotions(
            @Parameter(description = "Property ID", required = true)
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
    @Operation(summary = "Get daily room rates", 
              description = "Retrieves all minimum daily room rates for a property including promotion information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved daily room rates",
                   content = @Content(schema = @Schema(implementation = DailyRoomRateDTO.class)))
    })
    public ResponseEntity<List<DailyRoomRateDTO>> getDailyRatesWithPromotions(
            @Parameter(description = "Tenant ID", required = true)
            @RequestParam Integer tenantId,
            
            @Parameter(description = "Property ID", required = true)
            @RequestParam Integer propertyId) {
        
        List<DailyRoomRateDTO> dailyRates = roomRateService.getDailyRatesWithPromotions(
                tenantId, propertyId);
        
        return ResponseEntity.ok(dailyRates);
    }
} 