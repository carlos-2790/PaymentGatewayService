CREATE TABLE payments (
    id UUID PRIMARY KEY,
    payment_reference VARCHAR(255) NOT NULL UNIQUE,
    amount NUMERIC(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(255) NOT NULL,
    payment_method VARCHAR(255) NOT NULL,
    gateway_provider VARCHAR(255) NOT NULL,
    gateway_transaction_id VARCHAR(255),
    customer_id VARCHAR(255) NOT NULL,
    merchant_id VARCHAR(255) NOT NULL,
    description TEXT,
    failure_reason VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    completed_at TIMESTAMP,
    version BIGINT
);