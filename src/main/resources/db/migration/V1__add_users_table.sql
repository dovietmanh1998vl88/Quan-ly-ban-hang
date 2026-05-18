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