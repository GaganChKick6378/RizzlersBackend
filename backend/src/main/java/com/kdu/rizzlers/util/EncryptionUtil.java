package com.kdu.rizzlers.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;

/**
 * Utility class for encrypting and decrypting sensitive data
 */
@Component
public class EncryptionUtil {

    private final String secretKey;
    private static final String ALGORITHM = "AES";

    /**
     * Constructor with secret key injection
     * 
     * @param secretKey the secret key for encryption/decryption
     */
    public EncryptionUtil(@Value("${encryption.secret-key}") String secretKey) {
        // Ensure the key is exactly 16 bytes for AES-128
        if (secretKey.length() > 16) {
            this.secretKey = secretKey.substring(0, 16);
        } else if (secretKey.length() < 16) {
            // Pad the key if it's less than 16 characters
            this.secretKey = String.format("%-16s", secretKey);
        } else {
            this.secretKey = secretKey;
        }
    }

    /**
     * Encrypt a string value
     * 
     * @param data the data to encrypt
     * @return the encrypted string (Base64 encoded)
     */
    public String encrypt(String data) {
        try {
            if (data == null || data.isEmpty()) {
                return data;
            }
            
            Key key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            
            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting data", e);
        }
    }

    /**
     * Decrypt an encrypted string value
     * 
     * @param encryptedData the encrypted data (Base64 encoded)
     * @return the decrypted string
     */
    public String decrypt(String encryptedData) {
        try {
            if (encryptedData == null || encryptedData.isEmpty()) {
                return encryptedData;
            }
            
            Key key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting data", e);
        }
    }
} 