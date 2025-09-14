-- Script para crear la tabla users para autenticación JWT
-- Este script debe ejecutarse después del script principal de la base de datos

USE hr_management_db;

-- Crear tabla users para autenticación JWT
CREATE TABLE IF NOT EXISTS users (
    id BINARY(16) PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID())),
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_username (username),
    INDEX idx_role (role),
    INDEX idx_enabled (enabled)
);

-- Insertar usuarios de ejemplo para testing
-- Contraseña para todos los usuarios de ejemplo: "password123"
-- Hash BCrypt: $2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqfuiwTDt2TlJv7c1yqBWVH

INSERT INTO users (id, username, password, role, enabled) VALUES
(UUID_TO_BIN(UUID()), 'admin', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqfuiwTDt2TlJv7c1yqBWVH', 'ROLE_ADMIN', TRUE),
(UUID_TO_BIN(UUID()), 'hr_specialist', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqfuiwTDt2TlJv7c1yqBWVH', 'ROLE_HR_SPECIALIST', TRUE),
(UUID_TO_BIN(UUID()), 'manager', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqfuiwTDt2TlJv7c1yqBWVH', 'ROLE_MANAGER', TRUE),
(UUID_TO_BIN(UUID()), 'employee', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqfuiwTDt2TlJv7c1yqBWVH', 'ROLE_USER', TRUE);

-- Verificar que los usuarios se insertaron correctamente
SELECT
    BIN_TO_UUID(id) as user_id,
    username,
    role,
    enabled,
    created_at
FROM users
ORDER BY username;
