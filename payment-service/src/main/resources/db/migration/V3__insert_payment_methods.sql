CREATE
EXTENSION IF NOT EXISTS "pgcrypto";
INSERT INTO payment_methods (id, method_name, provider, is_active)
VALUES (gen_random_uuid(), 'MOMO', 'MoMo Wallet', true),
       (gen_random_uuid(), 'VNPAY', 'VNPay Gateway', true);