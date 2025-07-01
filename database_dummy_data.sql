-- Insert dummy users with password '123456' (using MD5 hash for demonstration)
INSERT INTO users (username, email, password, salt, level, total_xp) VALUES
('john_doe', 'john@example.com', 'e10adc3949ba59abbe56e057f20f883e', 'dummy_salt', 1, 100),
('jane_smith', 'jane@example.com', 'e10adc3949ba59abbe56e057f20f883e', 'dummy_salt', 2, 250),
('bob_wilson', 'bob@example.com', 'e10adc3949ba59abbe56e057f20f883e', 'dummy_salt', 1, 150);

-- Insert dummy subjects
INSERT INTO subjects (user_id, name, color) VALUES
(1, 'Mathematics', '#FF5733'),
(1, 'Physics', '#33FF57'),
(1, 'Computer Science', '#3357FF'),
(2, 'Biology', '#FF33F5'),
(2, 'Chemistry', '#33FFF5'),
(3, 'Literature', '#F5FF33');

-- Insert dummy tasks
INSERT INTO tasks (user_id, subject_id, title, description, priority, status, completed, due_date) VALUES
(1, 1, 'Calculus Homework', 'Complete exercises 1-10 from Chapter 3', 'HIGH', 'PENDING', FALSE, '2024-02-15'),
(1, 2, 'Physics Lab Report', 'Write up results from the pendulum experiment', 'MEDIUM', 'IN_PROGRESS', FALSE, '2024-02-20'),
(1, 3, 'Programming Project', 'Implement a basic calculator in Java', 'HIGH', 'COMPLETED', TRUE, '2024-02-10'),
(2, 4, 'Biology Research', 'Research paper on cell division', 'HIGH', 'IN_PROGRESS', FALSE, '2024-02-18'),
(2, 5, 'Chemistry Quiz Prep', 'Study for upcoming quiz on organic chemistry', 'MEDIUM', 'PENDING', FALSE, '2024-02-25'),
(3, 6, 'Book Analysis', 'Write analysis of Shakespeare\'s Hamlet', 'MEDIUM', 'PENDING', FALSE, '2024-02-28');

-- Insert dummy notes
INSERT INTO notes (user_id, subject_id, title, content, tags) VALUES
(1, 1, 'Derivatives Rules', 'Power Rule: d/dx(x^n) = nx^(n-1)\nProduct Rule: d/dx(uv) = u(dv/dx) + v(du/dx)', 'calculus,math,derivatives'),
(1, 2, 'Newton\'s Laws', '1. An object in motion stays in motion\n2. F = ma\n3. Every action has an equal and opposite reaction', 'physics,mechanics'),
(1, 3, 'Java Basics', 'Object-Oriented Programming Principles:\n1. Encapsulation\n2. Inheritance\n3. Polymorphism', 'programming,java,oop'),
(2, 4, 'Cell Structure', 'Main cell components:\n- Nucleus\n- Mitochondria\n- Endoplasmic Reticulum', 'biology,cells'),
(2, 5, 'Periodic Table', 'First 10 elements and their properties...', 'chemistry,elements'),
(3, 6, 'Literary Devices', 'Metaphor, Simile, Personification, Alliteration...', 'literature,writing');

-- Insert dummy pomodoro sessions
INSERT INTO pomodoro_sessions (user_id, task_id, subject_id, type, duration, completed_at) VALUES
(1, 1, 1, 'WORK', 25, '2024-02-01 10:25:00'),
(1, 1, 1, 'BREAK', 5, '2024-02-01 10:30:00'),
(1, 2, 2, 'WORK', 25, '2024-02-01 11:25:00'),
(2, 4, 4, 'WORK', 25, '2024-02-01 14:25:00'),
(2, 5, 5, 'WORK', 25, '2024-02-01 15:25:00'),
(3, 6, 6, 'WORK', 25, '2024-02-01 16:25:00');
