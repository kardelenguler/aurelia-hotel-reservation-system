-- Çakışan rezervasyon kontrolü için gerekli eklenti
CREATE EXTENSION IF NOT EXISTS btree_gist;

-- 1. Kullanıcılar
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL CHECK (TRIM(full_name) <> ''),
    email VARCHAR(255) NOT NULL UNIQUE CHECK (TRIM(email) <> ''),
    password_hash TEXT NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'customer'
        CHECK (role IN ('customer', 'admin')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 2. Oda tipleri
CREATE TABLE room_types (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE CHECK (TRIM(name) <> ''),
    description TEXT,
    capacity INT NOT NULL CHECK (capacity > 0),
    price_per_night NUMERIC(10, 2) NOT NULL CHECK (price_per_night >= 0)
);

-- 3. Odalar
CREATE TABLE rooms (
    id SERIAL PRIMARY KEY,
    room_number VARCHAR(10) NOT NULL UNIQUE CHECK (TRIM(room_number) <> ''),
    room_type_id INT NOT NULL REFERENCES room_types(id) ON DELETE RESTRICT,
    floor INT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- 4. Rezervasyonlar
CREATE TABLE reservations (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    room_id INT NOT NULL REFERENCES rooms(id) ON DELETE RESTRICT,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    guest_count INT NOT NULL CHECK (guest_count > 0),
    total_price NUMERIC(10, 2) NOT NULL CHECK (total_price >= 0),
    status VARCHAR(20) NOT NULL DEFAULT 'pending'
        CHECK (status IN ('pending', 'confirmed', 'checked_in', 'checked_out', 'cancelled')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- Çıkış tarihi girişten sonra olmalı
    CHECK (check_out_date > check_in_date),

    -- Aynı oda, iptal edilmemiş rezervasyonlarda çakışan tarihlere verilemez
    EXCLUDE USING gist (
        room_id WITH =,
        daterange(check_in_date, check_out_date) WITH &&
    ) WHERE (status <> 'cancelled')
);

-- 5. Ödemeler
CREATE TABLE payments (
    id SERIAL PRIMARY KEY,
    reservation_id INT NOT NULL UNIQUE REFERENCES reservations(id) ON DELETE RESTRICT,
    amount NUMERIC(10, 2) NOT NULL CHECK (amount >= 0),
    method VARCHAR(20) NOT NULL CHECK (method IN ('card', 'cash')),
    status VARCHAR(20) NOT NULL DEFAULT 'paid'
        CHECK (status IN ('paid', 'refunded')),
    refund_amount NUMERIC(10, 2) NOT NULL DEFAULT 0,
    paid_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- İade tutarı 0 ile ödenen tutar arasında olmalı
    CONSTRAINT chk_refund_amount CHECK (refund_amount >= 0 AND refund_amount <= amount)
);