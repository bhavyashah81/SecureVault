package com.securevault.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a credential entry with website, username, password, and metadata.
 * Enhanced version of the original C++ Credential struct with additional features.
 */
public class Credential {
    private String website;
    private String username;
    private String password;
    private LocalDateTime createdAt;
    private LocalDateTime lastModified;
    private String notes;
    
    // Constructors
    public Credential() {
        this.createdAt = LocalDateTime.now();
        this.lastModified = LocalDateTime.now();
    }
    
    public Credential(String website, String username, String password) {
        this();
        this.website = website;
        this.username = username;
        this.password = password;
    }
    
    public Credential(String website, String username, String password, String notes) {
        this(website, username, password);
        this.notes = notes;
    }
    
    // Getters and Setters
    public String getWebsite() {
        return website;
    }
    
    public void setWebsite(String website) {
        this.website = website;
        updateLastModified();
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
        updateLastModified();
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
        updateLastModified();
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getLastModified() {
        return lastModified;
    }
    
    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
        updateLastModified();
    }
    
    // Utility methods
    private void updateLastModified() {
        this.lastModified = LocalDateTime.now();
    }
    
    public String getMaskedPassword() {
        return "*".repeat(password != null ? password.length() : 0);
    }
    
    public boolean matches(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return true;
        }
        
        String term = searchTerm.toLowerCase();
        return (website != null && website.toLowerCase().contains(term)) ||
               (username != null && username.toLowerCase().contains(term)) ||
               (notes != null && notes.toLowerCase().contains(term));
    }
    
    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return String.format("Website: %s | Username: %s | Created: %s | Modified: %s%s",
                website != null ? website : "N/A",
                username != null ? username : "N/A",
                createdAt != null ? createdAt.format(formatter) : "N/A",
                lastModified != null ? lastModified.format(formatter) : "N/A",
                notes != null && !notes.trim().isEmpty() ? " | Notes: " + notes : "");
    }
    
    public String toDisplayString(boolean showPassword) {
        return String.format("Website: %s | Username: %s | Password: %s",
                website != null ? website : "N/A",
                username != null ? username : "N/A",
                showPassword ? (password != null ? password : "N/A") : getMaskedPassword());
    }
    
    // For serialization (simple format compatible with original)
    public String serialize() {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return String.format("%s|%s|%s|%s|%s|%s",
                website != null ? website : "",
                username != null ? username : "",
                password != null ? password : "",
                createdAt != null ? createdAt.format(formatter) : "",
                lastModified != null ? lastModified.format(formatter) : "",
                notes != null ? notes : "");
    }
    
    public static Credential deserialize(String data) {
        String[] parts = data.split("\\|", 6);
        if (parts.length < 3) {
            return null;
        }
        
        Credential credential = new Credential();
        credential.website = parts[0].isEmpty() ? null : parts[0];
        credential.username = parts[1].isEmpty() ? null : parts[1];
        credential.password = parts[2].isEmpty() ? null : parts[2];
        
        if (parts.length > 3 && !parts[3].isEmpty()) {
            try {
                credential.createdAt = LocalDateTime.parse(parts[3]);
            } catch (Exception e) {
                credential.createdAt = LocalDateTime.now();
            }
        }
        
        if (parts.length > 4 && !parts[4].isEmpty()) {
            try {
                credential.lastModified = LocalDateTime.parse(parts[4]);
            } catch (Exception e) {
                credential.lastModified = LocalDateTime.now();
            }
        }
        
        if (parts.length > 5) {
            credential.notes = parts[5].isEmpty() ? null : parts[5];
        }
        
        return credential;
    }
}
