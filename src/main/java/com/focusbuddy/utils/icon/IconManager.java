package com.focusbuddy.utils.icon;

import javafx.scene.control.Label;
import javafx.scene.paint.Color;

public class IconManager {
    private static IconManager instance;

    // Font Awesome icon codes
    public static final String ICON_DASHBOARD = "\uf0e4"; // fa-dashboard
    public static final String ICON_TASKS = "\uf0ae"; // fa-tasks
    public static final String ICON_POMODORO = "\uf017"; // fa-clock-o
    public static final String ICON_NOTES = "\uf249"; // fa-sticky-note
    public static final String ICON_PROFILE = "\uf007"; // fa-user
    public static final String ICON_SETTINGS = "\uf013"; // fa-cog
    public static final String ICON_LOGOUT = "\uf08b"; // fa-sign-out
    public static final String ICON_ADD = "\uf067"; // fa-plus
    public static final String ICON_EDIT = "\uf044"; // fa-edit
    public static final String ICON_DELETE = "\uf1f8"; // fa-trash
    public static final String ICON_SAVE = "\uf0c7"; // fa-save
    public static final String ICON_CANCEL = "\uf05e"; // fa-ban
    public static final String ICON_SUCCESS = "\uf058"; // fa-check-circle
    public static final String ICON_ERROR = "\uf057"; // fa-times-circle
    public static final String ICON_WARNING = "\uf071"; // fa-exclamation-triangle
    public static final String ICON_INFO = "\uf05a"; // fa-info-circle
    public static final String ICON_THEME = "\uf186"; // fa-moon-o
    public static final String ICON_CALENDAR = "\uf073"; // fa-calendar
    public static final String ICON_CHART = "\uf080"; // fa-bar-chart
    public static final String ICON_NOTIFICATION = "\uf0f3"; // fa-bell

    // Default sizes
    public static final int DEFAULT_SIZE = 16;
    public static final int LARGE_SIZE = 24;
    public static final int SMALL_SIZE = 12;

    private IconManager() {}

    public static IconManager getInstance() {
        if (instance == null) {
            instance = new IconManager();
        }
        return instance;
    }

    public Label createIcon(String iconCode) {
        return createIcon(iconCode, DEFAULT_SIZE, null);
    }

    public Label createIcon(String iconCode, int size) {
        return createIcon(iconCode, size, null);
    }

    public Label createIcon(String iconCode, int size, Color color) {
        Label icon = new Label(iconCode);
        icon.getStyleClass().add("icon");
        icon.setStyle(String.format(
            "-fx-font-family: 'FontAwesome'; -fx-font-size: %dpx;%s",
            size,
            color != null ? String.format(" -fx-text-fill: %s;", toRGBCode(color)) : ""
        ));
        return icon;
    }

    private String toRGBCode(Color color) {
        return String.format("#%02X%02X%02X",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255));
    }

    // Helper methods for commonly used icons
    public Label getDashboardIcon() {
        return createIcon(ICON_DASHBOARD, LARGE_SIZE);
    }

    public Label getTasksIcon() {
        return createIcon(ICON_TASKS, LARGE_SIZE);
    }

    public Label getPomodoroIcon() {
        return createIcon(ICON_POMODORO, LARGE_SIZE);
    }

    public Label getNotesIcon() {
        return createIcon(ICON_NOTES, LARGE_SIZE);
    }

    public Label getProfileIcon() {
        return createIcon(ICON_PROFILE, LARGE_SIZE);
    }

    public Label getSettingsIcon() {
        return createIcon(ICON_SETTINGS, LARGE_SIZE);
    }

    public Label getLogoutIcon() {
        return createIcon(ICON_LOGOUT);
    }

    public Label getAddIcon() {
        return createIcon(ICON_ADD);
    }

    public Label getEditIcon() {
        return createIcon(ICON_EDIT);
    }

    public Label getDeleteIcon() {
        return createIcon(ICON_DELETE);
    }

    public Label getSaveIcon() {
        return createIcon(ICON_SAVE);
    }

    public Label getCancelIcon() {
        return createIcon(ICON_CANCEL);
    }

    public Label getSuccessIcon() {
        return createIcon(ICON_SUCCESS, DEFAULT_SIZE, Color.GREEN);
    }

    public Label getErrorIcon() {
        return createIcon(ICON_ERROR, DEFAULT_SIZE, Color.RED);
    }

    public Label getWarningIcon() {
        return createIcon(ICON_WARNING, DEFAULT_SIZE, Color.ORANGE);
    }

    public Label getInfoIcon() {
        return createIcon(ICON_INFO, DEFAULT_SIZE, Color.BLUE);
    }

    public Label getThemeIcon() {
        return createIcon(ICON_THEME);
    }

    public Label getCalendarIcon() {
        return createIcon(ICON_CALENDAR);
    }

    public Label getChartIcon() {
        return createIcon(ICON_CHART);
    }

    public Label getNotificationIcon() {
        return createIcon(ICON_NOTIFICATION);
    }
}
