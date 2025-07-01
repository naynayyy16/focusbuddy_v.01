package com.focusbuddy.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.focusbuddy.models.settings.User;

public class DatabaseManager {
    private static DatabaseManager instance;
    private static final String URL = "jdbc:mysql://localhost:3306/focusbuddy?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    // Connection pool or single connection for tracking
    private Connection currentConnection;

    private DatabaseManager() {}

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        try {
            // Test if current connection is valid
            if (currentConnection != null && !currentConnection.isClosed() && currentConnection.isValid(5)) {
                return currentConnection;
            }
        } catch (SQLException e) {
            // Connection is not valid, create new one
        }

        // Create new connection
        currentConnection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        return currentConnection;
    }

    public void initializeDatabase() {
        try (Connection conn = getConnection()) {
            createDatabase(conn);
            createTables(conn);
            runMigrations(conn);
            System.out.println("Database initialized successfully!");
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createDatabase(Connection conn) throws SQLException {
        String createDbQuery = "CREATE DATABASE IF NOT EXISTS focusbuddy";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createDbQuery);
        }

        // Switch to the focusbuddy database
        String useDbQuery = "USE focusbuddy";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(useDbQuery);
        }
    }

    private void createTables(Connection conn) throws SQLException {
        String[] createTableQueries = {
                // Users table
                """
            CREATE TABLE IF NOT EXISTS users (
                id INT AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(50) NOT NULL UNIQUE,
                email VARCHAR(100),
                password VARCHAR(255) NOT NULL,
                salt VARCHAR(255),
                level INT DEFAULT 1,
                total_xp INT DEFAULT 0,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                INDEX idx_username (username)
            )
            """,

                // Subjects table
                """
            CREATE TABLE IF NOT EXISTS subjects (
                id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                name VARCHAR(100) NOT NULL,
                color VARCHAR(20),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                INDEX idx_user_id (user_id)
            )
            """,

                // Tasks table
                """
            CREATE TABLE IF NOT EXISTS tasks (
                id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                subject_id INT NOT NULL,
                title VARCHAR(255) NOT NULL,
                description TEXT,
                priority ENUM('LOW', 'MEDIUM', 'HIGH') DEFAULT 'MEDIUM',
                status ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED') DEFAULT 'PENDING',
                completed BOOLEAN DEFAULT FALSE,
                due_date DATE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
                INDEX idx_user_id (user_id),
                INDEX idx_subject_id (subject_id),
                INDEX idx_status (status),
                INDEX idx_due_date (due_date)
            )
            """,

                // Notes table
                """
            CREATE TABLE IF NOT EXISTS notes (
                id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                subject_id INT NOT NULL,
                title VARCHAR(255) NOT NULL,
                content TEXT,
                tags VARCHAR(255),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
                INDEX idx_user_id (user_id),
                INDEX idx_subject_id (subject_id),
                FULLTEXT KEY ft_title_content (title, content)
            )
            """,

                // Pomodoro sessions table
                """
            CREATE TABLE IF NOT EXISTS pomodoro_sessions (
                id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                task_id INT NOT NULL,
                subject_id INT NOT NULL,
                type ENUM('WORK', 'BREAK') NOT NULL,
                duration INT NOT NULL,
                completed_at TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE,
                FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
                INDEX idx_user_id (user_id),
                INDEX idx_task_id (task_id),
                INDEX idx_subject_id (subject_id)
            )
            """,

                // Database migrations table
                """
            CREATE TABLE IF NOT EXISTS migrations (
                id INT AUTO_INCREMENT PRIMARY KEY,
                migration_name VARCHAR(255) NOT NULL,
                executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                UNIQUE KEY unique_migration (migration_name)
            )
            """
        };

        for (String query : createTableQueries) {
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.execute();
            }
        }
    }

    private void runMigrations(Connection conn) throws SQLException {
        // Migration 1: Add level and total_xp columns to users table
        if (!migrationExists(conn, "add_user_level_xp")) {
            String addLevelXp = """
                ALTER TABLE users 
                ADD COLUMN IF NOT EXISTS level INT DEFAULT 1,
                ADD COLUMN IF NOT EXISTS total_xp INT DEFAULT 0
                """;
            try (PreparedStatement stmt = conn.prepareStatement(addLevelXp)) {
                stmt.execute();
                recordMigration(conn, "add_user_level_xp");
            }
        }

        // Migration 2: Add completed column to tasks table
        if (!migrationExists(conn, "add_task_completed")) {
            String addCompleted = "ALTER TABLE tasks ADD COLUMN IF NOT EXISTS completed BOOLEAN DEFAULT FALSE";
            try (PreparedStatement stmt = conn.prepareStatement(addCompleted)) {
                stmt.execute();
                recordMigration(conn, "add_task_completed");
            }
        }

        // Migration 3: Add subject_id to tasks and notes if not exists
        if (!migrationExists(conn, "add_subject_references")) {
            // Check if subject_id column exists in tasks table
            if (!columnExists(conn, "tasks", "subject_id")) {
                String addSubjectToTasks = "ALTER TABLE tasks ADD COLUMN subject_id INT NOT NULL DEFAULT 1";
                try (PreparedStatement stmt = conn.prepareStatement(addSubjectToTasks)) {
                    stmt.execute();
                }

                // Add foreign key constraint
                String addForeignKey = "ALTER TABLE tasks ADD FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE";
                try (PreparedStatement stmt = conn.prepareStatement(addForeignKey)) {
                    stmt.execute();
                }
            }

            // Check if subject_id column exists in notes table
            if (!columnExists(conn, "notes", "subject_id")) {
                String addSubjectToNotes = "ALTER TABLE notes ADD COLUMN subject_id INT NOT NULL DEFAULT 1";
                try (PreparedStatement stmt = conn.prepareStatement(addSubjectToNotes)) {
                    stmt.execute();
                }

                // Add foreign key constraint
                String addForeignKey = "ALTER TABLE notes ADD FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE";
                try (PreparedStatement stmt = conn.prepareStatement(addForeignKey)) {
                    stmt.execute();
                }
            }

            recordMigration(conn, "add_subject_references");
        }
    }

    private boolean migrationExists(Connection conn, String migrationName) throws SQLException {
        String query = "SELECT COUNT(*) FROM migrations WHERE migration_name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, migrationName);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private boolean columnExists(Connection conn, String tableName, String columnName) throws SQLException {
        String query = """
            SELECT COUNT(*) 
            FROM INFORMATION_SCHEMA.COLUMNS 
            WHERE TABLE_SCHEMA = 'focusbuddy' 
            AND TABLE_NAME = ? 
            AND COLUMN_NAME = ?
            """;
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, tableName);
            stmt.setString(2, columnName);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private void recordMigration(Connection conn, String migrationName) throws SQLException {
        String query = "INSERT INTO migrations (migration_name) VALUES (?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, migrationName);
            stmt.executeUpdate();
        }
    }

    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    // =============== CONNECTION MANAGEMENT METHODS ===============

    /**
     * Close all database connections properly
     * Called during application shutdown
     */
    public void closeConnections() {
        try {
            if (currentConnection != null && !currentConnection.isClosed()) {
                currentConnection.close();
                currentConnection = null;
                System.out.println("Database connection closed successfully.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Check if the current connection is valid
     * @return true if connection is valid, false otherwise
     */
    public boolean isConnectionValid() {
        try {
            return currentConnection != null &&
                    !currentConnection.isClosed() &&
                    currentConnection.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Ensure we have a valid connection, reconnect if needed
     */
    public void ensureConnection() {
        try {
            if (!isConnectionValid()) {
                System.out.println("Reconnecting to database...");
                initializeDatabase();
            }
        } catch (Exception e) {
            System.err.println("Failed to ensure database connection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get database connection statistics
     * @return String with connection info
     */
    public String getConnectionInfo() {
        try {
            if (currentConnection != null && !currentConnection.isClosed()) {
                DatabaseMetaData metaData = currentConnection.getMetaData();
                return String.format("Connected to: %s, Driver: %s, Version: %s",
                        metaData.getURL(),
                        metaData.getDriverName(),
                        metaData.getDriverVersion());
            } else {
                return "No active connection";
            }
        } catch (SQLException e) {
            return "Error getting connection info: " + e.getMessage();
        }
    }

    /**
     * Execute a simple health check query
     * @return true if database is responsive
     */
    public boolean healthCheck() {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT 1");
             ResultSet rs = stmt.executeQuery()) {

            return rs.next() && rs.getInt(1) == 1;
        } catch (SQLException e) {
            System.err.println("Database health check failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get the count of active connections (simplified version)
     * In a real connection pool, this would return actual pool stats
     * @return number of active connections
     */
    public int getActiveConnectionCount() {
        return (currentConnection != null && isConnectionValid()) ? 1 : 0;
    }

    /**
     * Force close and recreate the database connection
     */
    public void resetConnection() {
        closeConnections();
        ensureConnection();
    }

    /**
     * Get list of all tables in the database
     * @return List of table names
     */
    public List<String> getTableNames() {
        List<String> tables = new ArrayList<>();
        try (Connection conn = getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet rs = metaData.getTables("focusbuddy", null, null, new String[]{"TABLE"});
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting table names: " + e.getMessage());
        }
        return tables;
    }

    /**
     * Check if a specific table exists
     * @param tableName name of the table to check
     * @return true if table exists
     */
    public boolean tableExists(String tableName) {
        try (Connection conn = getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet rs = metaData.getTables("focusbuddy", null, tableName, new String[]{"TABLE"});
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Error checking table existence: " + e.getMessage());
            return false;
        }
    }

    // =============== DEBUG METHODS FOR LOGIN TROUBLESHOOTING ===============

    /**
     * Debug method to test user authentication
     * @param username the username to check
     * @param password the password to verify
     * @return authentication result with debug info
     */
    public boolean debugLogin(String username, String password) {
        String query = "SELECT id, username, password, salt, email FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            System.out.println("=== DEBUG LOGIN ===");
            System.out.println("Searching for username: '" + username + "'");
            System.out.println("Input password: '" + password + "'");

            if (rs.next()) {
                String dbUsername = rs.getString("username");
                String dbPassword = rs.getString("password");
                String dbSalt = rs.getString("salt");
                String dbEmail = rs.getString("email");
                int userId = rs.getInt("id");

                System.out.println("User found in database:");
                System.out.println("  ID: " + userId);
                System.out.println("  Username: '" + dbUsername + "'");
                System.out.println("  DB Password: '" + dbPassword + "'");
                System.out.println("  Salt: " + (dbSalt != null ? "'" + dbSalt + "'" : "NULL"));
                System.out.println("  Email: '" + dbEmail + "'");
                System.out.println("  Password match (plain): " + password.equals(dbPassword));
                System.out.println("  Username match: " + username.equals(dbUsername));

                return password.equals(dbPassword);
            } else {
                System.out.println("No user found with username: '" + username + "'");
                // Show all usernames for debugging
                showAllUsernames();
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Database error during debug login: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Show all usernames in the database for debugging
     */
    public void showAllUsernames() {
        String query = "SELECT id, username, email FROM users ORDER BY id";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("All users in database:");
            boolean hasUsers = false;
            while (rs.next()) {
                hasUsers = true;
                System.out.println("  ID: " + rs.getInt("id") +
                        ", Username: '" + rs.getString("username") +
                        "', Email: '" + rs.getString("email") + "'");
            }

            if (!hasUsers) {
                System.out.println("  No users found in database!");
            }
        } catch (SQLException e) {
            System.err.println("Error showing usernames: " + e.getMessage());
        }
    }

    /**
     * Verify database connection and user table
     */
    public void verifyDatabase() {
        System.out.println("=== DATABASE VERIFICATION ===");

        // Test connection
        boolean connected = testConnection();
        System.out.println("Database connected: " + connected);

        if (!connected) {
            System.out.println("Cannot connect to database!");
            return;
        }

        // Check if users table exists
        boolean usersTableExists = tableExists("users");
        System.out.println("Users table exists: " + usersTableExists);

        if (!usersTableExists) {
            System.out.println("Users table does not exist! Run initializeDatabase()");
            return;
        }

        // Count users
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM users");
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                int userCount = rs.getInt(1);
                System.out.println("Total users in database: " + userCount);

                if (userCount == 0) {
                    System.out.println("No users found! Database might need to be seeded with dummy data.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error counting users: " + e.getMessage());
        }

        // Show sample users
        showAllUsernames();
    }

    /**
     * Reset user passwords to plain text for testing
     */
    public void resetPasswordsForTesting() {
        String query = "UPDATE users SET password = ?, salt = NULL WHERE username = ?";
        String[] usernames = {"admin", "johndoe", "janesmith", "mikewilson", "sarahbrown"};

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            for (String username : usernames) {
                stmt.setString(1, "123456");
                stmt.setString(2, username);
                int updated = stmt.executeUpdate();
                System.out.println("Reset password for " + username + ": " + (updated > 0 ? "SUCCESS" : "FAILED"));
            }

        } catch (SQLException e) {
            System.err.println("Error resetting passwords: " + e.getMessage());
        }
    }

    /**
     * Insert dummy data if tables are empty
     */
    public void insertDummyData() {
        try (Connection conn = getConnection()) {
            // Check if users table is empty
            String countQuery = "SELECT COUNT(*) FROM users";
            try (PreparedStatement stmt = conn.prepareStatement(countQuery);
                 ResultSet rs = stmt.executeQuery()) {

                if (rs.next() && rs.getInt(1) == 0) {
                    System.out.println("Users table is empty, inserting dummy data...");
                    insertDummyUsers(conn);
                    insertDummySubjects(conn);
                    insertDummyTasks(conn);
                    insertDummyNotes(conn);
                    insertDummyPomodoroSessions(conn);
                    System.out.println("Dummy data inserted successfully!");
                } else {
                    System.out.println("Users table already has data, skipping dummy data insertion.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting dummy data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void insertDummyUsers(Connection conn) throws SQLException {
        String query = """
            INSERT INTO users (username, email, password, salt, level, total_xp) VALUES
            ('admin', 'admin@focusbuddy.com', '123456', NULL, 5, 1250),
            ('johndoe', 'john.doe@example.com', '123456', NULL, 3, 750),
            ('janesmith', 'jane.smith@example.com', '123456', NULL, 2, 300),
            ('mikewilson', 'mike.wilson@example.com', '123456', NULL, 4, 980),
            ('sarahbrown', 'sarah.brown@example.com', '123456', NULL, 1, 150)
            """;

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.executeUpdate();
        }
    }

    private void insertDummySubjects(Connection conn) throws SQLException {
        String query = """
            INSERT INTO subjects (user_id, name, color) VALUES
            (1, 'Web Development', '#3498db'),
            (1, 'Database Design', '#e74c3c'),
            (2, 'Mathematics', '#f39c12'),
            (2, 'Physics', '#9b59b6'),
            (3, 'English Literature', '#2ecc71'),
            (3, 'History', '#34495e'),
            (4, 'Programming', '#e67e22'),
            (4, 'Data Science', '#1abc9c'),
            (5, 'Art & Design', '#e91e63'),
            (5, 'Music Theory', '#8bc34a')
            """;

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.executeUpdate();
        }
    }

    private void insertDummyTasks(Connection conn) throws SQLException {
        String query = """
            INSERT INTO tasks (user_id, subject_id, title, description, priority, status, completed, due_date) VALUES
            (1, 1, 'Build Login System', 'Create user authentication with JWT tokens', 'HIGH', 'IN_PROGRESS', FALSE, '2025-07-15'),
            (1, 1, 'Design Database Schema', 'Create ERD for the application', 'MEDIUM', 'COMPLETED', TRUE, '2025-07-01'),
            (1, 2, 'Optimize SQL Queries', 'Improve performance of database queries', 'MEDIUM', 'PENDING', FALSE, '2025-07-20'),
            (2, 3, 'Calculus Assignment', 'Complete derivative problems 1-20', 'HIGH', 'PENDING', FALSE, '2025-07-05'),
            (2, 4, 'Physics Lab Report', 'Write report on pendulum experiment', 'MEDIUM', 'IN_PROGRESS', FALSE, '2025-07-10'),
            (3, 5, 'Essay on Shakespeare', 'Analysis of Hamlet character development', 'HIGH', 'PENDING', FALSE, '2025-07-12'),
            (3, 6, 'World War II Research', 'Research paper on Pacific Theater', 'MEDIUM', 'PENDING', FALSE, '2025-07-18'),
            (4, 7, 'Python Project', 'Build a web scraper application', 'HIGH', 'IN_PROGRESS', FALSE, '2025-07-08'),
            (4, 8, 'Data Analysis Task', 'Analyze customer behavior dataset', 'MEDIUM', 'PENDING', FALSE, '2025-07-25'),
            (5, 9, 'Logo Design', 'Create logo for local business', 'MEDIUM', 'COMPLETED', TRUE, '2025-06-30'),
            (5, 10, 'Music Composition', 'Compose a short piano piece', 'LOW', 'PENDING', FALSE, '2025-08-01')
            """;

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.executeUpdate();
        }
    }

    private void insertDummyNotes(Connection conn) throws SQLException {
        String query = """
            INSERT INTO notes (user_id, subject_id, title, content, tags) VALUES
            (1, 1, 'JWT Implementation Notes', 'JWT tokens should include user ID, role, and expiration time. Use bcrypt for password hashing.', 'authentication,security,jwt'),
            (1, 2, 'Database Normalization', 'Remember to apply 3NF to reduce data redundancy. Foreign keys ensure referential integrity.', 'database,normalization,design'),
            (2, 3, 'Derivative Rules', 'Power rule: d/dx[x^n] = nx^(n-1). Product rule: d/dx[uv] = u\'v + uv\'. Chain rule: d/dx[f(g(x))] = f\'(g(x))g\'(x)', 'calculus,derivatives,formulas'),
            (2, 4, 'Newton Laws', '1st Law: Object at rest stays at rest. 2nd Law: F = ma. 3rd Law: For every action, equal and opposite reaction.', 'physics,mechanics,newton'),
            (3, 5, 'Hamlet Quotes', 'To be or not to be, that is the question - Hamlet famous soliloquy about existence and suicide.', 'shakespeare,hamlet,quotes'),
            (3, 6, 'WWII Timeline', 'Pearl Harbor: Dec 7, 1941. Midway: June 4-7, 1942. Hiroshima: Aug 6, 1945. Japan Surrender: Aug 15, 1945.', 'history,wwii,timeline'),
            (4, 7, 'Python Best Practices', 'Use virtual environments, follow PEP 8 style guide, write docstrings, use type hints for better code readability.', 'python,coding,best-practices'),
            (4, 8, 'Data Science Process', '1. Data Collection 2. Data Cleaning 3. Exploratory Analysis 4. Modeling 5. Validation 6. Deployment', 'data-science,process,methodology'),
            (5, 9, 'Color Theory', 'Primary colors: Red, Blue, Yellow. Secondary: Orange, Green, Purple. Complementary colors create contrast.', 'design,color-theory,art'),
            (5, 10, 'Music Scales', 'C Major scale: C-D-E-F-G-A-B-C. No sharps or flats. Formula: W-W-H-W-W-W-H (W=whole step, H=half step)', 'music,scales,theory')
            """;

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.executeUpdate();
        }
    }

    private void insertDummyPomodoroSessions(Connection conn) throws SQLException {
        String query = """
            INSERT INTO pomodoro_sessions (user_id, task_id, subject_id, type, duration, completed_at) VALUES
            (1, 1, 1, 'WORK', 25, '2025-07-01 09:30:00'),
            (1, 1, 1, 'BREAK', 5, '2025-07-01 09:35:00'),
            (1, 1, 1, 'WORK', 25, '2025-07-01 10:05:00'),
            (2, 4, 3, 'WORK', 25, '2025-07-01 14:00:00'),
            (2, 4, 3, 'BREAK', 5, '2025-07-01 14:30:00'),
            (3, 6, 5, 'WORK', 25, '2025-07-01 16:15:00'),
            (4, 8, 7, 'WORK', 25, '2025-07-01 11:45:00'),
            (4, 8, 7, 'BREAK', 5, '2025-07-01 12:15:00'),
            (4, 8, 7, 'WORK', 25, '2025-07-01 12:45:00'),
            (5, 10, 9, 'WORK', 25, '2025-07-01 15:30:00')
            """;

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.executeUpdate();
        }
    }

    /**
     * Get user by username and password (for UserSession)
     * @param username the username
     * @param password the password
     * @return User object if found, null otherwise
     */
    public User getUserByCredentials(String username, String password) {
        String query = "SELECT id, username, email, level, total_xp FROM users WHERE username = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                // Add other fields based on your User model
                return user;
            }

        } catch (SQLException e) {
            System.err.println("Error getting user by credentials: " + e.getMessage());
        }

        return null;
    }

    /**
     * Initialize database with tables and dummy data
     */
    public void initializeDatabaseWithData() {
        initializeDatabase();
        insertDummyData();
    }
}