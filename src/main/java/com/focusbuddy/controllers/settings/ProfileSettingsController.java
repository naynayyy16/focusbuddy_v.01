package com.focusbuddy.controllers.settings;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import java.io.File;
import java.net.URL;

import com.focusbuddy.models.settings.User;
import com.focusbuddy.utils.error.ErrorHandler;
import com.focusbuddy.utils.notification.*;
import com.focusbuddy.utils.ThemeManager;
import com.focusbuddy.utils.session.*;
import com.focusbuddy.utils.validation.ValidationUtils;

public class ProfileSettingsController {

    @FXML private ImageView avatarImage;
    @FXML private Button changeAvatarBtn;
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    
    @FXML private RadioButton darkThemeRadio;
    @FXML private RadioButton lightThemeRadio;
    @FXML private ToggleGroup themeToggle;
    
    @FXML private CheckBox taskNotifCheck;
    @FXML private CheckBox pomodoroNotifCheck;
    @FXML private CheckBox reminderNotifCheck;
    
    @FXML private Spinner<Integer> focusDurationSpinner;
    @FXML private Spinner<Integer> breakDurationSpinner;
    
    @FXML private Button cancelBtn;
    @FXML private Button saveBtn;

    private User currentUser;
    private String defaultAvatarUrl = "https://images.pexels.com/photos/415829/pexels-photo-415829.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=750&w=1260";

    @FXML
    public void initialize() {
        setupControls();
        loadUserData();
        setupEventHandlers();
    }

    private void setupControls() {
        // Setup spinners
        SpinnerValueFactory<Integer> focusFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 60, 25);
        SpinnerValueFactory<Integer> breakFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 30, 5);
        
        focusDurationSpinner.setValueFactory(focusFactory);
        breakDurationSpinner.setValueFactory(breakFactory);

        // Setup default avatar
        try {
            avatarImage.setImage(new Image(defaultAvatarUrl));
            avatarImage.setFitWidth(120);
            avatarImage.setFitHeight(120);
        } catch (Exception e) {
            ErrorHandler.log("Error loading default avatar", e);
        }
    }

    private void loadUserData() {
        try {
            currentUser = UserSession.getCurrentUser();
            if (currentUser != null) {
                nameField.setText(currentUser.getName());
                emailField.setText(currentUser.getEmail());
                
                // Load user preferences
                boolean isDarkTheme = ThemeManager.isDarkTheme();
                darkThemeRadio.setSelected(isDarkTheme);
                lightThemeRadio.setSelected(!isDarkTheme);
                
                // Load notification preferences (assuming these are stored in User model)
                taskNotifCheck.setSelected(currentUser.isTaskNotificationsEnabled());
                pomodoroNotifCheck.setSelected(currentUser.isPomodoroNotificationsEnabled());
                reminderNotifCheck.setSelected(currentUser.isReminderNotificationsEnabled());
                
                // Load Pomodoro settings
                focusDurationSpinner.getValueFactory().setValue(currentUser.getFocusDuration());
                breakDurationSpinner.getValueFactory().setValue(currentUser.getBreakDuration());
                
                // Load avatar if exists
                if (currentUser.getAvatarUrl() != null && !currentUser.getAvatarUrl().isEmpty()) {
                    avatarImage.setImage(new Image(currentUser.getAvatarUrl()));
                }
            }
        } catch (Exception e) {
            ErrorHandler.log("Error loading user data", e);
            NotificationManager.showError("Gagal memuat data pengguna");
        }
    }

    private void setupEventHandlers() {
        changeAvatarBtn.setOnAction(e -> handleChangeAvatar());
        saveBtn.setOnAction(e -> handleSave());
        cancelBtn.setOnAction(e -> handleCancel());
        
        // Theme change listener
        themeToggle.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == darkThemeRadio) {
                ThemeManager.setDarkTheme();
            } else {
                ThemeManager.setLightTheme();
            }
        });
    }

    private void handleChangeAvatar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih Foto Profil");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        
        File selectedFile = fileChooser.showOpenDialog(avatarImage.getScene().getWindow());
        if (selectedFile != null) {
            try {
                Image image = new Image(selectedFile.toURI().toString());
                avatarImage.setImage(image);
                // TODO: Implement avatar upload to server/storage
            } catch (Exception e) {
                ErrorHandler.log("Error loading avatar image", e);
                NotificationManager.showError("Gagal memuat gambar avatar");
            }
        }
    }

    private void handleSave() {
        if (!validateInput()) {
            return;
        }

        try {
            // Update user data
            currentUser.setName(nameField.getText());
            currentUser.setEmail(emailField.getText());
            
            // Update notification preferences
            currentUser.setTaskNotificationsEnabled(taskNotifCheck.isSelected());
            currentUser.setPomodoroNotificationsEnabled(pomodoroNotifCheck.isSelected());
            currentUser.setReminderNotificationsEnabled(reminderNotifCheck.isSelected());
            
            // Update Pomodoro settings
            currentUser.setFocusDuration(focusDurationSpinner.getValue());
            currentUser.setBreakDuration(breakDurationSpinner.getValue());
            
            // Save theme preference
            ThemeManager.saveThemePreference(darkThemeRadio.isSelected());
            
            // TODO: Call UserService to save changes to database
            
            NotificationManager.showSuccess("Perubahan berhasil disimpan");
        } catch (Exception e) {
            ErrorHandler.log("Error saving user data", e);
            NotificationManager.showError("Gagal menyimpan perubahan");
        }
    }

    private void handleCancel() {
        // Reset to original values
        loadUserData();
    }

    private boolean validateInput() {
        if (nameField.getText().trim().isEmpty()) {
            NotificationManager.showError("Nama tidak boleh kosong");
            return false;
        }

        if (!ValidationUtils.isValidEmail(emailField.getText())) {
            NotificationManager.showError("Format email tidak valid");
            return false;
        }

        return true;
    }
}
