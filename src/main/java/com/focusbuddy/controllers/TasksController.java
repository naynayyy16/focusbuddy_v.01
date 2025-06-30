package com.focusbuddy.controllers;

import com.focusbuddy.models.Subject;
import com.focusbuddy.models.Task;
import com.focusbuddy.services.TaskService;
import com.focusbuddy.services.SubjectService;
import com.focusbuddy.utils.NotificationManager;
import com.focusbuddy.utils.UserSession;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class TasksController {
    
    @FXML private VBox tasksContainer;
    @FXML private ListView<Task> tasksList;
    @FXML private TextField taskTitleField;
    @FXML private TextArea taskDescriptionArea;
    @FXML private ComboBox<Task.Priority> priorityCombo;
    @FXML private ComboBox<Task.Status> statusCombo;
    @FXML private DatePicker dueDatePicker;
    @FXML private Button saveTaskButton;
    @FXML private Button newTaskButton;
    @FXML private Button deleteTaskButton;
    @FXML private ComboBox<String> subjectComboBox;
    
    // Filtering and sorting controls
    @FXML private TextField searchField;
    @FXML private ComboBox<Task.Priority> filterPriorityCombo;
    @FXML private ComboBox<Task.Status> filterStatusCombo;
    @FXML private ComboBox<String> sortCombo;
    @FXML private CheckBox showCompletedCheckBox;
    
    // Statistik
    @FXML private Label totalTasksLabel;
    @FXML private Label pendingTasksLabel;
    @FXML private Label completedTasksLabel;
    @FXML private Label overdueTasksLabel;
    
    private TaskService taskService;
    private SubjectService subjectService;
    private Task currentTask;
    private ObservableList<Task> allTasks;
    private ObservableList<String> subjectNames;
    
    @FXML
    private void initialize() {
        taskService = new TaskService();
        subjectService = new SubjectService();
        allTasks = FXCollections.observableArrayList();
        subjectNames = FXCollections.observableArrayList();
        
        setupTasksList();
        setupComboBoxes();
        setupButtons();
        setupFilters();
        loadSubjects();
        loadTasks();
        updateStatistics();
    }
    
    private void loadSubjects() {
        int userId = UserSession.getInstance().getCurrentUser().getId();
        List<Subject> subjects = subjectService.getSubjectsForUser(userId);
        subjectNames.clear();
        for (Subject subject : subjects) {
            subjectNames.add(subject.getName());
        }
        subjectComboBox.setItems(subjectNames);
        if (!subjectNames.isEmpty()) {
            subjectComboBox.setValue(subjectNames.get(0));
        }
    }
    
    private void setupTasksList() {
        tasksList.setCellFactory(listView -> new ListCell<Task>() {
            @Override
            protected void updateItem(Task task, boolean empty) {
                super.updateItem(task, empty);
                if (empty || task == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox content = new VBox(3);
                    
                    // Title with icon
                    HBox titleBox = new HBox(5);
                    Label titleIcon = IconManager.getInstance().createIcon(IconManager.ICON_TASKS);
                    Label titleLabel = new Label(task.getTitle());
                    titleLabel.getStyleClass().add("task-title");
                    titleBox.getChildren().addAll(titleIcon, titleLabel);
                    
                    // Description
                    Label descLabel = new Label(task.getDescription());
                    descLabel.getStyleClass().add("task-description");
                    descLabel.setWrapText(true);
                    
                    // Priority with icon
                    HBox priorityBox = new HBox(5);
                    Label priorityIcon = IconManager.getInstance().createIcon(getPriorityIcon(task.getPriority()));
                    Label priorityLabel = new Label(getPriorityText(task.getPriority()));
                    priorityLabel.getStyleClass().addAll("task-priority", "priority-" + task.getPriority().name().toLowerCase());
                    priorityBox.getChildren().addAll(priorityIcon, priorityLabel);
                    
                    // Status with icon
                    HBox statusBox = new HBox(5);
                    Label statusIcon = IconManager.getInstance().createIcon(getStatusIcon(task.getStatus()));
                    Label statusLabel = new Label(getStatusText(task.getStatus()));
                    statusLabel.getStyleClass().addAll("task-status", "status-" + task.getStatus().name().toLowerCase());
                    statusBox.getChildren().addAll(statusIcon, statusLabel);
                    
                    // Subject with icon
                    HBox subjectBox = new HBox(5);
                    Label subjectIcon = IconManager.getInstance().createIcon(IconManager.ICON_NOTES);
                    Label subjectLabel = new Label(getSubjectNameById(task.getSubjectId()));
                    subjectLabel.getStyleClass().add("task-subject");
                    subjectBox.getChildren().addAll(subjectIcon, subjectLabel);
                    
                    // Due date with icon
                    if (task.getDueDate() != null) {
                        HBox dateBox = new HBox(5);
                        Label dateIcon = IconManager.getInstance().createIcon(IconManager.ICON_CALENDAR);
                        Label dueDateLabel = new Label(formatDueDate(task.getDueDate()));
                        dueDateLabel.getStyleClass().add("task-date");
                        if (task.getDueDate().isBefore(LocalDate.now()) && task.getStatus() != Task.Status.COMPLETED) {
                            dueDateLabel.getStyleClass().add("overdue");
                        }
                        dateBox.getChildren().addAll(dateIcon, dueDateLabel);
                        content.getChildren().add(dateBox);
                    }
                    
                    content.getChildren().addAll(titleBox, descLabel, priorityBox, statusBox, subjectBox);
                    setGraphic(content);
                    
                    // Set background color based on priority
                    setStyle(getTaskItemStyle(task));
                }
            }
        });
        
        tasksList.getSelectionModel().selectedItemProperty().addListener((obs, oldTask, newTask) -> {
            if (newTask != null) {
                loadTaskDetails(newTask);
            }
        });
    }
    
    private String getSubjectNameById(int subjectId) {
        int index = 0;
        for (Subject subject : subjectService.getSubjectsForUser(UserSession.getInstance().getCurrentUser().getId())) {
            if (subject.getId() == subjectId) {
                return subject.getName();
            }
            index++;
        }
        return "Unknown";
    }
    
    private String getPriorityIcon(Task.Priority priority) {
        return switch (priority) {
            case HIGH -> IconManager.ICON_WARNING;
            case MEDIUM -> IconManager.ICON_INFO;
            case LOW -> IconManager.ICON_SUCCESS;
        };
    }
    
    private String getPriorityText(Task.Priority priority) {
        return switch (priority) {
            case HIGH -> "Prioritas Tinggi";
            case MEDIUM -> "Prioritas Sedang";
            case LOW -> "Prioritas Rendah";
        };
    }
    
    private String getStatusIcon(Task.Status status) {
        return switch (status) {
            case COMPLETED -> IconManager.ICON_SUCCESS;
            case IN_PROGRESS -> IconManager.ICON_INFO;
            case PENDING -> IconManager.ICON_WARNING;
        };
    }
    
    private String getStatusText(Task.Status status) {
        return switch (status) {
            case COMPLETED -> "Selesai";
            case IN_PROGRESS -> "Sedang Dikerjakan";
            case PENDING -> "Menunggu";
        };
    }
    
    private String formatDueDate(LocalDate date) {
        if (date.equals(LocalDate.now())) {
            return "Hari ini";
        } else if (date.equals(LocalDate.now().plusDays(1))) {
            return "Besok";
        } else if (date.equals(LocalDate.now().minusDays(1))) {
            return "Kemarin";
        }
        return date.format(DateTimeFormatter.ofPattern("d MMMM yyyy"));
    }
    
    private String getTaskItemStyle(Task task) {
        if (task.getStatus() == Task.Status.COMPLETED) {
            return "-fx-background-color: #E8F5E8; -fx-background-radius: 5px;";
        } else if (task.getDueDate() != null && task.getDueDate().isBefore(LocalDate.now())) {
            return "-fx-background-color: #FFEBEE; -fx-background-radius: 5px;";
        } else if (task.getPriority() == Task.Priority.HIGH) {
            return "-fx-background-color: #FFF3E0; -fx-background-radius: 5px;";
        }
        return "-fx-background-color: #F8F9FA; -fx-background-radius: 5px;";
    }
    
    private void setupComboBoxes() {
        priorityCombo.getItems().addAll(Task.Priority.values());
        statusCombo.getItems().addAll(Task.Status.values());
        
        filterPriorityCombo.getItems().add(null); // Untuk opsi "Semua"
        filterPriorityCombo.getItems().addAll(Task.Priority.values());
        filterPriorityCombo.setPromptText("Semua Prioritas");
        
        filterStatusCombo.getItems().add(null); // Untuk opsi "Semua"
        filterStatusCombo.getItems().addAll(Task.Status.values());
        filterStatusCombo.setPromptText("Semua Status");
        
        sortCombo.getItems().addAll(
            "Judul (A-Z)", "Judul (Z-A)",
            "Prioritas (Tinggi-Rendah)", "Prioritas (Rendah-Tinggi)",
            "Tenggat Waktu (Terdekat)", "Tenggat Waktu (Terjauh)",
            "Tanggal Dibuat (Terbaru)", "Tanggal Dibuat (Terlama)"
        );
        sortCombo.setValue("Prioritas (Tinggi-Rendah)");
    }
    
    private void setupButtons() {
        newTaskButton.setOnAction(e -> createNewTask());
        saveTaskButton.setOnAction(e -> saveCurrentTask());
        deleteTaskButton.setOnAction(e -> deleteCurrentTask());
    }
    
    private void setupFilters() {
        searchField.textProperty().addListener((obs, oldText, newText) -> applyFilters());
        filterPriorityCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        filterStatusCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        sortCombo.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        showCompletedCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }
    
    private void loadTasks() {
        int userId = UserSession.getInstance().getCurrentUser().getId();
        List<Task> tasks = taskService.getTasksForUser(userId);
        allTasks.setAll(tasks);
        applyFilters();
    }
    
    private void applyFilters() {
        List<Task> filteredTasks = allTasks.stream()
            .filter(task -> {
                // Search filter
                String searchText = searchField.getText();
                if (searchText != null && !searchText.trim().isEmpty()) {
                    String search = searchText.toLowerCase();
                    if (!task.getTitle().toLowerCase().contains(search) &&
                        !task.getDescription().toLowerCase().contains(search)) {
                        return false;
                    }
                }
                
                // Priority filter
                Task.Priority priorityFilter = filterPriorityCombo.getValue();
                if (priorityFilter != null && task.getPriority() != priorityFilter) {
                    return false;
                }
                
                // Status filter
                Task.Status statusFilter = filterStatusCombo.getValue();
                if (statusFilter != null && task.getStatus() != statusFilter) {
                    return false;
                }
                
                // Show completed filter
                if (!showCompletedCheckBox.isSelected() && task.getStatus() == Task.Status.COMPLETED) {
                    return false;
                }
                
                return true;
            })
            .collect(Collectors.toList());
        
        // Apply sorting
        String sortOption = sortCombo.getValue();
        if (sortOption != null) {
            switch (sortOption) {
                case "Judul (A-Z)" -> filteredTasks.sort((t1, t2) -> t1.getTitle().compareToIgnoreCase(t2.getTitle()));
                case "Judul (Z-A)" -> filteredTasks.sort((t1, t2) -> t2.getTitle().compareToIgnoreCase(t1.getTitle()));
                case "Prioritas (Tinggi-Rendah)" -> filteredTasks.sort((t1, t2) -> t2.getPriority().compareTo(t1.getPriority()));
                case "Prioritas (Rendah-Tinggi)" -> filteredTasks.sort((t1, t2) -> t1.getPriority().compareTo(t2.getPriority()));
                case "Tenggat Waktu (Terdekat)" -> filteredTasks.sort((t1, t2) -> {
                    if (t1.getDueDate() == null && t2.getDueDate() == null) return 0;
                    if (t1.getDueDate() == null) return 1;
                    if (t2.getDueDate() == null) return -1;
                    return t1.getDueDate().compareTo(t2.getDueDate());
                });
                case "Tenggat Waktu (Terjauh)" -> filteredTasks.sort((t1, t2) -> {
                    if (t1.getDueDate() == null && t2.getDueDate() == null) return 0;
                    if (t1.getDueDate() == null) return -1;
                    if (t2.getDueDate() == null) return 1;
                    return t2.getDueDate().compareTo(t1.getDueDate());
                });
                case "Tanggal Dibuat (Terbaru)" -> filteredTasks.sort((t1, t2) -> {
                    if (t1.getCreatedAt() == null && t2.getCreatedAt() == null) return 0;
                    if (t1.getCreatedAt() == null) return 1;
                    if (t2.getCreatedAt() == null) return -1;
                    return t2.getCreatedAt().compareTo(t1.getCreatedAt());
                });
                case "Tanggal Dibuat (Terlama)" -> filteredTasks.sort((t1, t2) -> {
                    if (t1.getCreatedAt() == null && t2.getCreatedAt() == null) return 0;
                    if (t1.getCreatedAt() == null) return -1;
                    if (t2.getCreatedAt() == null) return 1;
                    return t1.getCreatedAt().compareTo(t2.getCreatedAt());
                });
            }
        }
        
        tasksList.getItems().setAll(filteredTasks);
        updateStatistics();
    }
    
    private void loadTaskDetails(Task task) {
        currentTask = task;
        taskTitleField.setText(task.getTitle());
        taskDescriptionArea.setText(task.getDescription());
        priorityCombo.setValue(task.getPriority());
        statusCombo.setValue(task.getStatus());
        dueDatePicker.setValue(task.getDueDate());
    }
    
    private void createNewTask() {
        currentTask = null;
        taskTitleField.clear();
        taskDescriptionArea.clear();
        priorityCombo.setValue(Task.Priority.MEDIUM);
        statusCombo.setValue(Task.Status.PENDING);
        dueDatePicker.setValue(null);
        
        tasksList.getSelectionModel().clearSelection();
    }
    
    private void saveCurrentTask() {
        String title = taskTitleField.getText().trim();
        String description = taskDescriptionArea.getText().trim();
        Task.Priority priority = priorityCombo.getValue();
        Task.Status status = statusCombo.getValue();
        LocalDate dueDate = dueDatePicker.getValue();
        String subjectName = subjectComboBox.getValue();
        
        // Validasi input
        if (title.isEmpty()) {
            NotificationManager.getInstance().showNotification(
                "Error Validasi", 
                "Judul tugas tidak boleh kosong", 
                NotificationManager.NotificationType.WARNING
            );
            return;
        }

        if (description.isEmpty()) {
            NotificationManager.getInstance().showNotification(
                "Error Validasi", 
                "Deskripsi tugas tidak boleh kosong", 
                NotificationManager.NotificationType.WARNING
            );
            return;
        }

        if (subjectName == null || subjectName.isEmpty()) {
            NotificationManager.getInstance().showNotification(
                "Error Validasi", 
                "Silakan pilih mata pelajaran", 
                NotificationManager.NotificationType.WARNING
            );
            return;
        }
        
        int userId = UserSession.getInstance().getCurrentUser().getId();
        int subjectId = subjectService.getSubjectIdByName(userId, subjectName);
        
        Task task;
        if (currentTask == null) {
            task = new Task(title, description, priority, dueDate);
            task.setUserId(userId);
            task.setSubjectId(subjectId);
        } else {
            task = currentTask;
            task.setTitle(title);
            task.setDescription(description);
            task.setPriority(priority);
            task.setDueDate(dueDate);
            task.setSubjectId(subjectId);
        }
        
        task.setStatus(status);
        
        boolean success;
        if (currentTask == null) {
            success = taskService.addTask(task);
        } else {
            success = taskService.updateTask(task);
        }
        
        if (success) {
            NotificationManager.getInstance().showNotification(
                "Tugas Tersimpan", 
                "Tugas Anda telah berhasil disimpan!", 
                NotificationManager.NotificationType.SUCCESS
            );
            loadTasks();
        } else {
            NotificationManager.getInstance().showNotification(
                "Error", 
                "Gagal menyimpan tugas", 
                NotificationManager.NotificationType.ERROR
            );
        }
    }
    
    private void deleteCurrentTask() {
        if (currentTask == null) {
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Hapus Tugas");
        alert.setHeaderText("Apakah Anda yakin ingin menghapus tugas ini?");
        alert.setContentText("Tindakan ini tidak dapat dibatalkan.");
        
        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                if (taskService.deleteTask(currentTask.getId())) {
            NotificationManager.getInstance().showNotification(
                "Tugas Dihapus", 
                "Tugas telah berhasil dihapus", 
                NotificationManager.NotificationType.SUCCESS
            );
                    loadTasks();
                    createNewTask();
                }
            }
        });
    }
    
    private void updateStatistics() {
        List<Task> currentTasks = tasksList.getItems();
        
        int total = allTasks.size();
        int pending = (int) allTasks.stream().filter(t -> t.getStatus() == Task.Status.PENDING).count();
        int completed = (int) allTasks.stream().filter(t -> t.getStatus() == Task.Status.COMPLETED).count();
        int overdue = (int) allTasks.stream()
            .filter(t -> t.getDueDate() != null && 
                        t.getDueDate().isBefore(LocalDate.now()) && 
                        t.getStatus() != Task.Status.COMPLETED)
            .count();
        
        totalTasksLabel.setText("Total Tugas: " + total);
        pendingTasksLabel.setText("Menunggu: " + pending);
        completedTasksLabel.setText("Selesai: " + completed);
        overdueTasksLabel.setText("Terlambat: " + overdue);
    }
}
