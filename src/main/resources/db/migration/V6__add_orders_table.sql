-- db/migration/V6__add_orders_table.sql
CREATE TABLE orders (
    id           VARCHAR(36)    PRIMARY KEY,
    customer_id  VARCHAR(36)    NOT NULL,  -- Keycloak user UUID
    status       VARCHAR(50)    NOT NULL DEFAULT 'DRAFT',
    total_amount DECIMAL(15,2)  NOT NULL DEFAULT 0,
    created_at   DATETIME,
    updated_at   DATETIME
);

CREATE TABLE order_items (
    id            VARCHAR(36)    PRIMARY KEY,
    order_id      VARCHAR(36)    NOT NULL,
    product_id    VARCHAR(36)    NOT NULL,
    product_name  VARCHAR(255)   NOT NULL,  -- snapshot
    quantity      INT            NOT NULL,
    unit_price    DECIMAL(15,2)  NOT NULL,  -- snapshot
    created_at    DATETIME,
    updated_at    DATETIME,

    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id) REFERENCES orders(id)
);