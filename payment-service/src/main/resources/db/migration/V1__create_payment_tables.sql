CREATE TABLE payment_method (
                                id UUID PRIMARY KEY,
                                method_name VARCHAR(50) NOT NULL,
                                provider VARCHAR(100) NOT NULL,
                                is_active VARCHAR(20),
                                created_at TIMESTAMP DEFAULT NOW()
);