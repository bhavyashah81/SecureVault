package com.securevault.util;

import java.awt.*;
import java.awt.datatransfer.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for clipboard operations with auto-clear functionality.
 * Enhanced version of the original C++ clipboard utilities.
 */
public class ClipboardUtil {
    
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    /**
     * Copies text to the system clipboard.
     * 
     * @param text The text to copy
     * @return true if successful, false otherwise
     */
    public static boolean copyToClipboard(String text) {
        try {
            if (!GraphicsEnvironment.isHeadless()) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection selection = new StringSelection(text);
                clipboard.setContents(selection, null);
                return true;
            } else {
                // In headless environment, try command line approach
                return copyToClipboardCommandLine(text);
            }
        } catch (Exception e) {
            System.err.println("Failed to copy to clipboard: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Copies text to clipboard and automatically clears it after the specified delay.
     * 
     * @param text The text to copy
     * @param clearAfterSeconds Seconds after which to clear the clipboard
     * @return true if successful, false otherwise
     */
    public static boolean copyToClipboardWithAutoClear(String text, int clearAfterSeconds) {
        if (copyToClipboard(text)) {
            scheduleClipboardClear(clearAfterSeconds);
            return true;
        }
        return false;
    }
    
    /**
     * Clears the system clipboard.
     * 
     * @return true if successful, false otherwise
     */
    public static boolean clearClipboard() {
        return copyToClipboard(" "); // Clear with a space character
    }
    
    /**
     * Gets text from the system clipboard.
     * 
     * @return Clipboard text or null if not available
     */
    public static String getClipboardText() {
        try {
            if (!GraphicsEnvironment.isHeadless()) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                Transferable contents = clipboard.getContents(null);
                
                if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    return (String) contents.getTransferData(DataFlavor.stringFlavor);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to read from clipboard: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Schedules clipboard clearing after the specified delay.
     * 
     * @param seconds Delay in seconds
     */
    private static void scheduleClipboardClear(int seconds) {
        scheduler.schedule(() -> {
            if (clearClipboard()) {
                System.out.println("Clipboard cleared for security.");
            }
        }, seconds, TimeUnit.SECONDS);
    }
    
    /**
     * Copies text to clipboard using command line tools (fallback for headless environments).
     * 
     * @param text The text to copy
     * @return true if successful, false otherwise
     */
    private static boolean copyToClipboardCommandLine(String text) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;
            
            if (os.contains("mac")) {
                // macOS
                pb = new ProcessBuilder("pbcopy");
            } else if (os.contains("linux")) {
                // Linux - try xclip first, then xsel
                if (isCommandAvailable("xclip")) {
                    pb = new ProcessBuilder("xclip", "-selection", "clipboard");
                } else if (isCommandAvailable("xsel")) {
                    pb = new ProcessBuilder("xsel", "--clipboard", "--input");
                } else {
                    return false;
                }
            } else if (os.contains("windows")) {
                // Windows
                pb = new ProcessBuilder("clip");
            } else {
                return false;
            }
            
            Process process = pb.start();
            process.getOutputStream().write(text.getBytes());
            process.getOutputStream().close();
            
            int exitCode = process.waitFor();
            return exitCode == 0;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Checks if a command is available in the system PATH.
     * 
     * @param command The command to check
     * @return true if available, false otherwise
     */
    private static boolean isCommandAvailable(String command) {
        try {
            ProcessBuilder pb = new ProcessBuilder("which", command);
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Shuts down the clipboard scheduler (call when application exits).
     */
    public static void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
