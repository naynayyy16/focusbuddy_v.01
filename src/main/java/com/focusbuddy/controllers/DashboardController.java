package com.focusbuddy.controllers;

import com.focusbuddy.models.tasks.*;
import com.focusbuddy.services.pomodoro.*;
import com.focusbuddy.services.tasks.*;
import com.focusbuddy.utils.ThemeManager;
import com.focusbuddy.utils.session.*;
import com.focusbuddy.utils.notification.*;
import com.focusbuddy.utils.security.*;
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

import java.time.LocalDate;
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
    @FXML private Button moodBtn;
    @FXML private Button notesBtn;
    @FXML private Button profileBtn;

    // Dashboard content (these will be null when other views are loaded)
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

    @FXML
    private void initialize() {
        try {
            // Initialize services
            taskService = new TaskService();
            pomodoroTimer = new PomodoroTimer();

            // Set welcome message
            if (UserSession.getInstance().isLoggedIn()) {
                welcomeLabel.setText("Selamat datang kembali, " +
                        UserSession.getInstance().getCurrentUser().getUsername() + "!");
            }

            // Store original dashboard content
            if (contentArea.getChildren().size() > 0) {
                dashboardContent = contentArea.getChildren().get(0);
            }

            // Set up icons for navigation buttons
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
        
        // Add icons to navigation buttons
        dashboardBtn.setGraphic(iconManager.getDashboardIcon());
        tasksBtn.setGraphic(iconManager.getTasksIcon());
        pomodoroBtn.setGraphic(iconManager.getPomodoroIcon());
        notesBtn.setGraphic(iconManager.getNotesIcon());
        profileBtn.setGraphic(iconManager.getProfileIcon());
        
        // Add icon to logout button
        logoutButton.setGraphic(iconManager.getLogoutIcon());
        
        // Set theme toggle icon
        themeToggle.setGraphic(iconManager.getThemeIcon());
    }

    private void setupResponsiveLayout() {
        // Make sure the content area grows with the window
        Platform.runLater(() -> {
            Stage stage = (Stage) dashboardContainer.getScene().getWindow();

            // Listen for window resize events
            stage.widthProperty().addListener((obs, oldWidth, newWidth) -> {
                adjustLayoutForSize(newWidth.doubleValue(), stage.getHeight());
            });

            stage.heightProperty().addListener((obs, oldHeight, newHeight) -> {
                adjustLayoutForSize(stage.getWidth(), newHeight.doubleValue());
            });
        });
    }

    private void adjustLayoutForSize(double width, double height) {
        // Adjust sidebar width based on screen size
        if (width < 1200) {
            sidebar.setPrefWidth(200);
        } else {
            sidebar.setPrefWidth(250);
        }

        // Adjust content area padding based on screen size
        if (width < 1000) {
            contentArea.setStyle("-fx-padding: 10;");
        } else {
            contentArea.setStyle("-fx-padding: 20;");
        }
    }

    private void setupNavigation() {
        // Setup navigation buttons with error handling
        setupNavigationButton(dashboardBtn, this::showDashboard, "Dashboard");
        setupNavigationButton(tasksBtn, this::showTasks, "Tasks");
        setupNavigationButton(pomodoroBtn, this::showPomodoro, "Pomodoro");
        setupNavigationButton(notesBtn, this::showNotes, "Notes");
        setupNavigationButton(profileBtn, this::showProfile, "Profile");

        // Set initial active button
        setActiveButton(dashboardBtn);
        showDashboard(); // Show dashboard initially
    }

    private void setupNavigationButton(Button button, Runnable action, String pageName) {
        if (button != null) {
            button.setOnAction(e -> {
                try {
                    action.run();
                } catch (Exception ex) {
                    ErrorHandler.log("Error navigating to " + pageName, ex);
                    NotificationManager.getInstance().showError(
                        "Gagal membuka halaman " + pageName + ". " + ex.getMessage()
                    );
                }
            });
        }
    }

    private void showNotes() {
        setActiveButton(notesBtn);
        currentView = "notes";
        loadView("/fxml/notes.fxml");
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
                NotificationManager.getInstance().showNotification(
                        "Timer Complete!",
                        "Great job! Time for a break.",
                        NotificationManager.NotificationType.SUCCESS
                );
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

    private void loadDashboardData() {
        if (tasksList == null) return;

        try {
            // Load today's tasks
            List<Task> todayTasks = taskService.getTasksForUser(
                    UserSession.getInstance().getCurrentUser().getId()
            );

            // Update task list
            updateTasksList(todayTasks);

            // Update statistics
            long completedTasks = todayTasks.stream()
                    .filter(task -> task.getStatus() == Task.Status.COMPLETED)
                    .count();

            if (tasksCompletedLabel != null) {
                tasksCompletedLabel.setText(completedTasks + " tasks completed today");
            }
            if (focusTimeLabel != null) {
                focusTimeLabel.setText("2h 30m focus time today"); // This would come from focus sessions
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateTasksList(List<Task> tasks) {
        if (tasksList == null) return;

        tasksList.getChildren().clear();

        for (Task task : tasks.subList(0, Math.min(5, tasks.size()))) { // Show only first 5
            HBox taskItem = createTaskItem(task);
            tasksList.getChildren().add(taskItem);
        }
    }

    private HBox createTaskItem(Task task) {
        HBox taskItem = new HBox(10);
        taskItem.getStyleClass().add("task-item");

        // Priority indicator
        String priorityClass = "priority-" + task.getPriority().name().toLowerCase();
        taskItem.getStyleClass().add(priorityClass);

        // Checkbox
        CheckBox checkbox = new CheckBox();
        checkbox.setSelected(task.getStatus() == Task.Status.COMPLETED);
        checkbox.setOnAction(e -> {
            task.setStatus(checkbox.isSelected() ? Task.Status.COMPLETED : Task.Status.PENDING);
            taskService.updateTask(task);
            loadDashboardData(); // Refresh data
        });

        // Task details
        VBox taskDetails = new VBox(2);
        Label titleLabel = new Label(task.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold;");

        Label descLabel = new Label(task.getDescription());
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: gray;");

        taskDetails.getChildren().addAll(titleLabel, descLabel);

        // Due date
        Label dueDateLabel = new Label();
        if (task.getDueDate() != null) {
            dueDateLabel.setText(task.getDueDate().toString());
            dueDateLabel.setStyle("-fx-font-size: 11px;");
        }

        taskItem.getChildren().addAll(checkbox, taskDetails, dueDateLabel);
        HBox.setHgrow(taskDetails, Priority.ALWAYS);

        return taskItem;
    }

    private void setActiveButton(Button activeBtn) {
        // Remove active class from all buttons
        dashboardBtn.getStyleClass().remove("active");
        tasksBtn.getStyleClass().remove("active");
        pomodoroBtn.getStyleClass().remove("active");
        notesBtn.getStyleClass().remove("active");
        profileBtn.getStyleClass().remove("active");

        // Add active class to current button
        activeBtn.getStyleClass().add("active");
    }

    private void showDashboard() {
        if (!currentView.equals("dashboard")) {
            setActiveButton(dashboardBtn);
            currentView = "dashboard";

            // Restore original dashboard content
            contentArea.getChildren().clear();
            if (dashboardContent != null) {
                contentArea.getChildren().add(dashboardContent);
            }

            // Re-setup timer if needed
            setupPomodoroTimer();
            loadDashboardData();
        }
    }

    // Menghapus duplikat method loadView

    private Button getButtonForView(String fxmlPath) {
        return switch (fxmlPath) {
            case "/fxml/dashboard.fxml" -> dashboardBtn;
            case "/fxml/new_tasks.fxml" -> tasksBtn;
            case "/fxml/notes.fxml" -> notesBtn;
            case "/fxml/profile_settings.fxml" -> profileBtn;
            default -> null;
        };
    }

    private String getViewName(String fxmlPath) {
        return switch (fxmlPath) {
            case "/fxml/dashboard.fxml" -> "dashboard";
            case "/fxml/new_tasks.fxml" -> "tasks";
            case "/fxml/notes.fxml" -> "notes";
            case "/fxml/profile_settings.fxml" -> "profile";
            default -> "";
        };
    }

    private void showTasks() {
        setActiveButton(tasksBtn);
        currentView = "tasks";
        loadView("/fxml/new_tasks.fxml");
    }

    private void showPomodoro() {
        setActiveButton(pomodoroBtn);
        currentView = "pomodoro";
        showDashboard(); // Pomodoro is part of dashboard
    }

    private void showProfile() {
        setActiveButton(profileBtn);
        currentView = "profile";
        loadView("/fxml/profile_settings.fxml");
    }

    private void loadView(String fxmlPath) {
        try {
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

    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Logout");
        alert.setHeaderText("Are you sure you want to logout?");
        alert.setContentText("Any unsaved changes will be lost.");

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

            // Get screen dimensions
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

            // Create scene without fixed dimensions
            Scene scene = new Scene(loader.load());

            ThemeManager.getInstance().applyTheme(scene, ThemeManager.getInstance().getCurrentTheme());

            stage.setScene(scene);
            stage.setTitle("FocusBuddy - Login");

            // Reset window to normal size for login
            stage.setMaximized(false);
            stage.setWidth(800);
            stage.setHeight(600);
            stage.setResizable(true);

            // Center window
            stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
            stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);

        } catch (Exception e) {
            ErrorHandler.log("Error during logout", e);
            NotificationManager.getInstance().showError("Gagal melakukan logout. Silakan coba lagi.");
        }
    }
}
