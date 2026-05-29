-- db/migration/V2__change_id_to_uuid.sql

-- Xóa table cũ và tạo lại với UUID
-- (môi trường dev — production cần migration phức tạp hơn)

DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id          VARCHAR(36)  PRIMARY KEY,
    username    VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(255) NOT NULL DEFAULT 'CUSTOMER',
    created_at  DATETIME,
    updated_at  DATETIME
);

CREATE TABLE products (
    id          VARCHAR(36)   PRIMARY KEY,
    name        VARCHAR(255)  NOT NULL UNIQUE,
    category    VARCHAR(100)  NOT NULL,
    description TEXT,
    price       DECIMAL(15,2) NOT NULL,
    stock       INT           NOT NULL,
    created_at  DATETIME,
    updated_at  DATETIME
);