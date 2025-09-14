package com.securevault.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.SecretKeyFactory;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * Advanced encryption utility using AES-GCM for secure credential storage.
 * Much more secure than the original Feistel cipher implementation.
 */
public class EncryptionUtil {
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int GCM_IV_LENGTH = 12; // 96 bits
    private static final int GCM_TAG_LENGTH = 16; // 128 bits
    private static final int SALT_LENGTH = 16; // 128 bits
    private static final int KEY_LENGTH = 256; // 256 bits
    private static final int PBKDF2_ITERATIONS = 100000; // Strong iteration count
    
    /**
     * Encrypts plaintext using AES-GCM with a password-derived key.
     * 
     * @param plaintext The text to encrypt
     * @param password The master password
     * @return Base64-encoded encrypted data with salt and IV
     */
    public static String encrypt(String plaintext, String password) {
        try {
            // Generate random salt
            byte[] salt = generateRandomBytes(SALT_LENGTH);
            
            // Derive key from password
            SecretKey secretKey = deriveKeyFromPassword(password, salt);
            
            // Generate random IV
            byte[] iv = generateRandomBytes(GCM_IV_LENGTH);
            
            // Initialize cipher
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
            
            // Encrypt the plaintext
            byte[] encryptedData = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            
            // Combine salt + IV + encrypted data
            byte[] result = new byte[salt.length + iv.length + encryptedData.length];
            System.arraycopy(salt, 0, result, 0, salt.length);
            System.arraycopy(iv, 0, result, salt.length, iv.length);
            System.arraycopy(encryptedData, 0, result, salt.length + iv.length, encryptedData.length);
            
            // Return Base64 encoded result
            return Base64.getEncoder().encodeToString(result);
            
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Decrypts Base64-encoded encrypted data using AES-GCM.
     * 
     * @param encryptedData Base64-encoded encrypted data with salt and IV
     * @param password The master password
     * @return Decrypted plaintext
     */
    public static String decrypt(String encryptedData, String password) {
        try {
            // Decode Base64
            byte[] data = Base64.getDecoder().decode(encryptedData);
            
            // Extract salt, IV, and encrypted data
            byte[] salt = new byte[SALT_LENGTH];
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encrypted = new byte[data.length - SALT_LENGTH - GCM_IV_LENGTH];
            
            System.arraycopy(data, 0, salt, 0, SALT_LENGTH);
            System.arraycopy(data, SALT_LENGTH, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(data, SALT_LENGTH + GCM_IV_LENGTH, encrypted, 0, encrypted.length);
            
            // Derive key from password and salt
            SecretKey secretKey = deriveKeyFromPassword(password, salt);
            
            // Initialize cipher for decryption
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
            
            // Decrypt the data
            byte[] decryptedData = cipher.doFinal(encrypted);
            
            return new String(decryptedData, StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Derives a cryptographic key from a password using PBKDF2.
     * 
     * @param password The password
     * @param salt Random salt
     * @return SecretKey for AES encryption
     */
    private static SecretKey deriveKeyFromPassword(String password, byte[] salt) {
        try {
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM);
            byte[] key = factory.generateSecret(spec).getEncoded();
            return new SecretKeySpec(key, ALGORITHM);
        } catch (Exception e) {
            throw new RuntimeException("Key derivation failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Generates cryptographically secure random bytes.
     * 
     * @param length Number of bytes to generate
     * @return Random byte array
     */
    private static byte[] generateRandomBytes(int length) {
        byte[] bytes = new byte[length];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }
    
    /**
     * Generates a random AES key (for testing purposes).
     * 
     * @return Random SecretKey
     */
    public static SecretKey generateRandomKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(KEY_LENGTH);
            return keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Key generation failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Validates if encrypted data can be decrypted with the given password.
     * Used for master password verification.
     * 
     * @param encryptedData The encrypted data to test
     * @param password The password to test
     * @return true if password is correct, false otherwise
     */
    public static boolean validatePassword(String encryptedData, String password) {
        try {
            decrypt(encryptedData, password);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Creates a test encrypted string for password validation.
     * This is stored with the credentials to verify the master password.
     * 
     * @param password The master password
     * @return Encrypted validation string
     */
    public static String createPasswordValidator(String password) {
        return encrypt("SECUREVAULT_PASSWORD_VALIDATOR", password);
    }
}
