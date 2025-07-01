package com.focusbuddy;

import com.focusbuddy.database.DatabaseManager;
import com.focusbuddy.utils.config.*;
import com.focusbuddy.utils.error.*;
import com.focusbuddy.utils.NewThemeManager;
import com.focusbuddy.utils.notification.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;

public class NewFocusBuddyApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize database
            DatabaseManager.getInstance().initializeDatabase();

            // Database sudah diinisialisasi, tidak perlu update password

            // Initialize configuration
            ConfigManager.getInstance();

            // Load integrated login scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/new_integrated_login.fxml"));
            Scene scene = new Scene(loader.load());

            // Apply theme
            NewThemeManager.getInstance().initializeTheme(scene);

            // Configure stage
            primaryStage.setTitle("FocusBuddy");
            primaryStage.setScene(scene);
            
            // Set size and position
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            primaryStage.setWidth(800);
            primaryStage.setHeight(600);
            primaryStage.setX((screenBounds.getWidth() - 800) / 2);
            primaryStage.setY((screenBounds.getHeight() - 600) / 2);
            
            // Set minimum size
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);

            // Set application icon
            try {
                Image icon = new Image(getClass().getResourceAsStream("/images/icon.png"));
                if (!icon.isError()) {
                    primaryStage.getIcons().add(icon);
                }
            } catch (Exception iconError) {
                ErrorHandler.log("Could not load application icon", iconError);
            }

            // Show the stage
            primaryStage.show();

            // Show welcome notification
            NotificationManager.getInstance().showInfo(
                "Selamat datang di FocusBuddy!\n" +
                "Silakan login untuk memulai."
            );

            // Handle window close
            primaryStage.setOnCloseRequest(event -> {
                try {
                    // Save configuration
                    ConfigManager.getInstance().saveConfig();
                    
                    // Close database connections
                    DatabaseManager.getInstance().closeConnections();
                } catch (Exception e) {
                    ErrorHandler.log("Error during shutdown", e);
                }
            });

        } catch (Exception e) {
            ErrorHandler.log("Error starting application", e);
            System.exit(1);
        }
    }

    @Override
    public void stop() {
        try {
            // Save configuration changes
            ConfigManager.getInstance().saveConfig();
        } catch (Exception e) {
            ErrorHandler.log("Error saving configuration on exit", e);
        }
    }

    public static void main(String[] args) {
        // Set system properties for better UI scaling and performance
        System.setProperty("prism.allowhidpi", "true");
        System.setProperty("glass.gtk.uiScale", "1.0");
        System.setProperty("prism.order", "sw,d3d,es2");

        launch(args);
    }
}
