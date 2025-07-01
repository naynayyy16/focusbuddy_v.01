package com.focusbuddy.controllers;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class SplashController {
    @FXML
    private Label statusLabel;
    
    @FXML
    private VBox mainContainer;

    @FXML
    public void initialize() {
        // Buat efek fade in saat splash screen muncul
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), mainContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    public void updateStatus(String message) {
        if (statusLabel != null) {
            // Buat efek fade untuk perubahan status
            FadeTransition fade = new FadeTransition(Duration.millis(200), statusLabel);
            fade.setFromValue(1);
            fade.setToValue(0);
            fade.setOnFinished(e -> {
                statusLabel.setText(message);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), statusLabel);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
            });
            fade.play();
        }
    }
}
