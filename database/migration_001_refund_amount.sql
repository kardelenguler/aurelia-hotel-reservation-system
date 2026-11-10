-- Mevcut veritabanına iade tutarı sütununu ekler (bir kez çalıştırılır)
ALTER TABLE payments
    ADD COLUMN refund_amount NUMERIC(10, 2) NOT NULL DEFAULT 0;

ALTER TABLE payments
    ADD CONSTRAINT chk_refund_amount CHECK (refund_amount >= 0 AND refund_amount <= amount);