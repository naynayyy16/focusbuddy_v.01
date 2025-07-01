package com.focusbuddy;

import com.focusbuddy.controllers.LoginController;
import com.focusbuddy.controllers.SplashController;
import com.focusbuddy.database.DatabaseManager;
import com.focusbuddy.utils.ThemeManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.animation.FadeTransition;
import javafx.concurrent.Task;
import javafx.util.Duration;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import com.focusbuddy.utils.config.*;
import com.focusbuddy.utils.notification.*;
import com.focusbuddy.utils.error.*;

public class FocusBuddyApp extends Application {
    private Stage primaryStage;
    private SplashController splashController;
    private Scene splashScene;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        try {
            // Tampilkan splash screen
            FXMLLoader splashLoader = new FXMLLoader(getClass().getResource("/fxml/splash.fxml"));
            splashScene = new Scene(splashLoader.load());
            splashController = splashLoader.getController();
            
            // Konfigurasi stage
            primaryStage.setTitle("FocusBuddy");
            primaryStage.setScene(splashScene);
            
            // Atur ukuran dan posisi
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            primaryStage.setWidth(600);
            primaryStage.setHeight(400);
            primaryStage.setX((screenBounds.getWidth() - 600) / 2);
            primaryStage.setY((screenBounds.getHeight() - 400) / 2);
            
            // Set ikon aplikasi
            try {
                Image icon = new Image(getClass().getResourceAsStream("/images/icon.png"));
                if (!icon.isError()) {
                    primaryStage.getIcons().add(icon);
                }
            } catch (Exception iconError) {
                ErrorHandler.log("Tidak dapat memuat ikon aplikasi", iconError);
            }

            // Tampilkan stage
            primaryStage.show();

            // Mulai inisialisasi di background
            startInitialization();

        } catch (Exception e) {
            ErrorHandler.log("Error saat memulai aplikasi", e);
            Platform.exit();
        }
    }

    private void startInitialization() {
        Task<Void> initTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                try {
                    // Inisialisasi database
                    updateMessage("Menginisialisasi database...");
                    Thread.sleep(500); // Memberikan waktu untuk UI update
                    DatabaseManager.getInstance().initializeDatabase();

                    // Inisialisasi konfigurasi
                    updateMessage("Memuat konfigurasi...");
                    Thread.sleep(500);
                    ConfigManager.getInstance();

                    // Migrasi password jika diperlukan
                    updateMessage("Memeriksa keamanan...");
                    Thread.sleep(500);
                    com.focusbuddy.utils.DatabaseMigration.migratePasswords();

                    return null;
                } catch (Exception e) {
                    ErrorHandler.log("Error selama inisialisasi", e);
                    throw e;
                }
            }
        };

        // Update status label
        initTask.messageProperty().addListener((obs, oldMsg, newMsg) -> {
            Platform.runLater(() -> {
                if (splashController != null) {
                    splashController.updateStatus(newMsg);
                }
            });
        });

        // Setelah inisialisasi selesai
        initTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                try {
                    // Load login scene
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
                    Scene loginScene = new Scene(loader.load());

                    // Terapkan tema
                    ThemeManager.getInstance().initializeTheme(loginScene);

                    // Buat transisi fade out untuk splash screen
                    FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), splashScene.getRoot());
                    fadeOut.setFromValue(1);
                    fadeOut.setToValue(0);
                    fadeOut.setOnFinished(event -> {
                        // Update stage
                        primaryStage.setScene(loginScene);
                        primaryStage.setWidth(800);
                        primaryStage.setHeight(600);
                        primaryStage.centerOnScreen();
                        
                        // Set ukuran minimum
                        primaryStage.setMinWidth(800);
                        primaryStage.setMinHeight(600);

                        // Buat transisi fade in untuk login screen
                        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), loginScene.getRoot());
                        fadeIn.setFromValue(0);
                        fadeIn.setToValue(1);
                        fadeIn.setOnFinished(e -> {
                            // Tampilkan notifikasi selamat datang
                            NotificationManager.getInstance().showInfo(
                                "Selamat datang di FocusBuddy!\n" +
                                "Silakan login untuk memulai."
                            );
                        });
                        fadeIn.play();
                    });
                    fadeOut.play();
                } catch (Exception ex) {
                    ErrorHandler.log("Error saat beralih ke layar login", ex);
                    Platform.exit();
                }
            });
        });

        // Handle kegagalan inisialisasi
        initTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                ErrorHandler.showError(
                    "Kesalahan Inisialisasi",
                    "Terjadi kesalahan saat memulai aplikasi",
                    "Silakan coba lagi atau hubungi administrator."
                );
                Platform.exit();
            });
        });

        // Handle penutupan window
        primaryStage.setOnCloseRequest(event -> {
            try {
                // Simpan konfigurasi
                ConfigManager.getInstance().saveConfig();
                
                // Tutup koneksi database
                DatabaseManager.getInstance().closeConnections();
            } catch (Exception e) {
                ErrorHandler.log("Error saat menutup aplikasi", e);
            }
        });

        // Jalankan task inisialisasi
        Thread initThread = new Thread(initTask);
        initThread.setDaemon(true);
        initThread.start();
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
        launch(args);
    }
}