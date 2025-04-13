package com.kdu.rizzlers.service.impl;

import com.kdu.rizzlers.service.CognitoAuthService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CognitoAuthServiceImpl implements CognitoAuthService {

    @Value("${aws.cognito.jwk-url}")
    private String jwkUrl;
    
    @Value("${aws.cognito.user-pool-id}")
    private String userPoolId;
    
    @Value("${aws.cognito.region}")
    private String region;
    
    @Value("${aws.cognito.app-client-id}")
    private String clientId;
    
    @Override
    public String validateIdToken(String idToken) {
        try {
            // Parse the JWT
            SignedJWT signedJWT = SignedJWT.parse(idToken);
            
            // Get the claims from the token
            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
            
            // Verify that the token is not expired
            if (new Date().after(claimsSet.getExpirationTime())) {
                log.warn("ID token has expired");
                return null;
            }
            
            // Verify that the token was issued for your application
            if (!claimsSet.getAudience().contains(clientId)) {
                log.warn("ID token has invalid audience: {}", claimsSet.getAudience());
                return null;
            }
            
            // Verify that the token was issued by Cognito
            String issuer = "https://cognito-idp." + region + ".amazonaws.com/" + userPoolId;
            if (!claimsSet.getIssuer().equals(issuer)) {
                log.warn("ID token has invalid issuer: {}", claimsSet.getIssuer());
                return null;
            }
            
            // Verify the signature of the token
            boolean signatureVerified = verifyTokenSignature(signedJWT);
            if (!signatureVerified) {
                log.warn("ID token has invalid signature");
                return null;
            }
            
            // If all verifications pass, get user email
            return claimsSet.getStringClaim("email");
            
        } catch (ParseException | JOSEException | IOException e) {
            log.error("Error validating ID token", e);
            return null;
        }
    }
    
    @Override
    public boolean isAuthenticated(String idToken) {
        if (idToken == null || idToken.isEmpty()) {
            return false;
        }
        return validateIdToken(idToken) != null;
    }
    
    private boolean verifyTokenSignature(SignedJWT signedJWT) throws IOException, ParseException, JOSEException {
        // Load JWK set from AWS
        URL jwkURL = new URL(jwkUrl);
        JWKSet jwkSet = JWKSet.load(jwkURL);
        
        // Get the kid (Key ID) from the JWT header
        String keyId = signedJWT.getHeader().getKeyID();
        
        // Find the public key with the matching kid
        List<JWK> keys = jwkSet.getKeys();
        RSAKey publicKey = null;
        
        for (JWK key : keys) {
            if (key.getKeyID().equals(keyId)) {
                publicKey = (RSAKey) key;
                break;
            }
        }
        
        if (publicKey == null) {
            log.warn("Public key not found for kid: {}", keyId);
            return false;
        }
        
        // Create a verifier for the JWT
        JWSVerifier verifier = new RSASSAVerifier((RSAPublicKey) publicKey.toPublicKey());
        
        // Verify the signature
        return signedJWT.verify(verifier);
    }
} 