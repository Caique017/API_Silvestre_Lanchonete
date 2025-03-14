DROP TABLE IF EXISTS payment;

ALTER TABLE "order" RENAME TO orders;

ALTER TABLE orders
DROP COLUMN IF EXISTS payment_method;