package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.dto.in.PromoCodeValidateRequest;
import com.kdu.rizzlers.dto.out.PromoCodeResponse;
import com.kdu.rizzlers.dto.out.PropertyPromotionScheduleResponse;
import com.kdu.rizzlers.service.PromoCodeService;
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
public class PromoCodeController {

    private final PromoCodeService promoCodeService;
    
    /**
     * Get all visible promotions
     * 
     * @return List of all visible promotions
     */
    @GetMapping("/visible")
    public ResponseEntity<List<PromoCodeResponse>> getVisiblePromotions() {
        log.info("Received request to get all visible promotions");
        
        List<PromoCodeResponse> visiblePromotions = promoCodeService.getAllVisiblePromotions()
                .stream()
                .map(promotion -> PromoCodeResponse.builder()
                        .promotionId(promotion.getPromotionId())
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
    public ResponseEntity<Object> validatePromoCodeByPath(@PathVariable String promoCode) {
        log.info("Received GET request to validate promo code: {}", promoCode);
        
        return promoCodeService.validatePromoCode(promoCode)
                .map(response -> {
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
    public ResponseEntity<Object> validatePromoCodePost(@Valid @RequestBody PromoCodeValidateRequest request) {
        log.info("Received POST request to validate promo code: {}", request.getPromoCode());
        
        return promoCodeService.validatePromoCode(request.getPromoCode())
                .map(response -> {
                    // Create simplified response with only requested fields
                    PromoCodeResponse simplifiedResponse = PromoCodeResponse.builder()
                            .promotionId(response.getPromotionId())
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