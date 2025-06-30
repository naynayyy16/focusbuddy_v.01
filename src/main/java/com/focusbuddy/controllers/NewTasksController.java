package com.focusbuddy.controllers;

import com.focusbuddy.models.Subject;
import com.focusbuddy.models.Task;
import com.focusbuddy.services.TaskService;
import com.focusbuddy.services.SubjectService;
import com.focusbuddy.utils.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.text.TextAlignment;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class NewTasksController {
    
    @FXML private VBox tasksContainer;
    @FXML private VBox tasksList;
    @FXML private TextField taskTitleField;
    @FXML private TextArea taskDescriptionArea;
    @FXML private ComboBox<Task.Priority> priorityCombo;
    @FXML private ComboBox<Task.Status> statusCombo;
    @FXML private DatePicker dueDatePicker;
    @FXML private Button saveTaskButton;
    @FXML private Button newTaskButton;
    @FXML private Button deleteTaskButton;
    @FXML private ComboBox<String> subjectComboBox;
    
    // Filter dan pengurutan
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
        try {
            taskService = new TaskService();
            subjectService = new SubjectService();
            allTasks = FXCollections.observableArrayList();
            subjectNames = FXCollections.observableArrayList();
            
            setupControls();
            setupComboBoxes();
            setupButtons();
            setupFilters();
            loadSubjects();
            loadTasks();
            updateStatistics();
        } catch (Exception e) {
            ErrorHandler.log("Error initializing tasks view", e);
            NotificationManager.getInstance().showError("Gagal memuat tampilan tugas");
        }
    }

    private void setupControls() {
        // Setup placeholder text
        taskTitleField.setPromptText("Judul tugas");
        taskDescriptionArea.setPromptText("Deskripsi tugas");
        searchField.setPromptText("Cari tugas...");
        
        // Setup tooltips
        saveTaskButton.setTooltip(new Tooltip("Simpan tugas"));
        newTaskButton.setTooltip(new Tooltip("Buat tugas baru"));
        deleteTaskButton.setTooltip(new Tooltip("Hapus tugas"));
        
        // Setup icons
        saveTaskButton.setGraphic(IconManager.getInstance().createIcon(IconManager.ICON_SAVE));
        newTaskButton.setGraphic(IconManager.getInstance().createIcon(IconManager.ICON_ADD));
        deleteTaskButton.setGraphic(IconManager.getInstance().createIcon(IconManager.ICON_DELETE));
    }
    
    private void setupComboBoxes() {
        // Setup priority combo
        priorityCombo.getItems().addAll(Task.Priority.values());
        priorityCombo.setPromptText("Pilih prioritas");
        
        // Setup status combo
        statusCombo.getItems().addAll(Task.Status.values());
        statusCombo.setPromptText("Pilih status");
        
        // Setup filter combos
        filterPriorityCombo.getItems().add(null);
        filterPriorityCombo.getItems().addAll(Task.Priority.values());
        filterPriorityCombo.setPromptText("Semua Prioritas");
        
        filterStatusCombo.getItems().add(null);
        filterStatusCombo.getItems().addAll(Task.Status.values());
        filterStatusCombo.setPromptText("Semua Status");
        
        // Setup sort combo
        sortCombo.getItems().addAll(
            "Judul (A-Z)", "Judul (Z-A)",
            "Prioritas (Tinggi-Rendah)", "Prioritas (Rendah-Tinggi)",
            "Tenggat Waktu (Terdekat)", "Tenggat Waktu (Terjauh)",
            "Tanggal Dibuat (Terbaru)", "Tanggal Dibuat (Terlama)"
        );
        sortCombo.setValue("Prioritas (Tinggi-Rendah)");
        
        // Setup subject combo
        subjectComboBox.setPromptText("Pilih mata pelajaran");
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
    
    private void loadSubjects() {
        try {
            int userId = UserSession.getInstance().getCurrentUser().getId();
            List<Subject> subjects = subjectService.getSubjectsForUser(userId);
            subjectNames.clear();
            subjects.forEach(subject -> subjectNames.add(subject.getName()));
            subjectComboBox.setItems(subjectNames);
        } catch (Exception e) {
            ErrorHandler.log("Error loading subjects", e);
            NotificationManager.getInstance().showError("Gagal memuat daftar mata pelajaran");
        }
    }
    
    private void loadTasks() {
        try {
            int userId = UserSession.getInstance().getCurrentUser().getId();
            List<Task> tasks = taskService.getTasksForUser(userId);
            allTasks.setAll(tasks);
            applyFilters();
        } catch (Exception e) {
            ErrorHandler.log("Error loading tasks", e);
            NotificationManager.getInstance().showError("Gagal memuat daftar tugas");
        }
    }
    
    private void updateTasksList(List<Task> tasks) {
        try {
            tasksList.getChildren().clear();
            
            if (tasks.isEmpty()) {
                showEmptyState();
                return;
            }
            
            tasks.forEach(task -> tasksList.getChildren().add(createTaskItem(task)));
            
        } catch (Exception e) {
            ErrorHandler.log("Error updating tasks list", e);
            NotificationManager.getInstance().showError("Gagal memperbarui daftar tugas");
        }
    }
    
    private void showEmptyState() {
        VBox emptyBox = new VBox();
        emptyBox.setAlignment(Pos.CENTER);
        emptyBox.setSpacing(10);
        emptyBox.setPadding(new Insets(20));
        
        Label emptyIcon = IconManager.getInstance().createIcon(IconManager.ICON_INFO, 48);
        emptyIcon.getStyleClass().add("empty-state-icon");
        
        Label emptyLabel = new Label("Belum ada tugas");
        emptyLabel.getStyleClass().add("empty-state-title");
        
        Label emptySubLabel = new Label("Klik tombol '+ Tambah Tugas' untuk membuat tugas baru");
        emptySubLabel.getStyleClass().add("empty-state-subtitle");
        emptySubLabel.setWrapText(true);
        emptySubLabel.setTextAlignment(TextAlignment.CENTER);
        
        emptyBox.getChildren().addAll(emptyIcon, emptyLabel, emptySubLabel);
        tasksList.getChildren().add(emptyBox);
    }
    
    private HBox createTaskItem(Task task) {
        try {
            HBox taskItem = new HBox(10);
            taskItem.getStyleClass().add("task-item");
            taskItem.setPadding(new Insets(10));
            
            // Priority indicator with icon
            String priorityClass = "priority-" + task.getPriority().name().toLowerCase();
            taskItem.getStyleClass().add(priorityClass);
            
            // Checkbox with styling
            CheckBox checkbox = new CheckBox();
            checkbox.setSelected(task.getStatus() == Task.Status.COMPLETED);
            checkbox.getStyleClass().add("task-checkbox");
            checkbox.setOnAction(e -> handleTaskStatusChange(task, checkbox));
            
            // Task details container
            VBox taskDetails = new VBox(5);
            taskDetails.getStyleClass().add("task-details");
            
            // Title with icon
            HBox titleBox = new HBox(5);
            Label titleIcon = IconManager.getInstance().createIcon(IconManager.ICON_TASKS);
            Label titleLabel = new Label(task.getTitle());
            titleLabel.getStyleClass().addAll("task-title", 
                task.getStatus() == Task.Status.COMPLETED ? "completed" : "");
            titleBox.getChildren().addAll(titleIcon, titleLabel);
            
            // Description
            Label descLabel = new Label(task.getDescription());
            descLabel.getStyleClass().add("task-description");
            descLabel.setWrapText(true);
            
            // Metadata container
            HBox metadata = new HBox(10);
            metadata.getStyleClass().add("task-metadata");
            
            // Due date with icon
            if (task.getDueDate() != null) {
                HBox dateBox = new HBox(5);
                Label dateIcon = IconManager.getInstance().createIcon(IconManager.ICON_CALENDAR);
                Label dateLabel = new Label(formatDueDate(task.getDueDate()));
                dateLabel.getStyleClass().add("task-date");
                if (task.getDueDate().isBefore(LocalDate.now()) && 
                    task.getStatus() != Task.Status.COMPLETED) {
                    dateLabel.getStyleClass().add("overdue");
                }
                dateBox.getChildren().addAll(dateIcon, dateLabel);
                metadata.getChildren().add(dateBox);
            }
            
            // Priority label
            Label priorityLabel = new Label(getPriorityText(task.getPriority()));
            priorityLabel.getStyleClass().addAll("task-priority", priorityClass);
            metadata.getChildren().add(priorityLabel);
            
            // Subject
            HBox subjectBox = new HBox(5);
            Label subjectIcon = IconManager.getInstance().createIcon(IconManager.ICON_NOTES);
            Label subjectLabel = new Label(getSubjectNameById(task.getSubjectId()));
            subjectLabel.getStyleClass().add("task-subject");
            subjectBox.getChildren().addAll(subjectIcon, subjectLabel);
            metadata.getChildren().add(subjectBox);
            
            taskDetails.getChildren().addAll(titleBox, descLabel, metadata);
            
            // Action buttons
            HBox actions = new HBox(5);
            actions.getStyleClass().add("task-actions");
            actions.setAlignment(Pos.CENTER_RIGHT);
            
            Button editBtn = new Button();
            editBtn.setGraphic(IconManager.getInstance().createIcon(IconManager.ICON_EDIT));
            editBtn.getStyleClass().add("icon-button");
            editBtn.setOnAction(e -> handleEditTask(task));
            
            Button deleteBtn = new Button();
            deleteBtn.setGraphic(IconManager.getInstance().createIcon(IconManager.ICON_DELETE));
            deleteBtn.getStyleClass().add("icon-button");
            deleteBtn.setOnAction(e -> handleDeleteTask(task));
            
            actions.getChildren().addAll(editBtn, deleteBtn);
            actions.setVisible(false);
            
            // Add all components
            taskItem.getChildren().addAll(checkbox, taskDetails, actions);
            HBox.setHgrow(taskDetails, Priority.ALWAYS);
            
            // Hover effect
            taskItem.setOnMouseEntered(e -> actions.setVisible(true));
            taskItem.setOnMouseExited(e -> actions.setVisible(false));
            
            return taskItem;
        } catch (Exception e) {
            ErrorHandler.log("Error creating task item", e);
            return new HBox(); // Return empty HBox in case of error
        }
    }
    
    private void handleTaskStatusChange(Task task, CheckBox checkbox) {
        try {
            task.setStatus(checkbox.isSelected() ? Task.Status.COMPLETED : Task.Status.PENDING);
            taskService.updateTask(task);
            NotificationManager.getInstance().showSuccess(
                checkbox.isSelected() ? "Tugas selesai!" : "Tugas dibuka kembali"
            );
            loadTasks();
        } catch (Exception e) {
            ErrorHandler.log("Error updating task status", e);
            NotificationManager.getInstance().showError("Gagal mengubah status tugas");
            checkbox.setSelected(!checkbox.isSelected());
        }
    }
    
    private String getSubjectNameById(int subjectId) {
        try {
            Subject subject = subjectService.getSubjectById(subjectId);
            return subject != null ? subject.getName() : "Tidak ada mata pelajaran";
        } catch (Exception e) {
            ErrorHandler.log("Error getting subject name", e);
            return "Error";
        }
    }
    
    private String getPriorityText(Task.Priority priority) {
        return switch (priority) {
            case HIGH -> "Prioritas Tinggi";
            case MEDIUM -> "Prioritas Sedang";
            case LOW -> "Prioritas Rendah";
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
    
    private void applyFilters() {
        List<Task> filteredTasks = allTasks.stream()
            .filter(task -> {
                String searchText = searchField.getText();
                if (searchText != null && !searchText.trim().isEmpty()) {
                    String search = searchText.toLowerCase();
                    if (!task.getTitle().toLowerCase().contains(search) &&
                        !task.getDescription().toLowerCase().contains(search)) {
                        return false;
                    }
                }
                
                Task.Priority priorityFilter = filterPriorityCombo.getValue();
                if (priorityFilter != null && task.getPriority() != priorityFilter) {
                    return false;
                }
                
                Task.Status statusFilter = filterStatusCombo.getValue();
                if (statusFilter != null && task.getStatus() != statusFilter) {
                    return false;
                }
                
                if (!showCompletedCheckBox.isSelected() && 
                    task.getStatus() == Task.Status.COMPLETED) {
                    return false;
                }
                
                return true;
            })
            .collect(Collectors.toList());
        
        // Apply sorting
        String sortOption = sortCombo.getValue();
        if (sortOption != null) {
            switch (sortOption) {
                case "Judul (A-Z)" -> 
                    filteredTasks.sort((t1, t2) -> t1.getTitle().compareToIgnoreCase(t2.getTitle()));
                case "Judul (Z-A)" -> 
                    filteredTasks.sort((t1, t2) -> t2.getTitle().compareToIgnoreCase(t1.getTitle()));
                case "Prioritas (Tinggi-Rendah)" -> 
                    filteredTasks.sort((t1, t2) -> t2.getPriority().compareTo(t1.getPriority()));
                case "Prioritas (Rendah-Tinggi)" -> 
                    filteredTasks.sort((t1, t2) -> t1.getPriority().compareTo(t2.getPriority()));
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
        
        updateTasksList(filteredTasks);
        updateStatistics();
    }
    
    private void createNewTask() {
        currentTask = null;
        taskTitleField.clear();
        taskDescriptionArea.clear();
        priorityCombo.setValue(Task.Priority.MEDIUM);
        statusCombo.setValue(Task.Status.PENDING);
        dueDatePicker.setValue(null);
        subjectComboBox.getSelectionModel().clearSelection();
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
            NotificationManager.getInstance().showError("Judul tugas tidak boleh kosong");
            return;
        }
        
        if (description.isEmpty()) {
            NotificationManager.getInstance().showError("Deskripsi tugas tidak boleh kosong");
            return;
        }
        
        if (priority == null) {
            NotificationManager.getInstance().showError("Silakan pilih prioritas tugas");
            return;
        }
        
        if (subjectName == null || subjectName.isEmpty()) {
            NotificationManager.getInstance().showError("Silakan pilih mata pelajaran");
            return;
        }
        
        try {
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
            
            task.setStatus(status != null ? status : Task.Status.PENDING);
            
            boolean success = currentTask == null ? 
                taskService.addTask(task) : 
                taskService.updateTask(task);
            
            if (success) {
                NotificationManager.getInstance().showSuccess(
                    currentTask == null ? "Tugas baru berhasil dibuat!" : "Tugas berhasil diperbarui!"
                );
                loadTasks();
                createNewTask();
            } else {
                NotificationManager.getInstance().showError("Gagal menyimpan tugas");
            }
        } catch (Exception e) {
            ErrorHandler.log("Error saving task", e);
            NotificationManager.getInstance().showError("Terjadi kesalahan saat menyimpan tugas");
        }
    }
    
    private void handleEditTask(Task task) {
        currentTask = task;
        taskTitleField.setText(task.getTitle());
        taskDescriptionArea.setText(task.getDescription());
        priorityCombo.setValue(task.getPriority());
        statusCombo.setValue(task.getStatus());
        dueDatePicker.setValue(task.getDueDate());
        
        String subjectName = getSubjectNameById(task.getSubjectId());
        subjectComboBox.setValue(subjectName);
    }
    
    private void handleDeleteTask(Task task) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Hapus Tugas");
        alert.setHeaderText("Apakah Anda yakin ingin menghapus tugas ini?");
        alert.setContentText("Tindakan ini tidak dapat dibatalkan.");
        
        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    if (taskService.deleteTask(task.getId())) {
                        NotificationManager.getInstance().showSuccess("Tugas berhasil dihapus");
                        loadTasks();
                        if (currentTask != null && currentTask.getId() == task.getId()) {
                            createNewTask();
                        }
                    } else {
                        NotificationManager.getInstance().showError("Gagal menghapus tugas");
                    }
                } catch (Exception e) {
                    ErrorHandler.log("Error deleting task", e);
                    NotificationManager.getInstance().showError("Terjadi kesalahan saat menghapus tugas");
                }
            }
        });
    }
    
    private void updateStatistics() {
        int total = allTasks.size();
        long pending = allTasks.stream()
            .filter(t -> t.getStatus() == Task.Status.PENDING)
            .count();
        long completed = allTasks.stream()
            .filter(t -> t.getStatus() == Task.Status.COMPLETED)
            .count();
        long overdue = allTasks.stream()
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
