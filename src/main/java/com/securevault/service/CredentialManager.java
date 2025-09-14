package com.securevault.service;

import com.securevault.model.Credential;
import com.securevault.util.EncryptionUtil;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Enhanced credential manager with secure storage and advanced features.
 * Improved version of the original C++ CredentialManager class.
 */
public class CredentialManager {
    
    private static final String DATA_FILE = "securevault.enc";
    private static final String BACKUP_DIR = "backups";
    private static final String PASSWORD_VALIDATOR_PREFIX = "VALIDATOR:";
    
    private List<Credential> credentials;
    private String masterPasswordHash;
    private boolean isLoaded;
    
    public CredentialManager() {
        this.credentials = new ArrayList<>();
        this.isLoaded = false;
    }
    
    /**
     * Loads credentials from encrypted file using the master password.
     * 
     * @param masterPassword The master password
     * @return true if successful, false if wrong password or file doesn't exist
     */
    public boolean load(String masterPassword) {
        Path dataFile = Paths.get(DATA_FILE);
        
        if (!Files.exists(dataFile)) {
            // First time setup - create password validator
            this.masterPasswordHash = EncryptionUtil.createPasswordValidator(masterPassword);
            this.credentials = new ArrayList<>();
            this.isLoaded = true;
            return true;
        }
        
        try {
            String encryptedData = Files.readString(dataFile);
            String decryptedData = EncryptionUtil.decrypt(encryptedData, masterPassword);
            
            // Parse the decrypted data
            parseCredentialsData(decryptedData);
            
            this.isLoaded = true;
            return true;
            
        } catch (Exception e) {
            System.err.println("Failed to load credentials. Wrong password or corrupted file.");
            return false;
        }
    }
    
    /**
     * Saves credentials to encrypted file.
     * 
     * @param masterPassword The master password
     * @return true if successful, false otherwise
     */
    public boolean save(String masterPassword) {
        if (!isLoaded) {
            return false;
        }
        
        try {
            // Create backup first
            createBackup();
            
            // Serialize credentials
            String data = serializeCredentials(masterPassword);
            
            // Encrypt and save
            String encryptedData = EncryptionUtil.encrypt(data, masterPassword);
            Files.writeString(Paths.get(DATA_FILE), encryptedData);
            
            return true;
            
        } catch (Exception e) {
            System.err.println("Failed to save credentials: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Adds a new credential.
     * 
     * @param website Website/service name
     * @param username Username
     * @param password Password
     */
    public void addCredential(String website, String username, String password) {
        addCredential(website, username, password, null);
    }
    
    /**
     * Adds a new credential with notes.
     * 
     * @param website Website/service name
     * @param username Username
     * @param password Password
     * @param notes Optional notes
     */
    public void addCredential(String website, String username, String password, String notes) {
        if (!isLoaded) {
            throw new IllegalStateException("Credentials not loaded");
        }
        
        Credential credential = new Credential(website, username, password, notes);
        credentials.add(credential);
    }
    
    /**
     * Gets all credentials.
     * 
     * @return List of all credentials
     */
    public List<Credential> getAllCredentials() {
        if (!isLoaded) {
            throw new IllegalStateException("Credentials not loaded");
        }
        
        return new ArrayList<>(credentials);
    }
    
    /**
     * Finds credentials by website name.
     * 
     * @param website Website to search for
     * @return Optional containing the credential if found
     */
    public Optional<Credential> findByWebsite(String website) {
        if (!isLoaded) {
            throw new IllegalStateException("Credentials not loaded");
        }
        
        return credentials.stream()
                .filter(c -> website.equalsIgnoreCase(c.getWebsite()))
                .findFirst();
    }
    
    /**
     * Searches credentials by term (website, username, or notes).
     * 
     * @param searchTerm The search term
     * @return List of matching credentials
     */
    public List<Credential> searchCredentials(String searchTerm) {
        if (!isLoaded) {
            throw new IllegalStateException("Credentials not loaded");
        }
        
        return credentials.stream()
                .filter(c -> c.matches(searchTerm))
                .collect(Collectors.toList());
    }
    
    /**
     * Updates an existing credential.
     * 
     * @param website Website to update
     * @param newUsername New username (null to keep existing)
     * @param newPassword New password (null to keep existing)
     * @param newNotes New notes (null to keep existing)
     * @return true if credential was found and updated, false otherwise
     */
    public boolean updateCredential(String website, String newUsername, String newPassword, String newNotes) {
        Optional<Credential> credentialOpt = findByWebsite(website);
        
        if (credentialOpt.isPresent()) {
            Credential credential = credentialOpt.get();
            
            if (newUsername != null) {
                credential.setUsername(newUsername);
            }
            if (newPassword != null) {
                credential.setPassword(newPassword);
            }
            if (newNotes != null) {
                credential.setNotes(newNotes);
            }
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Removes a credential by website name.
     * 
     * @param website Website to remove
     * @return true if credential was found and removed, false otherwise
     */
    public boolean removeCredential(String website) {
        if (!isLoaded) {
            throw new IllegalStateException("Credentials not loaded");
        }
        
        return credentials.removeIf(c -> website.equalsIgnoreCase(c.getWebsite()));
    }
    
    /**
     * Gets the number of stored credentials.
     * 
     * @return Number of credentials
     */
    public int getCredentialCount() {
        return isLoaded ? credentials.size() : 0;
    }
    
    /**
     * Checks if credentials are loaded.
     * 
     * @return true if loaded, false otherwise
     */
    public boolean isLoaded() {
        return isLoaded;
    }
    
    /**
     * Changes the master password.
     * 
     * @param currentPassword Current master password
     * @param newPassword New master password
     * @return true if successful, false if current password is wrong
     */
    public boolean changeMasterPassword(String currentPassword, String newPassword) {
        if (!isLoaded) {
            throw new IllegalStateException("Credentials not loaded");
        }
        
        // Verify current password
        if (masterPasswordHash != null && !EncryptionUtil.validatePassword(masterPasswordHash, currentPassword)) {
            return false;
        }
        
        // Save with new password
        this.masterPasswordHash = EncryptionUtil.createPasswordValidator(newPassword);
        return save(newPassword);
    }
    
    /**
     * Exports credentials to a plain text file (for backup purposes).
     * WARNING: This creates an unencrypted file!
     * 
     * @param filePath Path to export file
     * @param includePasswords Whether to include passwords in export
     * @return true if successful, false otherwise
     */
    public boolean exportToFile(String filePath, boolean includePasswords) {
        if (!isLoaded) {
            throw new IllegalStateException("Credentials not loaded");
        }
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("SecureVault Credential Export");
            writer.println("Generated: " + java.time.LocalDateTime.now());
            writer.println("Total Credentials: " + credentials.size());
            writer.println("Passwords Included: " + includePasswords);
            writer.println("=" .repeat(50));
            writer.println();
            
            for (Credential credential : credentials) {
                writer.println("Website: " + (credential.getWebsite() != null ? credential.getWebsite() : "N/A"));
                writer.println("Username: " + (credential.getUsername() != null ? credential.getUsername() : "N/A"));
                
                if (includePasswords) {
                    writer.println("Password: " + (credential.getPassword() != null ? credential.getPassword() : "N/A"));
                } else {
                    writer.println("Password: [HIDDEN]");
                }
                
                writer.println("Created: " + credential.getCreatedAt());
                writer.println("Modified: " + credential.getLastModified());
                
                if (credential.getNotes() != null && !credential.getNotes().trim().isEmpty()) {
                    writer.println("Notes: " + credential.getNotes());
                }
                
                writer.println("-".repeat(30));
            }
            
            return true;
            
        } catch (IOException e) {
            System.err.println("Failed to export credentials: " + e.getMessage());
            return false;
        }
    }
    
    private void parseCredentialsData(String data) {
        this.credentials = new ArrayList<>();
        
        String[] lines = data.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            
            if (line.startsWith(PASSWORD_VALIDATOR_PREFIX)) {
                this.masterPasswordHash = line.substring(PASSWORD_VALIDATOR_PREFIX.length());
            } else {
                Credential credential = Credential.deserialize(line);
                if (credential != null) {
                    credentials.add(credential);
                }
            }
        }
    }
    
    private String serializeCredentials(String masterPassword) {
        StringBuilder sb = new StringBuilder();
        
        // Add password validator
        if (masterPasswordHash == null) {
            masterPasswordHash = EncryptionUtil.createPasswordValidator(masterPassword);
        }
        sb.append(PASSWORD_VALIDATOR_PREFIX).append(masterPasswordHash).append("\n");
        
        // Add credentials
        for (Credential credential : credentials) {
            sb.append(credential.serialize()).append("\n");
        }
        
        return sb.toString();
    }
    
    private void createBackup() {
        try {
            Path backupDir = Paths.get(BACKUP_DIR);
            if (!Files.exists(backupDir)) {
                Files.createDirectories(backupDir);
            }
            
            Path dataFile = Paths.get(DATA_FILE);
            if (Files.exists(dataFile)) {
                String timestamp = java.time.LocalDateTime.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                Path backupFile = backupDir.resolve("securevault_" + timestamp + ".enc");
                Files.copy(dataFile, backupFile);
            }
        } catch (Exception e) {
            System.err.println("Failed to create backup: " + e.getMessage());
        }
    }
}
