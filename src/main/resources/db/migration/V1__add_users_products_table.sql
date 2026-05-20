CREATE TABLE users (

    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    username VARCHAR(255)
        NOT NULL UNIQUE,

    password VARCHAR(255)
        NOT NULL,

    role VARCHAR(255),

    created_at DATETIME,

    updated_at DATETIME
);

CREATE TABLE products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    name VARCHAR(255) NOT NULL UNIQUE,

    category VARCHAR(100) NOT NULL,

    description TEXT,

    price DECIMAL(15,2) NOT NULL,

    stock INT NOT NULL,

    created_at DATETIME,

    updated_at DATETIME


);