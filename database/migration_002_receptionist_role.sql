-- Resepsiyon rolünü ekler ve bir resepsiyon test hesabı oluşturur (bir kez çalıştırılır)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

ALTER TABLE users DROP CONSTRAINT users_role_check;
ALTER TABLE users ADD CONSTRAINT users_role_check
    CHECK (role IN ('customer', 'receptionist', 'admin'));

-- Sadece geliştirme için test hesabı
INSERT INTO users (full_name, email, password_hash, role) VALUES
('Mert Aydın', 'resepsiyon@aurelia.com', crypt('Resepsiyon2026!', gen_salt('bf', 10)), 'receptionist');