package com.securevault.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for PasswordGenerator utility.
 */
public class PasswordGeneratorTest {
    
    @Test
    public void testGeneratePassword() {
        String password = PasswordGenerator.generatePassword(12, true, true, true);
        
        assertNotNull(password);
        assertEquals(12, password.length());
        
        // Test that password contains required character types
        assertTrue(password.chars().anyMatch(Character::isLowerCase), "Should contain lowercase");
        assertTrue(password.chars().anyMatch(Character::isUpperCase), "Should contain uppercase");
        assertTrue(password.chars().anyMatch(Character::isDigit), "Should contain digits");
        assertTrue(password.chars().anyMatch(c -> "!@#$%^&*()_+-=[]{}|;:,.<>?".indexOf(c) >= 0), "Should contain symbols");
    }
    
    @Test
    public void testGeneratePasswordWithoutSymbols() {
        String password = PasswordGenerator.generatePassword(10, false, true, true);
        
        assertNotNull(password);
        assertEquals(10, password.length());
        
        // Should not contain symbols
        assertFalse(password.chars().anyMatch(c -> "!@#$%^&*()_+-=[]{}|;:,.<>?".indexOf(c) >= 0), "Should not contain symbols");
    }
    
    @Test
    public void testPasswordStrengthEvaluation() {
        // Test weak password
        int weakScore = PasswordGenerator.evaluatePasswordStrength("123");
        assertTrue(weakScore < 30, "Short password should be weak");
        
        // Test strong password
        int strongScore = PasswordGenerator.evaluatePasswordStrength("MyStr0ng!P@ssw0rd2024");
        assertTrue(strongScore > 70, "Complex password should be strong");
    }
    
    @Test
    public void testStrengthDescription() {
        assertEquals("Very Weak", PasswordGenerator.getStrengthDescription(20));
        assertEquals("Weak", PasswordGenerator.getStrengthDescription(40));
        assertEquals("Fair", PasswordGenerator.getStrengthDescription(60));
        assertEquals("Strong", PasswordGenerator.getStrengthDescription(80));
        assertEquals("Very Strong", PasswordGenerator.getStrengthDescription(95));
    }
    
    @Test
    public void testPasswordConfig() {
        PasswordGenerator.PasswordConfig config = new PasswordGenerator.PasswordConfig()
                .setLength(16)
                .setIncludeUppercase(true)
                .setIncludeDigits(true)
                .setIncludeSymbols(false)
                .setMinUppercase(2)
                .setMinDigits(2);
        
        String password = PasswordGenerator.generatePassword(config);
        
        assertNotNull(password);
        assertEquals(16, password.length());
        
        long uppercaseCount = password.chars().filter(Character::isUpperCase).count();
        long digitCount = password.chars().filter(Character::isDigit).count();
        
        assertTrue(uppercaseCount >= 2, "Should have at least 2 uppercase letters");
        assertTrue(digitCount >= 2, "Should have at least 2 digits");
    }
}
