package com.focusbuddy.utils;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.HashMap;
import java.util.Map;

public class IconGenerator {
    private static IconGenerator instance;
    private final Map<String, Image> iconCache;
    private static final String ICON_PATH = "/images/icons/";
    
    // Default icon sizes
    public static final double DEFAULT_ICON_SIZE = 24;
    public static final double SMALL_ICON_SIZE = 16;
    public static final double LARGE_ICON_SIZE = 32;

    // Icon names
    public static final String ICON_DASHBOARD = "dashboard";
    public static final String ICON_TASKS = "tasks";
    public static final String ICON_POMODORO = "pomodoro";
    public static final String ICON_NOTES = "notes";
    public static final String ICON_PROFILE = "profile";
    public static final String ICON_SETTINGS = "settings";
    public static final String ICON_LOGOUT = "logout";
    public static final String ICON_ADD = "add";
    public static final String ICON_EDIT = "edit";
    public static final String ICON_DELETE = "delete";
    public static final String ICON_SAVE = "save";
    public static final String ICON_CANCEL = "cancel";
    public static final String ICON_SUCCESS = "success";
    public static final String ICON_ERROR = "error";
    public static final String ICON_WARNING = "warning";
    public static final String ICON_INFO = "info";

    private IconGenerator() {
        iconCache = new HashMap<>();
        preloadIcons();
    }

    public static IconGenerator getInstance() {
        if (instance == null) {
            instance = new IconGenerator();
        }
        return instance;
    }

    private void preloadIcons() {
        // Preload commonly used icons
        loadIcon(ICON_DASHBOARD);
        loadIcon(ICON_TASKS);
        loadIcon(ICON_POMODORO);
        loadIcon(ICON_NOTES);
        loadIcon(ICON_PROFILE);
        loadIcon(ICON_SETTINGS);
        loadIcon(ICON_LOGOUT);
    }

    private void loadIcon(String iconName) {
        try {
            String path = ICON_PATH + iconName + ".png";
            Image icon = new Image(getClass().getResourceAsStream(path));
            iconCache.put(iconName, icon);
        } catch (Exception e) {
            ErrorHandler.log("Failed to load icon: " + iconName, e);
        }
    }

    public ImageView getIcon(String iconName) {
        return getIcon(iconName, DEFAULT_ICON_SIZE);
    }

    public ImageView getIcon(String iconName, double size) {
        Image icon = iconCache.computeIfAbsent(iconName, this::loadIconFromResource);
        if (icon != null) {
            ImageView imageView = new ImageView(icon);
            imageView.setFitWidth(size);
            imageView.setFitHeight(size);
            imageView.setPreserveRatio(true);
            return imageView;
        }
        return createPlaceholderIcon(size);
    }

    private Image loadIconFromResource(String iconName) {
        try {
            String path = ICON_PATH + iconName + ".png";
            return new Image(getClass().getResourceAsStream(path));
        } catch (Exception e) {
            ErrorHandler.log("Failed to load icon: " + iconName, e);
            return null;
        }
    }

    private ImageView createPlaceholderIcon(double size) {
        // Create a simple placeholder for missing icons
        ImageView placeholder = new ImageView();
        placeholder.setFitWidth(size);
        placeholder.setFitHeight(size);
        placeholder.setStyle("-fx-background-color: #CCCCCC;");
        return placeholder;
    }

    public static String getIconPath(String iconName) {
        return ICON_PATH + iconName + ".png";
    }

    // Helper methods for specific icons
    public ImageView getDashboardIcon() {
        return getIcon(ICON_DASHBOARD);
    }

    public ImageView getTasksIcon() {
        return getIcon(ICON_TASKS);
    }

    public ImageView getPomodoroIcon() {
        return getIcon(ICON_POMODORO);
    }

    public ImageView getNotesIcon() {
        return getIcon(ICON_NOTES);
    }

    public ImageView getProfileIcon() {
        return getIcon(ICON_PROFILE);
    }

    public ImageView getSettingsIcon() {
        return getIcon(ICON_SETTINGS);
    }

    public ImageView getLogoutIcon() {
        return getIcon(ICON_LOGOUT);
    }

    public ImageView getAddIcon() {
        return getIcon(ICON_ADD, SMALL_ICON_SIZE);
    }

    public ImageView getEditIcon() {
        return getIcon(ICON_EDIT, SMALL_ICON_SIZE);
    }

    public ImageView getDeleteIcon() {
        return getIcon(ICON_DELETE, SMALL_ICON_SIZE);
    }

    public ImageView getSaveIcon() {
        return getIcon(ICON_SAVE);
    }

    public ImageView getCancelIcon() {
        return getIcon(ICON_CANCEL);
    }

    // Notification icons
    public ImageView getSuccessIcon() {
        return getIcon(ICON_SUCCESS);
    }

    public ImageView getErrorIcon() {
        return getIcon(ICON_ERROR);
    }

    public ImageView getWarningIcon() {
        return getIcon(ICON_WARNING);
    }

    public ImageView getInfoIcon() {
        return getIcon(ICON_INFO);
    }

    // Clear icon cache to free memory
    public void clearCache() {
        iconCache.clear();
    }

    // Reload all icons (useful when changing themes)
    public void reloadIcons() {
        clearCache();
        preloadIcons();
    }
}
