package com.focusbuddy.utils;

import javafx.scene.Scene;
import javafx.scene.Parent;
import java.util.prefs.Preferences;

public class ThemeManager {
    private static ThemeManager instance;
    private static Theme currentTheme;
    private static final Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
    private static final String THEME_PREF_KEY = "app_theme";

    public enum Theme {
        LIGHT, DARK
    }

    private ThemeManager() {
        String savedTheme = prefs.get(THEME_PREF_KEY, Theme.DARK.name());
        currentTheme = Theme.valueOf(savedTheme);
    }

    public static ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    public void toggleTheme(Scene scene) {
        currentTheme = (currentTheme == Theme.DARK) ? Theme.LIGHT : Theme.DARK;
        applyTheme(scene, currentTheme);
        saveThemePreference(currentTheme == Theme.DARK);
    }

    public void applyTheme(Scene scene, Theme theme) {
        if (scene == null) return;

        scene.getStylesheets().clear();
        String themePath = (theme == Theme.DARK) 
            ? "/css/dark-theme.css" 
            : "/css/light-theme.css";
            
        scene.getStylesheets().add(getClass().getResource(themePath).toExternalForm());
    }

    public static void setDarkTheme() {
        currentTheme = Theme.DARK;
        saveThemePreference(true);
    }

    public static void setLightTheme() {
        currentTheme = Theme.LIGHT;
        saveThemePreference(false);
    }

    public Theme getCurrentTheme() {
        return currentTheme;
    }

    public static boolean isDarkTheme() {
        return currentTheme == Theme.DARK;
    }

    public static void saveThemePreference(boolean isDark) {
        currentTheme = isDark ? Theme.DARK : Theme.LIGHT;
        prefs.put(THEME_PREF_KEY, currentTheme.name());
    }

    public void applyThemeToAllWindows() {
        javafx.stage.Window.getWindows().forEach(window -> {
            if (window instanceof javafx.stage.Stage) {
                Scene scene = ((javafx.stage.Stage) window).getScene();
                if (scene != null) {
                    applyTheme(scene, currentTheme);
                }
            }
        });
    }

    public void initializeTheme(Scene scene) {
        applyTheme(scene, currentTheme);
    }
}
