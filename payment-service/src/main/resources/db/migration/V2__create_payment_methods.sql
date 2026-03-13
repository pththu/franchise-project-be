CREATE TABLE payment_methods (
                                id UUID PRIMARY KEY,
                                method_name VARCHAR(50) NOT NULL,
                                provider VARCHAR(100) NOT NULL,
                                is_active BOOLEAN,
                                created_at TIMESTAMP DEFAULT NOW()
);