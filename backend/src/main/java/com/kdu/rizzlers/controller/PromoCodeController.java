package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.dto.in.PromoCodeValidateRequest;
import com.kdu.rizzlers.dto.out.PromoCodeResponse;
import com.kdu.rizzlers.dto.out.PropertyPromotionScheduleResponse;
import com.kdu.rizzlers.service.PromoCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for promo code operations
 */
@Slf4j
@RestController
@RequestMapping("/promo-codes")
@RequiredArgsConstructor
@Tag(name = "Promo Codes", description = "APIs for promo code operations")
public class PromoCodeController {

    private final PromoCodeService promoCodeService;
    
    /**
     * Get all visible promotions
     * 
     * @return List of all visible promotions
     */
    @GetMapping("/visible")
    @Operation(summary = "Get visible promotions", description = "Returns all visible promotions")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved visible promotions",
                    content = @Content(schema = @Schema(implementation = PromoCodeResponse.class)))
    })
    public ResponseEntity<List<PromoCodeResponse>> getVisiblePromotions() {
        log.info("Received request to get all visible promotions");
        
        List<PromoCodeResponse> visiblePromotions = promoCodeService.getAllVisiblePromotions()
                .stream()
                .map(promotion -> PromoCodeResponse.builder()
                        .promotionId(promotion.getPromotionId() * 1000 + 2) // Apply RDS identifier suffix
                        .title(promotion.getTitle())
                        .description(promotion.getDescription())
                        .priceFactor(promotion.getPriceFactor())
                        .build())
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(visiblePromotions);
    }
    
    /**
     * Validate a promo code using path parameter
     * 
     * @param promoCode The promo code to validate
     * @return The promotion details if valid, error response otherwise
     */
    @GetMapping("/validate/{promoCode}")
    @Operation(summary = "Validate promo code by path", description = "Validates a promo code using path parameter")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Valid promo code"),
        @ApiResponse(responseCode = "400", description = "Invalid promo code")
    })
    public ResponseEntity<Object> validatePromoCodeByPath(
            @Parameter(description = "The promo code to validate", required = true)
            @PathVariable String promoCode) {
        log.info("Received GET request to validate promo code: {}", promoCode);
        
        return promoCodeService.validatePromoCode(promoCode)
                .map(response -> {
                    // Transform the promotionId to include RDS identifier (multiply by 1000 and add 2)
                    response.setPromotionId(response.getPromotionId() * 1000 + 2);
                    // Return full response
                    return ResponseEntity.ok().body((Object) response);
                })
                .orElseGet(() -> {
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Invalid promo code");
                    errorResponse.put("message", "The promo code is invalid, expired, or not available");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
                });
    }
    
    /**
     * Validate a promo code using POST request
     * 
     * @param request The request body containing the promo code
     * @return A simplified response with only price factor, title, description, promotion id if valid
     */
    @PostMapping("/validate")
    @Operation(summary = "Validate promo code", description = "Validates a promo code using POST request body")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Valid promo code",
                    content = @Content(schema = @Schema(implementation = PromoCodeResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid promo code")
    })
    public ResponseEntity<Object> validatePromoCodePost(
            @Parameter(description = "Request body containing the promo code", required = true)
            @Valid @RequestBody PromoCodeValidateRequest request) {
        log.info("Received POST request to validate promo code: {}", request.getPromoCode());
        
        return promoCodeService.validatePromoCode(request.getPromoCode())
                .map(response -> {
                    // Create simplified response with only requested fields
                    // Apply RDS identifier transformation to promotion ID
                    PromoCodeResponse simplifiedResponse = PromoCodeResponse.builder()
                            .promotionId(response.getPromotionId() * 1000 + 2) // Apply RDS identifier suffix
                            .title(response.getTitle())
                            .description(response.getDescription())
                            .priceFactor(response.getPriceFactor())
                            .build();
                    return ResponseEntity.ok().body((Object) simplifiedResponse);
                })
                .orElseGet(() -> {
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Invalid promo code");
                    errorResponse.put("message", "The promo code is invalid, expired, or not available");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
                });
    }
} 