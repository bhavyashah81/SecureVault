package com.securevault.util;

import java.io.Console;
import java.io.IOException;
import java.util.Scanner;

/**
 * Utility class for handling user input, including masked password input.
 * Enhanced version of the original C++ input utilities.
 */
public class InputUtil {
    
    private static final Scanner scanner = new Scanner(System.in);
    
    /**
     * Gets masked password input from the user.
     * Uses Console.readPassword() when available, falls back to regular input.
     * 
     * @param prompt The prompt to display
     * @return The entered password
     */
    public static String getMaskedInput(String prompt) {
        Console console = System.console();
        
        if (console != null) {
            // Use Console for proper password masking
            char[] passwordChars = console.readPassword(prompt);
            return new String(passwordChars);
        } else {
            // Fallback for IDEs and environments without Console
            System.out.print(prompt);
            return scanner.nextLine();
        }
    }
    
    /**
     * Gets a line of input from the user.
     * 
     * @param prompt The prompt to display
     * @return The entered text
     */
    public static String getInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }
    
    /**
     * Gets an integer input from the user with validation.
     * 
     * @param prompt The prompt to display
     * @param min Minimum allowed value
     * @param max Maximum allowed value
     * @return The entered integer
     */
    public static int getIntInput(String prompt, int min, int max) {
        while (true) {
            try {
                System.out.print(prompt);
                int value = Integer.parseInt(scanner.nextLine());
                if (value >= min && value <= max) {
                    return value;
                } else {
                    System.out.printf("Please enter a value between %d and %d.%n", min, max);
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }
    
    /**
     * Gets a yes/no confirmation from the user.
     * 
     * @param prompt The prompt to display
     * @return true for yes, false for no
     */
    public static boolean getYesNoInput(String prompt) {
        while (true) {
            String response = getInput(prompt + " (y/n): ").trim().toLowerCase();
            if (response.equals("y") || response.equals("yes")) {
                return true;
            } else if (response.equals("n") || response.equals("no") || response.isEmpty()) {
                return false;
            } else {
                System.out.println("Please enter 'y' for yes or 'n' for no.");
            }
        }
    }
    
    /**
     * Gets non-blank input from the user.
     * 
     * @param prompt The prompt to display
     * @return The entered non-blank text
     */
    public static String getNonBlankInput(String prompt) {
        while (true) {
            String input = getInput(prompt);
            if (!isBlank(input)) {
                return input;
            }
            System.out.println("Input cannot be blank. Please try again.");
        }
    }
    
    /**
     * Gets optional input from the user (can be blank).
     * 
     * @param prompt The prompt to display
     * @return The entered text or null if blank
     */
    public static String getOptionalInput(String prompt) {
        String input = getInput(prompt);
        return isBlank(input) ? null : input;
    }
    
    /**
     * Checks if a string is blank (null, empty, or only whitespace).
     * 
     * @param str The string to check
     * @return true if blank, false otherwise
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * Waits for the user to press Enter.
     * 
     * @param prompt The prompt to display
     */
    public static void waitForEnter(String prompt) {
        System.out.print(prompt);
        scanner.nextLine();
    }
    
    /**
     * Clears the console screen (platform-dependent).
     */
    public static void clearScreen() {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                // Unix/Linux/Mac
                System.out.print("\033[2J\033[H");
                System.out.flush();
            }
        } catch (IOException | InterruptedException e) {
            // If clearing fails, just print some newlines
            System.out.println("\n".repeat(50));
        }
    }
    
    /**
     * Displays a menu and gets the user's choice.
     * 
     * @param title Menu title
     * @param options Menu options
     * @return Selected option index (0-based)
     */
    public static int getMenuChoice(String title, String... options) {
        System.out.println("\n" + title);
        System.out.println("=".repeat(title.length()));
        
        for (int i = 0; i < options.length; i++) {
            System.out.printf("%d. %s%n", i + 1, options[i]);
        }
        
        return getIntInput("\nChoice: ", 1, options.length) - 1;
    }
}
