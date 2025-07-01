module com.focusbuddy {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.prefs;
    requires java.desktop;
    requires javafx.web;

    // Open controller packages for FXML
    opens com.focusbuddy to javafx.fxml;
    opens com.focusbuddy.controllers to javafx.fxml;
    opens com.focusbuddy.controllers.tasks to javafx.fxml;
    opens com.focusbuddy.controllers.notes to javafx.fxml;
    opens com.focusbuddy.controllers.pomodoro to javafx.fxml;
    opens com.focusbuddy.controllers.subjects to javafx.fxml;
    opens com.focusbuddy.controllers.settings to javafx.fxml;
    opens com.focusbuddy.controllers.auth to javafx.fxml;

    // Open model packages for JavaFX properties
    opens com.focusbuddy.models.notes to javafx.base;
    opens com.focusbuddy.models.tasks to javafx.base;
    opens com.focusbuddy.models.pomodoro to javafx.base;
    opens com.focusbuddy.models.subjects to javafx.base;
    opens com.focusbuddy.models.settings to javafx.base;

    // Export base packages
    exports com.focusbuddy;
    exports com.focusbuddy.controllers;
    exports com.focusbuddy.models.notes;
    exports com.focusbuddy.models.tasks;
    exports com.focusbuddy.models.pomodoro;
    exports com.focusbuddy.models.subjects;
    exports com.focusbuddy.models.settings;
    exports com.focusbuddy.services.notes;
    exports com.focusbuddy.services.tasks;
    exports com.focusbuddy.services.pomodoro;
    exports com.focusbuddy.services.subjects;
    exports com.focusbuddy.utils;
    exports com.focusbuddy.utils.data;
    exports com.focusbuddy.database;

    // Remove duplicate export of com.focusbuddy.utils.data
    // exports com.focusbuddy.utils.data;
    exports com.focusbuddy.observers.notification;
    exports com.focusbuddy.observers.timer;

    // Export controller subpackages
    exports com.focusbuddy.controllers.tasks;
    exports com.focusbuddy.controllers.notes;
    exports com.focusbuddy.controllers.pomodoro;
    exports com.focusbuddy.controllers.subjects;
    exports com.focusbuddy.controllers.settings;
    exports com.focusbuddy.controllers.auth;

    // Export utility packages
    exports com.focusbuddy.utils.validation;
    exports com.focusbuddy.utils.notification;
    exports com.focusbuddy.utils.config;
    exports com.focusbuddy.utils.error;
    exports com.focusbuddy.utils.session;
    exports com.focusbuddy.utils.icon;
    exports com.focusbuddy.utils.theme;
    exports com.focusbuddy.utils.security;
}
