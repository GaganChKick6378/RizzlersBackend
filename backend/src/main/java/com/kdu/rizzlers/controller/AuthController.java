package com.kdu.rizzlers.controller;

import com.kdu.rizzlers.entity.HousekeepingUser;
import com.kdu.rizzlers.service.HousekeepingUserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.kdu.rizzlers.service.CognitoAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for authentication
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "APIs for authentication operations")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final HousekeepingUserService userService;
    private final CognitoAuthService cognitoAuthService;
    
    @Value("${jwt.secret:defaultSecretKeyThatIsLongEnoughForHS512Signature}")
    private String jwtSecret;
    
    @Value("${jwt.expiration:86400}")
    private long jwtExpiration;

    /**
     * Login endpoint
     */
    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates a user and returns a JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully authenticated"),
        @ApiResponse(responseCode = "401", description = "Invalid username or password"),
        @ApiResponse(responseCode = "500", description = "Authentication failed due to server error")
    })
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
            
            // Get user details
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            
            // Get user role for inclusion in token
            String roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","));
            
            // Get user ID if needed
            Optional<HousekeepingUser> userOpt = userService.getUserByUsername(userDetails.getUsername());
            Integer userId = userOpt.map(HousekeepingUser::getUserId).orElse(null);
            Integer staffId = userOpt.map(HousekeepingUser::getStaffId).orElse(null);
            
            // Build claims
            Map<String, Object> claims = new HashMap<>();
            claims.put("roles", roles);
            if (userId != null) claims.put("userId", userId);
            if (staffId != null) claims.put("staffId", staffId);
            
            // Create signing key
            Key signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            
            // Generate JWT token
            String token = Jwts.builder()
                    .setClaims(claims)
                    .setSubject(userDetails.getUsername())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration * 1000))
                    .signWith(signingKey)
                    .compact();
            
            // Create response
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("type", "Bearer");
            response.put("username", userDetails.getUsername());
            response.put("roles", roles);
            response.put("expiresIn", jwtExpiration);
            
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Authentication failed: " + e.getMessage()));
        }
    }
    
    /**
     * Simple DTO for login requests
     */
    public static class LoginRequest {
        private String username;
        private String password;
        
        // Default constructor for Jackson
        public LoginRequest() {
        }
        
        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
        
        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    /**
     * Verify if a token is valid and return authenticated user information
     * 
     * @param idToken The Cognito ID token to validate
     * @return Authentication status and user email if authenticated
     */
    @PostMapping("/verify")
    @Operation(summary = "Verify authentication", description = "Check if a Cognito token is valid and return authenticated user information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token is valid"),
        @ApiResponse(responseCode = "401", description = "Token is invalid or expired")
    })
    public ResponseEntity<Map<String, Object>> verifyAuthentication(
            @Parameter(description = "ID token from Cognito", required = true)
            @RequestBody Map<String, String> request) {
        
        String idToken = request.get("idToken");
        if (idToken == null || idToken.isEmpty()) {
            return ResponseEntity.status(401).body(createResponse(false, null));
        }
        
        String email = cognitoAuthService.validateIdToken(idToken);
        
        if (email != null) {
            log.info("Authentication successful for user: {}", email);
            return ResponseEntity.ok(createResponse(true, email));
        } else {
            log.warn("Authentication failed for token");
            return ResponseEntity.status(401).body(createResponse(false, null));
        }
    }
    
    private Map<String, Object> createResponse(boolean authenticated, String email) {
        Map<String, Object> response = new HashMap<>();
        response.put("authenticated", authenticated);
        
        if (email != null) {
            response.put("email", email);
        }
        
        return response;
    }
} 