CREATE TABLE product (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255) NOT NULL,
    price DOUBLE PRECISION NOT NULL,
    category VARCHAR(70) NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    available BOOLEAN DEFAULT TRUE
);