package com.focusbuddy.controllers;

// Import statements remain the same...

import com.focusbuddy.models.tasks.*;
import com.focusbuddy.services.tasks.*;
import com.focusbuddy.services.pomodoro.*;
import com.focusbuddy.utils.*;
import com.focusbuddy.utils.session.UserSession;
import com.focusbuddy.utils.notification.*;
import com.focusbuddy.utils.error.*;
import com.focusbuddy.utils.icon.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import java.util.List;

public class DashboardController {

    @FXML private BorderPane dashboardContainer;
    @FXML private VBox sidebar;
    @FXML private StackPane contentArea;
    @FXML private Label welcomeLabel;
    @FXML private ToggleButton themeToggle;
    @FXML private Button logoutButton;

    // Navigation buttons
    @FXML private Button dashboardBtn;
    @FXML private Button tasksBtn;
    @FXML private Button pomodoroBtn;
    @FXML private Button notesBtn;
    @FXML private Button subjectBtn;
    @FXML private Button profileBtn;
    @FXML private Button editPasswordBtn;

    // Dashboard content
    @FXML private Label timerDisplay;
    @FXML private Button startTimerBtn;
    @FXML private Button pauseTimerBtn;
    @FXML private Button resetTimerBtn;
    @FXML private ProgressBar timerProgress;
    @FXML private VBox tasksList;
    @FXML private Label tasksCompletedLabel;
    @FXML private Label focusTimeLabel;

    private PomodoroTimer pomodoroTimer;
    private TaskService taskService;
    private String currentView = "dashboard";
    private Node dashboardContent;
    private Button activeButton;

    @FXML
    private void initialize() {
        try {
            // Initialize services
            taskService = new TaskService();
            pomodoroTimer = new PomodoroTimer();

            // Set welcome message
            if (UserSession.getInstance().isLoggedIn()) {
                welcomeLabel.setText("Selamat datang, " +
                        UserSession.getInstance().getCurrentUser().getUsername() + "!");
            }

            // Store original dashboard content
            if (contentArea.getChildren().size() > 0) {
                dashboardContent = contentArea.getChildren().get(0);
            }

            // Setup icons for navigation buttons
            setupIcons();

            // Set up navigation
            setupNavigation();

            // Set up theme toggle
            setupThemeToggle();

            // Set up timer (only if dashboard elements exist)
            if (timerDisplay != null) {
                setupPomodoroTimer();
                loadDashboardData();
            }

            // Set up logout
            logoutButton.setOnAction(e -> handleLogout());

            // Setup responsive layout
            setupResponsiveLayout();

        } catch (Exception e) {
            ErrorHandler.log("Error initializing dashboard", e);
            NotificationManager.getInstance().showError("Gagal memuat dashboard");
        }
    }

    private void setupIcons() {
        IconManager iconManager = IconManager.getInstance();
        
        dashboardBtn.setGraphic(iconManager.createIcon(IconManager.ICON_DASHBOARD));
        tasksBtn.setGraphic(iconManager.createIcon(IconManager.ICON_TASKS));
        pomodoroBtn.setGraphic(iconManager.createIcon(IconManager.ICON_POMODORO));
        notesBtn.setGraphic(iconManager.createIcon(IconManager.ICON_NOTES));
        subjectBtn.setGraphic(iconManager.createIcon(IconManager.ICON_NOTES));
        profileBtn.setGraphic(iconManager.createIcon(IconManager.ICON_PROFILE));
        editPasswordBtn.setGraphic(iconManager.createIcon(IconManager.ICON_SETTINGS));
        logoutButton.setGraphic(iconManager.createIcon(IconManager.ICON_LOGOUT));
        themeToggle.setGraphic(iconManager.createIcon(IconManager.ICON_THEME));

        // Tambahkan tooltip untuk edit password
        editPasswordBtn.setTooltip(new Tooltip("Ubah password akun Anda"));
    }

    private void setupNavigation() {
        // Setup navigation buttons with error handling and animations
        dashboardBtn.setOnAction(e -> showDashboard());
        tasksBtn.setOnAction(e -> loadView("/fxml/new_tasks.fxml"));
        pomodoroBtn.setOnAction(e -> showPomodoro());
        notesBtn.setOnAction(e -> loadView("/fxml/notes.fxml"));
        subjectBtn.setOnAction(e -> loadView("/fxml/subject.fxml"));
        profileBtn.setOnAction(e -> loadView("/fxml/profile_settings.fxml"));
        editPasswordBtn.setOnAction(e -> showEditPasswordDialog());

        // Set initial active button
        setActiveButton(dashboardBtn);
        showDashboard(); // Show dashboard initially
    }

    private void loadView(String fxmlPath) {
        try {
            setActiveButton(getButtonForView(fxmlPath));
            currentView = getViewName(fxmlPath);
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();

            // Animate transition
            FadeTransition fadeOut = new FadeTransition(Duration.millis(150), contentArea);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(view);

                // Fade in new content
                FadeTransition fadeIn = new FadeTransition(Duration.millis(150), contentArea);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            fadeOut.play();

        } catch (Exception e) {
            ErrorHandler.log("Failed to load view: " + fxmlPath, e);
            NotificationManager.getInstance().showError(
                "Gagal memuat halaman. Silakan coba lagi."
            );
        }
    }

    private Button getButtonForView(String fxmlPath) {
        return switch (fxmlPath) {
            case "/fxml/dashboard.fxml" -> dashboardBtn;
            case "/fxml/new_tasks.fxml" -> tasksBtn;
            case "/fxml/notes.fxml" -> notesBtn;
            case "/fxml/subject.fxml" -> subjectBtn;
            case "/fxml/profile_settings.fxml" -> profileBtn;
            default -> null;
        };
    }

    private String getViewName(String fxmlPath) {
        return switch (fxmlPath) {
            case "/fxml/dashboard.fxml" -> "dashboard";
            case "/fxml/new_tasks.fxml" -> "tasks";
            case "/fxml/notes.fxml" -> "notes";
            case "/fxml/subject.fxml" -> "subject";
            case "/fxml/profile_settings.fxml" -> "profile";
            default -> "";
        };
    }

    private void setupThemeToggle() {
        // Set initial theme toggle state
        if (ThemeManager.getInstance().getCurrentTheme() == ThemeManager.Theme.DARK) {
            themeToggle.setText("🌙");
            themeToggle.setSelected(true);
        } else {
            themeToggle.setText("☀️");
            themeToggle.setSelected(false);
        }

        themeToggle.setOnAction(e -> {
            ThemeManager.getInstance().toggleTheme(dashboardContainer.getScene());
            if (ThemeManager.getInstance().getCurrentTheme() == ThemeManager.Theme.DARK) {
                themeToggle.setText("🌙");
            } else {
                themeToggle.setText("☀️");
            }
        });
    }

    private void setupPomodoroTimer() {
        if (timerDisplay == null) return;

        // Set up timer display update
        pomodoroTimer.setOnTimeUpdate((minutes, seconds) -> {
            Platform.runLater(() -> {
                if (timerDisplay != null) {
                    timerDisplay.setText(String.format("%02d:%02d", minutes, seconds));
                }
                if (timerProgress != null) {
                    double progress = pomodoroTimer.getProgress();
                    timerProgress.setProgress(progress);
                }
            });
        });

        // Set up timer completion
        pomodoroTimer.setOnTimerComplete(() -> {
            Platform.runLater(() -> {
                NotificationManager.getInstance().showSuccess("Timer Selesai! Waktunya istirahat sejenak.");
                if (resetTimerBtn != null) {
                    resetTimerBtn.fire();
                }
            });
        });

        // Set up timer controls
        if (startTimerBtn != null) {
            startTimerBtn.setOnAction(e -> pomodoroTimer.start());
        }
        if (pauseTimerBtn != null) {
            pauseTimerBtn.setOnAction(e -> pomodoroTimer.pause());
        }
        if (resetTimerBtn != null) {
            resetTimerBtn.setOnAction(e -> {
                pomodoroTimer.reset();
                if (timerDisplay != null) {
                    timerDisplay.setText("25:00");
                }
                if (timerProgress != null) {
                    timerProgress.setProgress(0);
                }
            });
        }

        // Initialize display
        if (timerDisplay != null) {
            timerDisplay.setText("25:00");
        }
        if (timerProgress != null) {
            timerProgress.setProgress(0);
        }
    }

    private void setupResponsiveLayout() {
        Platform.runLater(() -> {
            Stage stage = (Stage) dashboardContainer.getScene().getWindow();
            stage.widthProperty().addListener((obs, oldWidth, newWidth) -> 
                adjustLayoutForSize(newWidth.doubleValue(), stage.getHeight()));
            stage.heightProperty().addListener((obs, oldHeight, newHeight) -> 
                adjustLayoutForSize(stage.getWidth(), newHeight.doubleValue()));
        });
    }

    private void adjustLayoutForSize(double width, double height) {
        if (width < 1200) {
            sidebar.setPrefWidth(200);
        } else {
            sidebar.setPrefWidth(250);
        }

        if (width < 1000) {
            contentArea.setStyle("-fx-padding: 10;");
        } else {
            contentArea.setStyle("-fx-padding: 20;");
        }
    }

    private void showDashboard() {
        if (!currentView.equals("dashboard")) {
            setActiveButton(dashboardBtn);
            currentView = "dashboard";

            // Restore original dashboard content with animation
            FadeTransition fadeOut = new FadeTransition(Duration.millis(150), contentArea);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                contentArea.getChildren().clear();
                if (dashboardContent != null) {
                    contentArea.getChildren().add(dashboardContent);
                }

                // Fade in dashboard content
                FadeTransition fadeIn = new FadeTransition(Duration.millis(150), contentArea);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();

                // Re-setup timer if needed
                setupPomodoroTimer();
                loadDashboardData();
            });
            fadeOut.play();
        }
    }

    private void showPomodoro() {
        setActiveButton(pomodoroBtn);
        currentView = "pomodoro";
        showDashboard(); // Pomodoro is part of dashboard
    }

    private void showEditPasswordDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/edit_password.fxml"));
            Stage dialogStage = new Stage();
            Scene scene = new Scene(loader.load());
            
            // Apply theme
            ThemeManager.getInstance().applyTheme(scene, ThemeManager.getInstance().getCurrentTheme());
            
            dialogStage.setTitle("Ubah Password");
            dialogStage.setScene(scene);
            dialogStage.initOwner(dashboardContainer.getScene().getWindow());
            
            // Set size and position
            dialogStage.setWidth(400);
            dialogStage.setHeight(500);
            dialogStage.setResizable(false);
            
            // Center on parent
            Stage parentStage = (Stage) dashboardContainer.getScene().getWindow();
            dialogStage.setX(parentStage.getX() + (parentStage.getWidth() - dialogStage.getWidth()) / 2);
            dialogStage.setY(parentStage.getY() + (parentStage.getHeight() - dialogStage.getHeight()) / 2);
            
            dialogStage.showAndWait();
            
        } catch (Exception e) {
            ErrorHandler.log("Error showing edit password dialog", e);
            NotificationManager.getInstance().showError("Gagal membuka form ubah password");
        }
    }

    private void loadDashboardData() {
        try {
            // Load today's tasks
            if (tasksList != null) {
                tasksList.getChildren().clear();
                taskService.getTasksForUser(UserSession.getInstance().getCurrentUser().getId())
                    .forEach(task -> {
                        HBox taskItem = createTaskItem(task);
                        tasksList.getChildren().add(taskItem);
                    });
            }

            // Update statistics
            updateStatistics();
        } catch (Exception e) {
            ErrorHandler.log("Error loading dashboard data", e);
        }
    }

    private HBox createTaskItem(Task task) {
        try {
            HBox taskItem = new HBox(10);
            taskItem.getStyleClass().add("task-item");
            
            CheckBox checkbox = new CheckBox();
            checkbox.setSelected(task.getStatus() == Task.Status.COMPLETED);
            checkbox.setOnAction(e -> handleTaskStatusChange(task, checkbox));
            
            VBox details = new VBox(5);
            Label title = new Label(task.getTitle());
            title.getStyleClass().add("task-title");
            
            Label desc = new Label(task.getDescription());
            desc.getStyleClass().add("task-description");
            desc.setWrapText(true);
            
            details.getChildren().addAll(title, desc);
            
            taskItem.getChildren().addAll(checkbox, details);
            HBox.setHgrow(details, Priority.ALWAYS);
            
            return taskItem;
        } catch (Exception e) {
            ErrorHandler.log("Error creating task item", e);
            return new HBox();
        }
    }

    private void handleTaskStatusChange(Task task, CheckBox checkbox) {
        try {
            task.setStatus(checkbox.isSelected() ? Task.Status.COMPLETED : Task.Status.PENDING);
            taskService.updateTask(task);
            loadDashboardData();
        } catch (Exception e) {
            ErrorHandler.log("Error updating task status", e);
            checkbox.setSelected(!checkbox.isSelected());
        }
    }

    private void updateStatistics() {
        try {
            int userId = UserSession.getInstance().getCurrentUser().getId();
            List<Task> tasks = taskService.getTasksForUser(userId);
            
            long completed = tasks.stream()
                .filter(t -> t.getStatus() == Task.Status.COMPLETED)
                .count();
                
            if (tasksCompletedLabel != null) {
                tasksCompletedLabel.setText(completed + " tugas selesai hari ini");
            }
            
            // Update focus time (this would come from PomodoroSession)
            if (focusTimeLabel != null) {
                focusTimeLabel.setText("2j 30m waktu fokus hari ini");
            }
        } catch (Exception e) {
            ErrorHandler.log("Error updating statistics", e);
        }
    }

    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Keluar");
        alert.setHeaderText("Apakah Anda yakin ingin keluar?");
        alert.setContentText("Perubahan yang belum disimpan akan hilang.");

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                UserSession.getInstance().logout();
                navigateToLogin();
            }
        });
    }

    private void navigateToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) dashboardContainer.getScene().getWindow();
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(loader.load());
            ThemeManager.getInstance().applyTheme(scene, ThemeManager.getInstance().getCurrentTheme());
            stage.setScene(scene);
            stage.setTitle("FocusBuddy - Login");
            stage.setMaximized(false);
            stage.setWidth(800);
            stage.setHeight(600);
            stage.setResizable(true);
            stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
            stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
        } catch (Exception e) {
            ErrorHandler.log("Error navigating to login", e);
            NotificationManager.getInstance().showError("Gagal kembali ke halaman login");
        }
    }

    public void setActiveButton(Button activeButton) {
        this.activeButton = activeButton;
    }

    public Button getActiveButton() {
        return activeButton;
    }
}
