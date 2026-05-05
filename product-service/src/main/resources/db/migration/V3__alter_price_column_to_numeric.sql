-- Altera o tipo da coluna price de DOUBLE PRECISION para NUMERIC(10,2)
-- Necessário após mudança da entidade Product de Double para BigDecimal
ALTER TABLE product
    ALTER COLUMN price TYPE NUMERIC(10, 2) USING price::NUMERIC(10, 2);
