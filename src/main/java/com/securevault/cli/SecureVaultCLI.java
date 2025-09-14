package com.securevault.cli;

import com.securevault.model.Credential;
import com.securevault.service.CredentialManager;
import com.securevault.util.ClipboardUtil;
import com.securevault.util.InputUtil;
import com.securevault.util.PasswordGenerator;

import java.util.List;
import java.util.Optional;

/**
 * Main CLI interface for SecureVault password manager.
 * Enhanced version of the original C++ main application.
 */
public class SecureVaultCLI {
    
    private static final String APP_NAME = "SecureVault";
    private static final String VERSION = "1.0.0";
    private static final int CLIPBOARD_CLEAR_DELAY = 30; // seconds
    
    private final CredentialManager credentialManager;
    private boolean running;
    
    public SecureVaultCLI() {
        this.credentialManager = new CredentialManager();
        this.running = true;
    }
    
    /**
     * Main entry point for the application.
     */
    public static void main(String[] args) {
        System.out.println("Welcome to " + APP_NAME + " v" + VERSION + " 🔐");
        System.out.println("A secure password manager for your digital life.");
        System.out.println("=" .repeat(50));
        
        SecureVaultCLI app = new SecureVaultCLI();
        
        // Setup shutdown hook for cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down SecureVault...");
            ClipboardUtil.shutdown();
        }));
        
        app.run();
    }
    
    /**
     * Main application loop.
     */
    public void run() {
        // Load credentials
        if (!loadCredentials()) {
            System.out.println("Exiting...");
            return;
        }
        
        System.out.println("\nCredentials loaded successfully!");
        System.out.println("Total credentials: " + credentialManager.getCredentialCount());
        
        // Main menu loop
        while (running) {
            try {
                showMainMenu();
            } catch (Exception e) {
                System.err.println("An error occurred: " + e.getMessage());
                if (InputUtil.getYesNoInput("Continue running")) {
                    continue;
                } else {
                    break;
                }
            }
        }
        
        // Save before exit
        saveAndExit();
    }
    
    private boolean loadCredentials() {
        int maxAttempts = 3;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            String masterPassword = InputUtil.getMaskedInput("Enter master password: ");
            
            if (credentialManager.load(masterPassword)) {
                return true;
            }
            
            System.out.printf("Invalid password. Attempt %d of %d.%n", attempt, maxAttempts);
            
            if (attempt < maxAttempts) {
                System.out.println("Please try again.");
            }
        }
        
        System.out.println("Maximum attempts exceeded. Access denied.");
        return false;
    }
    
    private void showMainMenu() {
        int choice = InputUtil.getMenuChoice(
                "SecureVault Main Menu",
                "Add New Credential",
                "List All Credentials",
                "Search Credentials",
                "Update Credential",
                "Delete Credential",
                "Copy Password to Clipboard",
                "Generate New Password",
                "Change Master Password",
                "Export Credentials",
                "Save and Exit"
        );
        
        switch (choice) {
            case 0 -> addCredential();
            case 1 -> listCredentials();
            case 2 -> searchCredentials();
            case 3 -> updateCredential();
            case 4 -> deleteCredential();
            case 5 -> copyPasswordToClipboard();
            case 6 -> generatePassword();
            case 7 -> changeMasterPassword();
            case 8 -> exportCredentials();
            case 9 -> { running = false; }
        }
    }
    
    private void addCredential() {
        System.out.println("\n--- Add New Credential ---");
        
        String website = InputUtil.getNonBlankInput("Website/Service: ");
        String username = InputUtil.getNonBlankInput("Username: ");
        
        // Check if credential already exists
        Optional<Credential> existing = credentialManager.findByWebsite(website);
        if (existing.isPresent()) {
            System.out.println("Warning: A credential for this website already exists!");
            if (!InputUtil.getYesNoInput("Continue anyway")) {
                return;
            }
        }
        
        String password;
        if (InputUtil.getYesNoInput("Generate a strong password")) {
            password = generatePasswordInteractive();
            System.out.println("Generated password: " + password);
        } else {
            password = InputUtil.getMaskedInput("Password: ");
        }
        
        String notes = InputUtil.getOptionalInput("Notes (optional): ");
        
        credentialManager.addCredential(website, username, password, notes);
        System.out.println("✓ Credential added successfully!");
    }
    
    private void listCredentials() {
        System.out.println("\n--- All Credentials ---");
        
        List<Credential> credentials = credentialManager.getAllCredentials();
        if (credentials.isEmpty()) {
            System.out.println("No credentials stored.");
            return;
        }
        
        boolean showPasswords = InputUtil.getYesNoInput("Show passwords");
        
        System.out.println();
        for (int i = 0; i < credentials.size(); i++) {
            Credential credential = credentials.get(i);
            System.out.printf("%d. %s%n", i + 1, credential.toDisplayString(showPasswords));
            
            if (credential.getNotes() != null && !credential.getNotes().trim().isEmpty()) {
                System.out.println("   Notes: " + credential.getNotes());
            }
            
            System.out.println("   Created: " + credential.getCreatedAt());
            System.out.println();
        }
    }
    
    private void searchCredentials() {
        System.out.println("\n--- Search Credentials ---");
        
        String searchTerm = InputUtil.getInput("Search term: ");
        List<Credential> results = credentialManager.searchCredentials(searchTerm);
        
        if (results.isEmpty()) {
            System.out.println("No credentials found matching '" + searchTerm + "'");
            return;
        }
        
        System.out.printf("Found %d credential(s):%n%n", results.size());
        boolean showPasswords = InputUtil.getYesNoInput("Show passwords");
        
        for (int i = 0; i < results.size(); i++) {
            Credential credential = results.get(i);
            System.out.printf("%d. %s%n", i + 1, credential.toDisplayString(showPasswords));
            
            if (credential.getNotes() != null && !credential.getNotes().trim().isEmpty()) {
                System.out.println("   Notes: " + credential.getNotes());
            }
            System.out.println();
        }
    }
    
    private void updateCredential() {
        System.out.println("\n--- Update Credential ---");
        
        String website = InputUtil.getNonBlankInput("Website to update: ");
        Optional<Credential> credentialOpt = credentialManager.findByWebsite(website);
        
        if (credentialOpt.isEmpty()) {
            System.out.println("No credential found for '" + website + "'");
            return;
        }
        
        Credential credential = credentialOpt.get();
        System.out.println("Current credential:");
        System.out.println(credential.toDisplayString(false));
        
        System.out.println("\nEnter new values (press Enter to keep current):");
        
        String newUsername = InputUtil.getOptionalInput("New username [" + credential.getUsername() + "]: ");
        String newPassword = null;
        
        if (InputUtil.getYesNoInput("Change password")) {
            if (InputUtil.getYesNoInput("Generate new password")) {
                newPassword = generatePasswordInteractive();
                System.out.println("Generated password: " + newPassword);
            } else {
                newPassword = InputUtil.getMaskedInput("New password: ");
            }
        }
        
        String newNotes = InputUtil.getOptionalInput("New notes [" + (credential.getNotes() != null ? credential.getNotes() : "") + "]: ");
        
        if (credentialManager.updateCredential(website, newUsername, newPassword, newNotes)) {
            System.out.println("✓ Credential updated successfully!");
        } else {
            System.out.println("✗ Failed to update credential.");
        }
    }
    
    private void deleteCredential() {
        System.out.println("\n--- Delete Credential ---");
        
        String website = InputUtil.getNonBlankInput("Website to delete: ");
        Optional<Credential> credentialOpt = credentialManager.findByWebsite(website);
        
        if (credentialOpt.isEmpty()) {
            System.out.println("No credential found for '" + website + "'");
            return;
        }
        
        Credential credential = credentialOpt.get();
        System.out.println("Credential to delete:");
        System.out.println(credential.toDisplayString(false));
        
        if (InputUtil.getYesNoInput("Are you sure you want to delete this credential")) {
            if (credentialManager.removeCredential(website)) {
                System.out.println("✓ Credential deleted successfully!");
            } else {
                System.out.println("✗ Failed to delete credential.");
            }
        }
    }
    
    private void copyPasswordToClipboard() {
        System.out.println("\n--- Copy Password to Clipboard ---");
        
        String website = InputUtil.getNonBlankInput("Website: ");
        Optional<Credential> credentialOpt = credentialManager.findByWebsite(website);
        
        if (credentialOpt.isEmpty()) {
            System.out.println("No credential found for '" + website + "'");
            return;
        }
        
        Credential credential = credentialOpt.get();
        String password = credential.getPassword();
        
        if (ClipboardUtil.copyToClipboardWithAutoClear(password, CLIPBOARD_CLEAR_DELAY)) {
            System.out.printf("✓ Password copied to clipboard. It will be cleared in %d seconds.%n", CLIPBOARD_CLEAR_DELAY);
        } else {
            System.out.println("✗ Failed to copy password to clipboard.");
            System.out.println("Password: " + password);
        }
    }
    
    private void generatePassword() {
        System.out.println("\n--- Generate New Password ---");
        
        String password = generatePasswordInteractive();
        System.out.println("Generated password: " + password);
        
        int strength = PasswordGenerator.evaluatePasswordStrength(password);
        System.out.printf("Password strength: %d/100 (%s)%n", strength, PasswordGenerator.getStrengthDescription(strength));
        
        if (InputUtil.getYesNoInput("Copy to clipboard")) {
            if (ClipboardUtil.copyToClipboardWithAutoClear(password, CLIPBOARD_CLEAR_DELAY)) {
                System.out.printf("✓ Password copied to clipboard. It will be cleared in %d seconds.%n", CLIPBOARD_CLEAR_DELAY);
            } else {
                System.out.println("✗ Failed to copy to clipboard.");
            }
        }
    }
    
    private String generatePasswordInteractive() {
        int length = InputUtil.getIntInput("Password length (8-128): ", 8, 128);
        boolean includeUppercase = InputUtil.getYesNoInput("Include uppercase letters");
        boolean includeDigits = InputUtil.getYesNoInput("Include numbers");
        boolean includeSymbols = InputUtil.getYesNoInput("Include symbols");
        
        return PasswordGenerator.generatePassword(length, includeSymbols, includeDigits, includeUppercase);
    }
    
    private void changeMasterPassword() {
        System.out.println("\n--- Change Master Password ---");
        
        String currentPassword = InputUtil.getMaskedInput("Current master password: ");
        String newPassword = InputUtil.getMaskedInput("New master password: ");
        String confirmPassword = InputUtil.getMaskedInput("Confirm new password: ");
        
        if (!newPassword.equals(confirmPassword)) {
            System.out.println("✗ Passwords do not match!");
            return;
        }
        
        if (credentialManager.changeMasterPassword(currentPassword, newPassword)) {
            System.out.println("✓ Master password changed successfully!");
        } else {
            System.out.println("✗ Failed to change master password. Current password may be incorrect.");
        }
    }
    
    private void exportCredentials() {
        System.out.println("\n--- Export Credentials ---");
        
        String filename = InputUtil.getInput("Export filename (e.g., backup.txt): ");
        if (filename.trim().isEmpty()) {
            filename = "securevault_export_" + java.time.LocalDateTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt";
        }
        
        boolean includePasswords = InputUtil.getYesNoInput("Include passwords in export (WARNING: Unencrypted!)");
        
        if (credentialManager.exportToFile(filename, includePasswords)) {
            System.out.println("✓ Credentials exported to: " + filename);
            if (includePasswords) {
                System.out.println("⚠️  WARNING: This file contains unencrypted passwords. Keep it secure!");
            }
        } else {
            System.out.println("✗ Failed to export credentials.");
        }
    }
    
    private void saveAndExit() {
        System.out.println("\nSaving credentials...");
        
        String masterPassword = InputUtil.getMaskedInput("Enter master password to save: ");
        
        if (credentialManager.save(masterPassword)) {
            System.out.println("✓ Credentials saved successfully!");
        } else {
            System.out.println("✗ Failed to save credentials!");
            if (InputUtil.getYesNoInput("Try again")) {
                saveAndExit();
                return;
            }
        }
        
        System.out.println("Thank you for using SecureVault! 🔐");
    }
}
