package com.focusbuddy.utils;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ErrorHandler {
    private static final Logger LOGGER = Logger.getLogger(ErrorHandler.class.getName());
    private static FileHandler fileHandler;
    private static final String LOG_FILE = "focusbuddy_error.log";

    static {
        try {
            // Create logs directory if it doesn't exist
            File logsDir = new File("logs");
            if (!logsDir.exists()) {
                logsDir.mkdir();
            }

            // Setup file handler for logging
            fileHandler = new FileHandler("logs/" + LOG_FILE, true);
            fileHandler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fileHandler);
            LOGGER.setLevel(Level.ALL);
        } catch (IOException e) {
            System.err.println("Failed to initialize error logging: " + e.getMessage());
        }
    }

    public static void log(String message, Throwable error) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String fullMessage = String.format("[%s] %s", timestamp, message);
        
        // Log to file
        LOGGER.log(Level.SEVERE, fullMessage, error);
        
        // Print to console for development
        System.err.println(fullMessage);
        error.printStackTrace();

        // Show notification to user
        NotificationManager.getInstance().showError("Terjadi kesalahan: " + message);
    }

    public static void log(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String fullMessage = String.format("[%s] %s", timestamp, message);
        
        // Log to file
        LOGGER.log(Level.SEVERE, fullMessage);
        
        // Print to console for development
        System.err.println(fullMessage);

        // Show notification to user
        NotificationManager.getInstance().showError(message);
    }

    public static void logWarning(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String fullMessage = String.format("[%s] WARNING: %s", timestamp, message);
        
        // Log to file
        LOGGER.log(Level.WARNING, fullMessage);
        
        // Print to console for development
        System.out.println(fullMessage);

        // Show notification to user
        NotificationManager.getInstance().showWarning(message);
    }

    public static void logInfo(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String fullMessage = String.format("[%s] INFO: %s", timestamp, message);
        
        // Log to file
        LOGGER.log(Level.INFO, fullMessage);
        
        // Print to console for development
        System.out.println(fullMessage);
    }

    public static void closeLogger() {
        if (fileHandler != null) {
            fileHandler.close();
        }
    }
}
