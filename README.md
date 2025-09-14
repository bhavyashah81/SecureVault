# SecureVault

A comprehensive password manager that consolidates secure credential storage, advanced password generation, and intuitive management features into a single, user-friendly CLI application for individuals and professionals.

## Features

**Secure Credential Storage**: Store unlimited passwords with military-grade AES-256-GCM encryption, PBKDF2 key derivation with 100,000 iterations, and automatic timestamped backups

**Advanced Password Generation**: Create strong passwords with customizable rules including length, character types, minimum requirements, and real-time strength evaluation

**Intelligent Search & Management**: Find credentials instantly with advanced search across websites, usernames, and notes, plus full CRUD operations with duplicate detection

**Enhanced Security Features**: Master password protection, password validation to prevent corruption, secure clipboard integration with auto-clear, and encrypted data export capabilities

**Cross-Platform Compatibility**: Native support for Windows, macOS, and Linux with consistent CLI experience across all platforms

**Metadata Tracking**: Automatic tracking of credential creation and modification dates with optional notes support for better organization

## Technologies and Languages Used

**Languages**: Java, Shell Script
**Build System**: Maven
**Encryption**: Java Cryptography Architecture (JCA), AES-256-GCM
**Testing**: JUnit 5

## How to Run the Project

```bash
./build.sh
```

This will:
- Validate Java 17+ and Maven installation
- Compile all source files and run tests
- Package the application into an executable JAR
- Launch the SecureVault CLI interface
- Load existing encrypted credentials (if any)
- Enable secure credential management with master password protection

## Dependencies

Install required packages:

```bash
mvn clean install
```

Required packages:
- java >= 17.0.0
- maven >= 3.6.0
- junit-jupiter-engine >= 5.10.0
- junit-jupiter-api >= 5.10.0

## Usage Guide

### First Time Setup
1. **Enter Master Password**: When you first run it, create a strong master password
   - This password encrypts ALL your stored credentials
   - Remember it well - there's no recovery option!

### Main Menu Options

#### 1. Add New Credential
- Enter website/service name (e.g., "gmail.com")
- Enter username/email
- Choose to generate a strong password OR enter your own
- Optionally add notes

#### 2. List All Credentials  
- Shows all stored credentials
- Choose whether to display passwords or hide them

#### 3. Search Credentials
- Search by website name, username, or notes
- Case-insensitive matching

#### 4. Update Credential
- Modify existing credentials
- Can change username, password, or notes

#### 5. Delete Credential
- Remove credentials you no longer need

#### 6. Copy Password to Clipboard
- **This is the killer feature!**
- Enter website name
- Password gets copied to clipboard
- **Automatically clears after 30 seconds** for security

#### 7. Generate New Password
- Create strong passwords with custom rules:
  - Length (8-128 characters)
  - Include/exclude uppercase, numbers, symbols
  - Exclude similar characters (like 1, l, I, 0, O)
- Shows password strength score (0-100)
- Can copy directly to clipboard

#### 8. Change Master Password
- Update your master password
- Re-encrypts all data with new password

#### 9. Export Credentials
- Backup your data to a text file
- Choose to include passwords or not


#### 10. Save and Exit
- Encrypts and saves all changes
- Must enter master password to save

### Pro Tips

- **Daily Usage**: Use "Copy Password to Clipboard" (option 6) for quick access - just type the website name and paste
- **Strong Passwords**: Generate 16+ character passwords with all character types for maximum security
- **Smart Search**: Search works across all fields (website, username, notes) to find accounts easily
- **Security**: All passwords auto-clear from clipboard after 30 seconds

### Example Workflow
1. Run app, enter master password
2. Add credential: "github.com" / "myusername" 
3. Generate 20-character password with all options
4. Later: Use "Copy to clipboard" to log in
5. Password auto-clears from clipboard after 30s
6. Save and exit when done

The app is designed for security-conscious users who want strong, unique passwords for every account without the hassle of remembering them all!