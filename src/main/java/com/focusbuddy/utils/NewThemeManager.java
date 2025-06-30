package com.focusbuddy.utils;

import javafx.scene.Scene;
import javafx.scene.Parent;
import java.util.prefs.Preferences;

public class NewThemeManager {
    private static NewThemeManager instance;
    private Theme currentTheme;
    private final Preferences prefs;
    private static final String THEME_PREF_KEY = "app_theme";

    public enum Theme {
        LIGHT, DARK
    }

    private NewThemeManager() {
        prefs = Preferences.userNodeForPackage(NewThemeManager.class);
        String savedTheme = prefs.get(THEME_PREF_KEY, Theme.DARK.name());
        currentTheme = Theme.valueOf(savedTheme);
    }

    public static NewThemeManager getInstance() {
        if (instance == null) {
            instance = new NewThemeManager();
        }
        return instance;
    }

    public void toggleTheme(Scene scene) {
        try {
            currentTheme = (currentTheme == Theme.DARK) ? Theme.LIGHT : Theme.DARK;
            applyTheme(scene, currentTheme);
            saveThemePreference(currentTheme == Theme.DARK);
            
            // Show notification
            String themeName = currentTheme == Theme.DARK ? "Gelap" : "Terang";
            NotificationManager.getInstance().showSuccess("Tema " + themeName + " diterapkan");
            
        } catch (Exception e) {
            ErrorHandler.log("Error toggling theme", e);
            NotificationManager.getInstance().showError("Gagal mengubah tema");
        }
    }

    public void applyTheme(Scene scene, Theme theme) {
        if (scene == null) return;

        try {
            scene.getStylesheets().clear();
            String themePath = getThemePath(theme);
            String stylesheet = getClass().getResource(themePath).toExternalForm();
            scene.getStylesheets().add(stylesheet);
        } catch (Exception e) {
            ErrorHandler.log("Error applying theme", e);
            // Fallback to default theme
            try {
                scene.getStylesheets().add(getClass().getResource("/css/dark-theme.css").toExternalForm());
            } catch (Exception fallbackError) {
                ErrorHandler.log("Error applying fallback theme", fallbackError);
            }
        }
    }

    private String getThemePath(Theme theme) {
        return switch (theme) {
            case DARK -> "/css/dark-theme.css";
            case LIGHT -> "/css/new_light_theme.css";
        };
    }

    public void initializeTheme(Scene scene) {
        try {
            applyTheme(scene, currentTheme);
        } catch (Exception e) {
            ErrorHandler.log("Error initializing theme", e);
        }
    }

    public void setDarkTheme() {
        currentTheme = Theme.DARK;
        saveThemePreference(true);
    }

    public void setLightTheme() {
        currentTheme = Theme.LIGHT;
        saveThemePreference(false);
    }

    public Theme getCurrentTheme() {
        return currentTheme;
    }

    public boolean isDarkTheme() {
        return currentTheme == Theme.DARK;
    }

    public void saveThemePreference(boolean isDark) {
        try {
            currentTheme = isDark ? Theme.DARK : Theme.LIGHT;
            prefs.put(THEME_PREF_KEY, currentTheme.name());
        } catch (Exception e) {
            ErrorHandler.log("Error saving theme preference", e);
        }
    }

    public void applyThemeToAllWindows() {
        try {
            javafx.stage.Window.getWindows().forEach(window -> {
                if (window instanceof javafx.stage.Stage) {
                    Scene scene = ((javafx.stage.Stage) window).getScene();
                    if (scene != null) {
                        applyTheme(scene, currentTheme);
                    }
                }
            });
        } catch (Exception e) {
            ErrorHandler.log("Error applying theme to all windows", e);
        }
    }

    public String getThemeName() {
        return currentTheme == Theme.DARK ? "Gelap" : "Terang";
    }

    public String getThemeIcon() {
        return currentTheme == Theme.DARK ? "🌙" : "☀️";
    }
}
