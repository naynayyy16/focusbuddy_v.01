package com.focusbuddy.controllers;

import com.focusbuddy.models.Task;
import com.focusbuddy.services.PomodoroTimer;
import com.focusbuddy.services.TaskService;
import com.focusbuddy.utils.ThemeManager;
import com.focusbuddy.utils.UserSession;
import com.focusbuddy.utils.NotificationManager;
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
    @FXML private Button goalsBtn;
    @FXML private Button exportBtn;

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
        // Initialize services
        taskService = new TaskService();
        pomodoroTimer = new PomodoroTimer();

        // Set welcome message
        if (UserSession.getInstance().isLoggedIn()) {
            welcomeLabel.setText("Welcome back, " +
                    UserSession.getInstance().getCurrentUser().getUsername() + "!");
        }

        // Store original dashboard content
        if (contentArea.getChildren().size() > 0) {
            dashboardContent = contentArea.getChildren().get(0);
        }

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
        dashboardBtn.setOnAction(e -> showDashboard());
        tasksBtn.setOnAction(e -> showTasks());
        pomodoroBtn.setOnAction(e -> showPomodoro());
        // moodBtn.setOnAction(e -> showMoodTracker());
        notesBtn.setOnAction(e -> showNotes());
        // goalsBtn.setOnAction(e -> showGoals());
        // exportBtn.setOnAction(e -> showExport());

        // Set initial active button
        setActiveButton(dashboardBtn);
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
        moodBtn.getStyleClass().remove("active");
        notesBtn.getStyleClass().remove("active");
        goalsBtn.getStyleClass().remove("active");
        exportBtn.getStyleClass().remove("active");

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

    private void showTasks() {
        setActiveButton(tasksBtn);
        currentView = "tasks";
        loadView("/fxml/tasks.fxml");
    }

    private void showPomodoro() {
        setActiveButton(pomodoroBtn);
        currentView = "pomodoro";
        showDashboard(); // Pomodoro is part of dashboard
    }

    /*
    private void showMoodTracker() {
        setActiveButton(moodBtn);
        currentView = "mood";
        loadView("/fxml/mood-tracker.fxml");
    }

    private void showGoals() {
        setActiveButton(goalsBtn);
        currentView = "goals";
        loadView("/fxml/goals.fxml");
    }

    private void showExport() {
        setActiveButton(exportBtn);
        currentView = "export";
        loadView("/fxml/export.fxml");
    }
    */

    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node view = loader.load();

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);

        } catch (Exception e) {
            e.printStackTrace();
            NotificationManager.getInstance().showNotification(
                    "Error",
                    "Failed to load view: " + e.getMessage(),
                    NotificationManager.NotificationType.ERROR
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
            e.printStackTrace();
        }
    }
}