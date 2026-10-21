-- ═══════════════════════════════════════════════════════════════════════════
-- EventSaathi – MySQL 8 Setup Script
-- Run this ONCE in MySQL CLI or MySQL Workbench BEFORE starting the app
-- ═══════════════════════════════════════════════════════════════════════════

-- 1. Create the database
CREATE DATABASE IF NOT EXISTS eventsaathi_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- 2. (Optional) Create a dedicated app user instead of using root
--    Replace 'your_password' with a strong password
-- CREATE USER IF NOT EXISTS 'eventsaathi_user'@'localhost' IDENTIFIED BY 'your_password';
-- GRANT ALL PRIVILEGES ON eventsaathi_db.* TO 'eventsaathi_user'@'localhost';
-- FLUSH PRIVILEGES;

-- 3. Select the database
USE eventsaathi_db;

-- 4. Verify
SELECT 'Database eventsaathi_db is ready!' AS status;
