CREATE TABLE order_product (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    amount INTEGER NOT NULL,
    price DOUBLE PRECISION NOT NULL,
    product_id UUID,
    order_id UUID,
    FOREIGN KEY (product_id) REFERENCES "product" (id) ON DELETE CASCADE,
    FOREIGN KEY (order_id) REFERENCES "order" (id) ON DELETE CASCADE
);