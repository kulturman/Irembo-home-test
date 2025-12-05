ALTER TABLE users ADD COLUMN role VARCHAR(50) NOT NULL DEFAULT 'USER';

UPDATE users SET role = 'ADMIN', email = 'admin@example.com' WHERE email = 'user2@example.com';
UPDATE users SET email = 'user@example.com' WHERE email = 'user1@example.com';
CREATE INDEX idx_users_role ON users(role);
