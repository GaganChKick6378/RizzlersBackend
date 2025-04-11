package com.kdu.rizzlers.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

public class EncryptionUtilTest {

    private EncryptionUtil encryptionUtil;
    private final String TEST_SECRET_KEY = "TestSecretKey123";
    private final String TEST_SECRET_KEY_SHORT = "Short";
    private final String TEST_SECRET_KEY_LONG = "ThisIsAVeryLongSecretKeyThatExceeds16Bytes";

    @BeforeEach
    public void setup() {
        encryptionUtil = new EncryptionUtil(TEST_SECRET_KEY);
    }

    @Test
    public void testConstructor_NormalKey() {
        // Act
        EncryptionUtil util = new EncryptionUtil(TEST_SECRET_KEY);
        
        // Assert
        assertNotNull(util);
    }

    @Test
    public void testConstructor_ShortKey() {
        // Act
        EncryptionUtil util = new EncryptionUtil(TEST_SECRET_KEY_SHORT);
        
        // Assert
        assertNotNull(util);
    }

    @Test
    public void testConstructor_LongKey() {
        // Act
        EncryptionUtil util = new EncryptionUtil(TEST_SECRET_KEY_LONG);
        
        // Assert
        assertNotNull(util);
    }

    @Test
    public void testEncryptAndDecrypt_Success() {
        // Arrange
        String originalText = "Sensitive data to encrypt";
        
        // Act
        String encryptedText = encryptionUtil.encrypt(originalText);
        String decryptedText = encryptionUtil.decrypt(encryptedText);
        
        // Assert
        assertNotEquals(originalText, encryptedText);
        assertEquals(originalText, decryptedText);
    }

    @ParameterizedTest
    @NullAndEmptySource
    public void testEncrypt_NullOrEmpty(String input) {
        // Act
        String result = encryptionUtil.encrypt(input);
        
        // Assert
        assertEquals(input, result);
    }

    @ParameterizedTest
    @NullAndEmptySource
    public void testDecrypt_NullOrEmpty(String input) {
        // Act
        String result = encryptionUtil.decrypt(input);
        
        // Assert
        assertEquals(input, result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Hello", "12345", "Special@Chars#", "Very long text with lots of words and special characters !@#$%^&*()"})
    public void testEncryptAndDecrypt_VariousInputs(String input) {
        // Act
        String encryptedText = encryptionUtil.encrypt(input);
        String decryptedText = encryptionUtil.decrypt(encryptedText);
        
        // Assert
        assertNotEquals(input, encryptedText);
        assertEquals(input, decryptedText);
    }

    @Test
    public void testDecrypt_InvalidInput() {
        // Arrange
        String invalidEncryptedText = "This is not a valid Base64 encrypted string!@#$";
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            encryptionUtil.decrypt(invalidEncryptedText);
        });
    }

    @Test
    public void testEncryptAndDecrypt_DifferentKeys() {
        // Arrange
        String originalText = "Sensitive data to encrypt";
        String encryptedText = encryptionUtil.encrypt(originalText);
        
        // Create instance with different key
        EncryptionUtil differentKeyUtil = new EncryptionUtil("DifferentKey123");
        
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            differentKeyUtil.decrypt(encryptedText);
        });
    }
} 