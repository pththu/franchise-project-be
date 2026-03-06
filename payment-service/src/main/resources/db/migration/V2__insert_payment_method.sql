CREATE EXTENSION IF NOT EXISTS "pgcrypto";
INSERT INTO payment_method (id, code, name, is_active, created_at)
VALUES
    (gen_random_uuid(), 'MOMO', 'MoMo Wallet', true, NOW()),
    (gen_random_uuid(), 'VNPAY', 'VNPay Gateway', true, NOW());