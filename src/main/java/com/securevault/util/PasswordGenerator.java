package com.securevault.util;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Advanced password generator with customizable rules and enhanced security.
 * Improved version of the original C++ password generator.
 */
public class PasswordGenerator {
    
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()_+-=[]{}|;:,.<>?";
    private static final String SIMILAR_CHARS = "il1Lo0O";
    
    private static final SecureRandom random = new SecureRandom();
    
    /**
     * Configuration class for password generation options.
     */
    public static class PasswordConfig {
        private int length = 12;
        private boolean includeUppercase = true;
        private boolean includeLowercase = true;
        private boolean includeDigits = true;
        private boolean includeSymbols = true;
        private boolean excludeSimilarChars = false;
        private int minUppercase = 1;
        private int minLowercase = 1;
        private int minDigits = 1;
        private int minSymbols = 1;
        
        // Getters and setters
        public int getLength() { return length; }
        public PasswordConfig setLength(int length) { this.length = Math.max(4, length); return this; }
        
        public boolean isIncludeUppercase() { return includeUppercase; }
        public PasswordConfig setIncludeUppercase(boolean includeUppercase) { this.includeUppercase = includeUppercase; return this; }
        
        public boolean isIncludeLowercase() { return includeLowercase; }
        public PasswordConfig setIncludeLowercase(boolean includeLowercase) { this.includeLowercase = includeLowercase; return this; }
        
        public boolean isIncludeDigits() { return includeDigits; }
        public PasswordConfig setIncludeDigits(boolean includeDigits) { this.includeDigits = includeDigits; return this; }
        
        public boolean isIncludeSymbols() { return includeSymbols; }
        public PasswordConfig setIncludeSymbols(boolean includeSymbols) { this.includeSymbols = includeSymbols; return this; }
        
        public boolean isExcludeSimilarChars() { return excludeSimilarChars; }
        public PasswordConfig setExcludeSimilarChars(boolean excludeSimilarChars) { this.excludeSimilarChars = excludeSimilarChars; return this; }
        
        public int getMinUppercase() { return minUppercase; }
        public PasswordConfig setMinUppercase(int minUppercase) { this.minUppercase = Math.max(0, minUppercase); return this; }
        
        public int getMinLowercase() { return minLowercase; }
        public PasswordConfig setMinLowercase(int minLowercase) { this.minLowercase = Math.max(0, minLowercase); return this; }
        
        public int getMinDigits() { return minDigits; }
        public PasswordConfig setMinDigits(int minDigits) { this.minDigits = Math.max(0, minDigits); return this; }
        
        public int getMinSymbols() { return minSymbols; }
        public PasswordConfig setMinSymbols(int minSymbols) { this.minSymbols = Math.max(0, minSymbols); return this; }
    }
    
    /**
     * Generates a password with the given configuration.
     * 
     * @param config Password generation configuration
     * @return Generated password
     */
    public static String generatePassword(PasswordConfig config) {
        if (config == null) {
            config = new PasswordConfig();
        }
        
        // Build character set
        StringBuilder charsetBuilder = new StringBuilder();
        List<Character> requiredChars = new ArrayList<>();
        
        if (config.isIncludeLowercase()) {
            String chars = config.isExcludeSimilarChars() ? removeSimilarChars(LOWERCASE) : LOWERCASE;
            charsetBuilder.append(chars);
            addRequiredChars(requiredChars, chars, config.getMinLowercase());
        }
        
        if (config.isIncludeUppercase()) {
            String chars = config.isExcludeSimilarChars() ? removeSimilarChars(UPPERCASE) : UPPERCASE;
            charsetBuilder.append(chars);
            addRequiredChars(requiredChars, chars, config.getMinUppercase());
        }
        
        if (config.isIncludeDigits()) {
            String chars = config.isExcludeSimilarChars() ? removeSimilarChars(DIGITS) : DIGITS;
            charsetBuilder.append(chars);
            addRequiredChars(requiredChars, chars, config.getMinDigits());
        }
        
        if (config.isIncludeSymbols()) {
            charsetBuilder.append(SYMBOLS);
            addRequiredChars(requiredChars, SYMBOLS, config.getMinSymbols());
        }
        
        String charset = charsetBuilder.toString();
        if (charset.isEmpty()) {
            throw new IllegalArgumentException("At least one character type must be included");
        }
        
        // Check if requirements can be met
        int totalRequired = requiredChars.size();
        if (totalRequired > config.getLength()) {
            throw new IllegalArgumentException("Password length is too short for the specified requirements");
        }
        
        // Generate password
        List<Character> passwordChars = new ArrayList<>(requiredChars);
        
        // Fill remaining positions with random characters
        for (int i = totalRequired; i < config.getLength(); i++) {
            passwordChars.add(charset.charAt(random.nextInt(charset.length())));
        }
        
        // Shuffle the password to avoid predictable patterns
        Collections.shuffle(passwordChars, random);
        
        // Convert to string
        StringBuilder password = new StringBuilder();
        for (char c : passwordChars) {
            password.append(c);
        }
        
        return password.toString();
    }
    
    /**
     * Generates a password with simple options (compatible with original interface).
     * 
     * @param length Password length
     * @param useSymbols Include symbols
     * @param useNumbers Include numbers
     * @param useUppercase Include uppercase letters
     * @return Generated password
     */
    public static String generatePassword(int length, boolean useSymbols, boolean useNumbers, boolean useUppercase) {
        PasswordConfig config = new PasswordConfig()
                .setLength(length)
                .setIncludeSymbols(useSymbols)
                .setIncludeDigits(useNumbers)
                .setIncludeUppercase(useUppercase)
                .setIncludeLowercase(true) // Always include lowercase
                .setMinUppercase(useUppercase ? 1 : 0)
                .setMinDigits(useNumbers ? 1 : 0)
                .setMinSymbols(useSymbols ? 1 : 0)
                .setMinLowercase(1);
        
        return generatePassword(config);
    }
    
    /**
     * Generates a strong password with default settings.
     * 
     * @param length Password length
     * @return Generated strong password
     */
    public static String generateStrongPassword(int length) {
        return generatePassword(new PasswordConfig().setLength(length));
    }
    
    /**
     * Generates a memorable password using word-like patterns.
     * 
     * @param length Approximate password length
     * @return Generated memorable password
     */
    public static String generateMemorablePassword(int length) {
        String[] consonants = {"b", "c", "d", "f", "g", "h", "j", "k", "l", "m", "n", "p", "q", "r", "s", "t", "v", "w", "x", "z"};
        String[] vowels = {"a", "e", "i", "o", "u", "y"};
        
        StringBuilder password = new StringBuilder();
        boolean useConsonant = random.nextBoolean();
        
        while (password.length() < length - 2) {
            if (useConsonant) {
                String consonant = consonants[random.nextInt(consonants.length)];
                password.append(random.nextBoolean() ? consonant.toUpperCase() : consonant);
            } else {
                password.append(vowels[random.nextInt(vowels.length)]);
            }
            useConsonant = !useConsonant;
        }
        
        // Add numbers and symbols to meet length
        while (password.length() < length) {
            if (random.nextBoolean()) {
                password.append(random.nextInt(10));
            } else {
                password.append(SYMBOLS.charAt(random.nextInt(SYMBOLS.length())));
            }
        }
        
        return password.toString();
    }
    
    /**
     * Evaluates password strength.
     * 
     * @param password Password to evaluate
     * @return Strength score (0-100)
     */
    public static int evaluatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }
        
        int score = 0;
        int length = password.length();
        
        // Length score
        score += Math.min(length * 2, 25);
        
        // Character variety
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSymbol = password.chars().anyMatch(c -> SYMBOLS.indexOf(c) >= 0);
        
        int variety = (hasLower ? 1 : 0) + (hasUpper ? 1 : 0) + (hasDigit ? 1 : 0) + (hasSymbol ? 1 : 0);
        score += variety * 10;
        
        // Bonus for length
        if (length >= 12) score += 10;
        if (length >= 16) score += 10;
        
        // Penalty for common patterns
        if (password.matches(".*123.*|.*abc.*|.*qwe.*")) score -= 10;
        if (password.toLowerCase().matches(".*password.*|.*123456.*")) score -= 20;
        
        return Math.max(0, Math.min(100, score));
    }
    
    /**
     * Gets a description of password strength.
     * 
     * @param score Strength score from evaluatePasswordStrength
     * @return Strength description
     */
    public static String getStrengthDescription(int score) {
        if (score < 30) return "Very Weak";
        if (score < 50) return "Weak";
        if (score < 70) return "Fair";
        if (score < 85) return "Strong";
        return "Very Strong";
    }
    
    private static void addRequiredChars(List<Character> requiredChars, String charset, int count) {
        for (int i = 0; i < count; i++) {
            requiredChars.add(charset.charAt(random.nextInt(charset.length())));
        }
    }
    
    private static String removeSimilarChars(String charset) {
        StringBuilder result = new StringBuilder();
        for (char c : charset.toCharArray()) {
            if (SIMILAR_CHARS.indexOf(c) == -1) {
                result.append(c);
            }
        }
        return result.toString();
    }
}
