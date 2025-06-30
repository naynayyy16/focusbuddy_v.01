package com.focusbuddy.utils;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;

public class NotificationManager {
    private static NotificationManager instance;
    private static final int NOTIFICATION_TIMEOUT = 3000; // 3 seconds

    public enum NotificationType {
        SUCCESS, ERROR, WARNING, INFO
    }

    private NotificationManager() {}

    public static NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    public void showNotification(String title, String message, NotificationType type) {
        Platform.runLater(() -> {
            // Find the active window
            Window activeWindow = null;
            for (Window window : Stage.getWindows()) {
                if (window.isFocused() && window instanceof Stage) {
                    activeWindow = window;
                    break;
                }
            }

            if (activeWindow == null) return;

            // Create notification popup
            Popup popup = new Popup();
            popup.setAutoFix(true);
            popup.setAutoHide(true);
            popup.setHideOnEscape(true);

            // Create notification content
            VBox notificationBox = createNotificationBox(title, message, type);
            popup.getContent().add(notificationBox);

            // Position the popup
            double centerX = activeWindow.getX() + activeWindow.getWidth() / 2;
            double centerY = activeWindow.getY() + 50; // Show at top of window

            popup.show(activeWindow, centerX - notificationBox.getPrefWidth() / 2, centerY);

            // Add fade in animation
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), notificationBox);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();

            // Auto hide after timeout
            PauseTransition delay = new PauseTransition(Duration.millis(NOTIFICATION_TIMEOUT));
            delay.setOnFinished(e -> {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(200), notificationBox);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(event -> popup.hide());
                fadeOut.play();
            });
            delay.play();
        });
    }

    private VBox createNotificationBox(String title, String message, NotificationType type) {
        VBox notificationBox = new VBox(5);
        notificationBox.setAlignment(Pos.CENTER);
        notificationBox.setPrefWidth(300);
        notificationBox.setPrefHeight(80);
        notificationBox.getStyleClass().add("notification-box");
        
        // Add type-specific style
        switch (type) {
            case SUCCESS:
                notificationBox.getStyleClass().add("notification-success");
                break;
            case ERROR:
                notificationBox.getStyleClass().add("notification-error");
                break;
            case WARNING:
                notificationBox.getStyleClass().add("notification-warning");
                break;
            case INFO:
                notificationBox.getStyleClass().add("notification-info");
                break;
        }

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("notification-title");

        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("notification-message");
        messageLabel.setWrapText(true);

        notificationBox.getChildren().addAll(titleLabel, messageLabel);

        // Add notification styles
        notificationBox.setStyle(
            "-fx-background-color: " + getBackgroundColor(type) + ";" +
            "-fx-padding: 15px;" +
            "-fx-border-radius: 5px;" +
            "-fx-background-radius: 5px;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2);"
        );

        titleLabel.setStyle(
            "-fx-font-weight: bold;" +
            "-fx-font-size: 14px;" +
            "-fx-text-fill: " + getTextColor(type) + ";"
        );

        messageLabel.setStyle(
            "-fx-font-size: 12px;" +
            "-fx-text-fill: " + getTextColor(type) + ";"
        );

        return notificationBox;
    }

    private String getBackgroundColor(NotificationType type) {
        switch (type) {
            case SUCCESS:
                return "#4CAF50";
            case ERROR:
                return "#F44336";
            case WARNING:
                return "#FFC107";
            case INFO:
                return "#2196F3";
            default:
                return "#424242";
        }
    }

    private String getTextColor(NotificationType type) {
        switch (type) {
            case WARNING:
                return "#000000";
            default:
                return "#FFFFFF";
        }
    }

    public void showSuccess(String message) {
        showNotification("Sukses", message, NotificationType.SUCCESS);
    }

    public void showError(String message) {
        showNotification("Error", message, NotificationType.ERROR);
    }

    public void showWarning(String message) {
        showNotification("Peringatan", message, NotificationType.WARNING);
    }

    public void showInfo(String message) {
        showNotification("Info", message, NotificationType.INFO);
    }
}
