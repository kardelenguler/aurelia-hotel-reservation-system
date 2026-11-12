-- =====================================================
--  AURELIA GRAND RESORT — ÖRNEK VERİLER
--  ⚠️ TÜM TABLOLARI SIFIRLAR — sadece geliştirme ortamı için
-- =====================================================

-- Şifreleri BCrypt ile üretmek için (Spring Boot ile uyumlu)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Tüm verileri sil, id sayaçlarını 1'den başlat
TRUNCATE payments, reservations, rooms, room_types, users RESTART IDENTITY CASCADE;

-- ---------- Oda tipleri ----------
INSERT INTO room_types (name, description, capacity, price_per_night) VALUES
('Standard Room',   'Bahçe manzaralı standart oda',        2,  3000.00),
('Deluxe Sea View', 'Deniz manzaralı, balkonlu geniş oda', 3,  4500.00),
('Family Suite',    'İki yatak odalı aile süiti',          5,  7500.00),
('King Suite',      'Jakuzili, özel teraslı lüks süit',    2, 12000.00);

-- ---------- 300 oda ----------
-- Standard Room (150): 1-5. katlar, her katta 30 oda → 101-130 ... 501-530
INSERT INTO rooms (room_number, room_type_id, floor)
SELECT (kat * 100 + no)::text, 1, kat
FROM generate_series(1, 5) AS kat, generate_series(1, 30) AS no
ORDER BY kat, no;

-- Deluxe Sea View (90): 6-8. katlar, her katta 30 oda → 601-630 ... 801-830
INSERT INTO rooms (room_number, room_type_id, floor)
SELECT (kat * 100 + no)::text, 2, kat
FROM generate_series(6, 8) AS kat, generate_series(1, 30) AS no
ORDER BY kat, no;

-- Family Suite (45): 9-11. katlar, her katta 15 oda → 901-915 ... 1101-1115
INSERT INTO rooms (room_number, room_type_id, floor)
SELECT (kat * 100 + no)::text, 3, kat
FROM generate_series(9, 11) AS kat, generate_series(1, 15) AS no
ORDER BY kat, no;

-- King Suite (15): 12. kat → 1201-1215
INSERT INTO rooms (room_number, room_type_id, floor)
SELECT (12 * 100 + no)::text, 4, 12
FROM generate_series(1, 15) AS no
ORDER BY no;

-- ---------- Kullanıcılar (giriş yapılabilir test hesapları) ----------
-- Sadece geliştirme için test şifreleri
INSERT INTO users (full_name, email, password_hash, role) VALUES
('Kardelen Güler', 'kardelen@aurelia.com',   crypt('Aurelia2026!',    gen_salt('bf', 10)), 'admin'),
('Ayşe Yılmaz',    'ayse@example.com',       crypt('Musteri2026!',    gen_salt('bf', 10)), 'customer'),
('Elif Demir',     'elif@example.com',       crypt('Musteri2026!',    gen_salt('bf', 10)), 'customer'),
('Mert Aydın',     'resepsiyon@aurelia.com', crypt('Resepsiyon2026!', gen_salt('bf', 10)), 'receptionist');

-- ---------- Örnek rezervasyonlar (tarihler bugüne göre) ----------
INSERT INTO reservations (user_id, room_id, check_in_date, check_out_date, guest_count, total_price, status) VALUES
-- 1) Ayşe: 20 gün sonra, 4 gece, oda 601 (Deluxe), ödenmiş
(2, (SELECT id FROM rooms WHERE room_number = '601'), CURRENT_DATE + 20, CURRENT_DATE + 24, 2, 18000.00, 'confirmed'),
-- 2) Ayşe: dün giriş yaptı, 3 gece, oda 102 (Standard), şu an otelde
(2, (SELECT id FROM rooms WHERE room_number = '102'), CURRENT_DATE - 1,  CURRENT_DATE + 2,  2,  9000.00, 'checked_in'),
-- 3) Elif: 40 gün sonra, 3 gece, oda 101 (Standard), ödeme bekliyor
(3, (SELECT id FROM rooms WHERE room_number = '101'), CURRENT_DATE + 40, CURRENT_DATE + 43, 1,  9000.00, 'pending');

INSERT INTO payments (reservation_id, amount, method, status) VALUES
(1, 18000.00, 'card', 'paid'),
(2,  9000.00, 'cash', 'paid');

-- ---------- Kontrol ----------
SELECT rt.name AS oda_tipi, COUNT(*) AS oda_sayisi
FROM rooms r
JOIN room_types rt ON r.room_type_id = rt.id
GROUP BY rt.id, rt.name
ORDER BY rt.id;