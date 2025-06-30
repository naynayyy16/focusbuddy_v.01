USE focusbuddy;

-- Insert dummy users
INSERT INTO users (username, email, password, salt, level, total_xp) VALUES
('user1', 'user1@example.com', 'hashed_password1', 'salt1', 1, 0),
('user2', 'user2@example.com', 'hashed_password2', 'salt2', 2, 100);

-- Insert dummy subjects
INSERT INTO subjects (user_id, name, color) VALUES
(1, 'Math', '#FF0000'),
(1, 'Science', '#00FF00'),
(2, 'History', '#0000FF');

-- Insert dummy tasks
INSERT INTO tasks (user_id, subject_id, title, description, priority, status, completed, due_date) VALUES
(1, 1, 'Task 1', 'Description 1', 'HIGH', 'PENDING', FALSE, '2025-07-10'),
(1, 2, 'Task 2', 'Description 2', 'MEDIUM', 'IN_PROGRESS', FALSE, '2025-07-15'),
(2, 3, 'Task 3', 'Description 3', 'LOW', 'COMPLETED', TRUE, '2025-07-05');

-- Insert dummy notes
INSERT INTO notes (user_id, subject_id, title, content, tags) VALUES
(1, 1, 'Note 1', 'Content of note 1', 'tag1,tag2'),
(1, 2, 'Note 2', 'Content of note 2', 'tag3'),
(2, 3, 'Note 3', 'Content of note 3', '');

-- Insert dummy pomodoro sessions
INSERT INTO pomodoro_sessions (user_id, task_id, subject_id, type, duration, completed_at) VALUES
(1, 1, 1, 'WORK', 25, NOW()),
(1, 2, 2, 'BREAK', 5, NOW()),
(2, 3, 3, 'WORK', 25, NOW());
