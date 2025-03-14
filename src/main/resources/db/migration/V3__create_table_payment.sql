CREATE TABLE payment(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    value DOUBLE PRECISION NOT NULL,
    payment_date TIMESTAMP NOT NULL,
    status VARCHAR(30) NOT NULL,
    order_id UUID,
    FOREIGN KEY (order_id) REFERENCES "order" (id) ON DELETE CASCADE
);