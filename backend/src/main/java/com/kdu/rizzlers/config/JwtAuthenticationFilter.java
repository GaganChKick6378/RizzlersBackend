package com.kdu.rizzlers.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        // Extract the token if the header is present and starts with "Bearer "
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                // Create signing key
                Key signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
                
                // Parse the JWT
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(signingKey)
                        .build()
                        .parseClaimsJws(jwt)
                        .getBody();
                
                // Extract username (subject)
                username = claims.getSubject();
                
                // If roles are in the claims, extract them
                String roles = (String) claims.get("roles");
                
                // If user isn't authenticated yet but the token is valid
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // Load user details
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    
                    // Create authentication token with authorities from JWT
                    UsernamePasswordAuthenticationToken authToken;
                    
                    if (roles != null && !roles.isEmpty()) {
                        // Parse roles from the JWT
                        List<SimpleGrantedAuthority> authorities = Arrays.stream(roles.split(","))
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList());
                        
                        authToken = new UsernamePasswordAuthenticationToken(
                                userDetails, null, authorities);
                    } else {
                        // If no roles, use authorities from UserDetails
                        authToken = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                    }
                    
                    // Set authentication details
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // Set authentication in context
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                logger.error("Cannot validate token: " + e.getMessage());
            }
        }
        
        chain.doFilter(request, response);
    }
} 