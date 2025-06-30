package com.focusbuddy.utils.config;

import java.io.*;
import java.util.Properties;
import java.util.prefs.Preferences;
import com.focusbuddy.utils.error.*;
import com.focusbuddy.utils.notification.*;

public class ConfigManager {
    private static ConfigManager instance;
    private final Properties properties;
    private final Preferences preferences;
    private static final String CONFIG_FILE = "config.properties";

    // Default values
    private static final int DEFAULT_POMODORO_DURATION = 25;
    private static final int DEFAULT_BREAK_DURATION = 5;
    private static final boolean DEFAULT_DARK_MODE = true;
    private static final boolean DEFAULT_NOTIFICATIONS = true;
    private static final String DEFAULT_LANGUAGE = "id"; // Indonesian

    // Config keys
    public static final String KEY_POMODORO_DURATION = "pomodoro.duration";
    public static final String KEY_BREAK_DURATION = "break.duration";
    public static final String KEY_DARK_MODE = "theme.dark_mode";
    public static final String KEY_NOTIFICATIONS = "notifications.enabled";
    public static final String KEY_LANGUAGE = "app.language";
    public static final String KEY_LAST_LOGIN = "user.last_login";
    public static final String KEY_AUTO_SAVE = "app.auto_save";
    public static final String KEY_WINDOW_WIDTH = "window.width";
    public static final String KEY_WINDOW_HEIGHT = "window.height";
    public static final String KEY_WINDOW_MAXIMIZED = "window.maximized";

    private ConfigManager() {
        properties = new Properties();
        preferences = Preferences.userNodeForPackage(ConfigManager.class);
        loadConfig();
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    private void loadConfig() {
        try {
            File configFile = new File(CONFIG_FILE);
            if (configFile.exists()) {
                try (FileInputStream fis = new FileInputStream(configFile)) {
                    properties.load(fis);
                }
            } else {
                setDefaultValues();
                saveConfig();
            }
        } catch (IOException e) {
            ErrorHandler.log("Error loading configuration", e);
            setDefaultValues();
        }
    }

    private void setDefaultValues() {
        properties.setProperty(KEY_POMODORO_DURATION, String.valueOf(DEFAULT_POMODORO_DURATION));
        properties.setProperty(KEY_BREAK_DURATION, String.valueOf(DEFAULT_BREAK_DURATION));
        properties.setProperty(KEY_DARK_MODE, String.valueOf(DEFAULT_DARK_MODE));
        properties.setProperty(KEY_NOTIFICATIONS, String.valueOf(DEFAULT_NOTIFICATIONS));
        properties.setProperty(KEY_LANGUAGE, DEFAULT_LANGUAGE);
        properties.setProperty(KEY_AUTO_SAVE, "true");
        properties.setProperty(KEY_WINDOW_WIDTH, "1024");
        properties.setProperty(KEY_WINDOW_HEIGHT, "768");
        properties.setProperty(KEY_WINDOW_MAXIMIZED, "false");
    }

    public void saveConfig() {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            properties.store(fos, "FocusBuddy Configuration");
            ErrorHandler.logInfo("Configuration saved successfully");
        } catch (IOException e) {
            ErrorHandler.log("Error saving configuration", e);
        }
    }

    // Getters with default values
    public int getPomodoroDuration() {
        return Integer.parseInt(properties.getProperty(
            KEY_POMODORO_DURATION, 
            String.valueOf(DEFAULT_POMODORO_DURATION)
        ));
    }

    public int getBreakDuration() {
        return Integer.parseInt(properties.getProperty(
            KEY_BREAK_DURATION, 
            String.valueOf(DEFAULT_BREAK_DURATION)
        ));
    }

    public boolean isDarkMode() {
        return Boolean.parseBoolean(properties.getProperty(
            KEY_DARK_MODE, 
            String.valueOf(DEFAULT_DARK_MODE)
        ));
    }

    public boolean isNotificationsEnabled() {
        return Boolean.parseBoolean(properties.getProperty(
            KEY_NOTIFICATIONS, 
            String.valueOf(DEFAULT_NOTIFICATIONS)
        ));
    }

    public String getLanguage() {
        return properties.getProperty(KEY_LANGUAGE, DEFAULT_LANGUAGE);
    }

    public boolean isAutoSaveEnabled() {
        return Boolean.parseBoolean(properties.getProperty(KEY_AUTO_SAVE, "true"));
    }

    // Setters with automatic save
    public void setPomodoroDuration(int minutes) {
        if (minutes >= 1 && minutes <= 60) {
            properties.setProperty(KEY_POMODORO_DURATION, String.valueOf(minutes));
            saveConfig();
        }
    }

    public void setBreakDuration(int minutes) {
        if (minutes >= 1 && minutes <= 30) {
            properties.setProperty(KEY_BREAK_DURATION, String.valueOf(minutes));
            saveConfig();
        }
    }

    public void setDarkMode(boolean enabled) {
        properties.setProperty(KEY_DARK_MODE, String.valueOf(enabled));
        saveConfig();
    }

    public void setNotificationsEnabled(boolean enabled) {
        properties.setProperty(KEY_NOTIFICATIONS, String.valueOf(enabled));
        saveConfig();
    }

    public void setLanguage(String language) {
        properties.setProperty(KEY_LANGUAGE, language);
        saveConfig();
    }

    public void setAutoSave(boolean enabled) {
        properties.setProperty(KEY_AUTO_SAVE, String.valueOf(enabled));
        saveConfig();
    }

    // Window state management
    public void saveWindowState(double width, double height, boolean maximized) {
        properties.setProperty(KEY_WINDOW_WIDTH, String.valueOf((int) width));
        properties.setProperty(KEY_WINDOW_HEIGHT, String.valueOf((int) height));
        properties.setProperty(KEY_WINDOW_MAXIMIZED, String.valueOf(maximized));
        saveConfig();
    }

    public double getWindowWidth() {
        return Double.parseDouble(properties.getProperty(KEY_WINDOW_WIDTH, "1024"));
    }

    public double getWindowHeight() {
        return Double.parseDouble(properties.getProperty(KEY_WINDOW_HEIGHT, "768"));
    }

    public boolean isWindowMaximized() {
        return Boolean.parseBoolean(properties.getProperty(KEY_WINDOW_MAXIMIZED, "false"));
    }

    // User preferences in registry (persists between sessions)
    public void saveUserPreference(String key, String value) {
        preferences.put(key, value);
    }

    public String getUserPreference(String key, String defaultValue) {
        return preferences.get(key, defaultValue);
    }

    public void removeUserPreference(String key) {
        preferences.remove(key);
    }

    // Reset all settings to default
    public void resetToDefault() {
        setDefaultValues();
        saveConfig();
        NotificationManager.getInstance().showInfo("Pengaturan telah direset ke default");
    }

    // Export/Import configuration
    public void exportConfig(String filePath) {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            properties.store(fos, "FocusBuddy Configuration Export");
            NotificationManager.getInstance().showSuccess("Konfigurasi berhasil diekspor");
        } catch (IOException e) {
            ErrorHandler.log("Error exporting configuration", e);
        }
    }

    public void importConfig(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            properties.load(fis);
            saveConfig();
            NotificationManager.getInstance().showSuccess("Konfigurasi berhasil diimpor");
        } catch (IOException e) {
            ErrorHandler.log("Error importing configuration", e);
        }
    }
}
