-- Örnek veriler (sadece BİR KEZ çalıştır)

-- Oda tipleri
INSERT INTO room_types (name, description, capacity, price_per_night) VALUES
('Standard Room',   'Bahçe manzaralı standart oda',          2,  3000.00),
('Deluxe Sea View', 'Deniz manzaralı, balkonlu geniş oda',   3,  4500.00),
('Family Suite',    'İki yatak odalı aile süiti',             5,  7500.00),
('King Suite',      'Jakuzili, özel teraslı lüks süit',       2, 12000.00);

-- Odalar
INSERT INTO rooms (room_number, room_type_id, floor) VALUES
('101', 1, 1),
('102', 1, 1),
('103', 1, 1),
('201', 2, 2),
('202', 2, 2),
('301', 3, 3),
('302', 3, 3),
('401', 4, 4);

-- Kullanıcılar
-- NOT: Şifreler şimdilik sahte; gerçek şifreleme backend'de yapılacak.
INSERT INTO users (full_name, email, password_hash, role) VALUES
('Admin Aurelia', 'admin@aurelia.com', 'temp_hash', 'admin'),
('Ayşe Yılmaz',   'ayse@example.com',  'temp_hash', 'customer');

-- Örnek rezervasyon: Ayşe, oda 201 (Deluxe), 4 gece
INSERT INTO reservations (user_id, room_id, check_in_date, check_out_date, guest_count, total_price, status) VALUES
(2, 4, '2026-12-20', '2026-12-24', 2, 18000.00, 'confirmed');

-- Bu rezervasyonun ödemesi
INSERT INTO payments (reservation_id, amount, method) VALUES
(1, 18000.00, 'card');