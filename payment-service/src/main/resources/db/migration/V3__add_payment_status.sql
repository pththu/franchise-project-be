CREATE INDEX idx_payment_transaction_order_id
    ON payment_transaction (order_id);

CREATE INDEX idx_payment_transaction_customer_id
    ON payment_transaction (user_id);

CREATE INDEX idx_payment_transaction_status
    ON payment_transaction (status);