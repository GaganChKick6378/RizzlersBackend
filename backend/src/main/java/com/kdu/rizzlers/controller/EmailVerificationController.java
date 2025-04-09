package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.dto.OtpResponseDTO;
import com.kdu.rizzlers.service.EmailOtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for email verification operations
 */
@RestController
@RequestMapping("/email-verification")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Email Verification", description = "APIs for email verification using OTP")
public class EmailVerificationController {

    private final EmailOtpService emailOtpService;
    
    /**
     * Generate and send OTP to the specified email
     * 
     * @param email The recipient's email address
     * @return ResponseEntity with success or error message
     */
    @PostMapping("/send-otp")
    @Operation(summary = "Generate and send OTP", 
               description = "Generates a new OTP and sends it to the specified email address")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OTP sent successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid email address"),
        @ApiResponse(responseCode = "500", description = "Failed to send OTP")
    })
    public ResponseEntity<OtpResponseDTO> sendOtp(
            @Parameter(description = "Email address to send the OTP to", required = true) 
            @RequestParam String email) {
        
        log.info("Received request to send OTP to email: {}", email);
        OtpResponseDTO response = emailOtpService.generateAndSendOtp(email);
        
        if (response.getSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Verify OTP for the specified email
     * 
     * @param email The email address the OTP was sent to
     * @param otp The OTP code provided by the user
     * @return ResponseEntity with success or error message
     */
    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP", 
               description = "Verifies the OTP provided by the user for the specified email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OTP verified successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid OTP or email"),
        @ApiResponse(responseCode = "404", description = "No OTP found for this email")
    })
    public ResponseEntity<OtpResponseDTO> verifyOtp(
            @Parameter(description = "Email address the OTP was sent to", required = true) 
            @RequestParam String email,
            
            @Parameter(description = "OTP code provided by the user", required = true) 
            @RequestParam String otp) {
        
        log.info("Received request to verify OTP for email: {}", email);
        OtpResponseDTO response = emailOtpService.verifyOtp(email, otp);
        
        if (response.getSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Resend OTP to the specified email
     * 
     * @param email The recipient's email address
     * @return ResponseEntity with success or error message
     */
    @PostMapping("/resend-otp")
    @Operation(summary = "Resend OTP", 
               description = "Resends the OTP to the specified email if it exists and is still valid")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OTP resent successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid email address or email already verified"),
        @ApiResponse(responseCode = "500", description = "Failed to resend OTP")
    })
    public ResponseEntity<OtpResponseDTO> resendOtp(
            @Parameter(description = "Email address to resend the OTP to", required = true) 
            @RequestParam String email) {
        
        log.info("Received request to resend OTP to email: {}", email);
        OtpResponseDTO response = emailOtpService.resendOtp(email);
        
        if (response.getSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
} 