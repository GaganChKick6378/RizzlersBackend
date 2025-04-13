package com.kdu.rizzlers.service;

/**
 * Service for validating Cognito authentication tokens
 */
public interface CognitoAuthService {
    
    /**
     * Validate the Cognito ID token and extract user information
     * 
     * @param idToken The ID token from Cognito
     * @return The email of the authenticated user, or null if validation fails
     */
    String validateIdToken(String idToken);
    
    /**
     * Check if the user is authenticated based on the ID token
     * 
     * @param idToken The ID token from Cognito
     * @return true if the token is valid, false otherwise
     */
    boolean isAuthenticated(String idToken);
} 