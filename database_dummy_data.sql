-- Update data dummy users with plain text password and hashed password example
USE focusbuddy;

DELETE FROM users WHERE username IN ('user1', 'user2');

INSERT INTO users (username, email, password, salt, level, total_xp) VALUES
('user1', 'user1@example.com', '5e884898da28047151d0e56f8dc6292773603d0d6aabbddf', 'somesalt1', 1, 0),
('user2', 'user2@example.com', '6cb75f652a9b52798eb6cf2201057c73e0677d7f', 'somesalt2', 2, 100);

-- Passwords are SHA-256 hashes of "password123" with salt (example).
-- Adjust hashing method as per your application logic.
