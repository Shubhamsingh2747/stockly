CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(32)  NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE categories (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(120) NOT NULL UNIQUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE products (
    id              BIGSERIAL PRIMARY KEY,
    sku             VARCHAR(64)  NOT NULL UNIQUE,
    name            VARCHAR(255) NOT NULL,
    category_id     BIGINT       NOT NULL REFERENCES categories (id),
    unit_price      NUMERIC(12, 2) NOT NULL,
    stock_quantity  INTEGER      NOT NULL DEFAULT 0,
    reorder_level   INTEGER      NOT NULL DEFAULT 0,
    version         BIGINT       NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT products_stock_non_negative CHECK (stock_quantity >= 0),
    CONSTRAINT products_reorder_non_negative CHECK (reorder_level >= 0)
);

CREATE INDEX idx_products_category ON products (category_id);
CREATE INDEX idx_products_low_stock ON products (stock_quantity, reorder_level);

CREATE TABLE sales_orders (
    id              BIGSERIAL PRIMARY KEY,
    status          VARCHAR(32) NOT NULL,
    created_by      BIGINT      NOT NULL REFERENCES users (id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE sales_order_lines (
    id              BIGSERIAL PRIMARY KEY,
    sales_order_id  BIGINT  NOT NULL REFERENCES sales_orders (id) ON DELETE CASCADE,
    product_id      BIGINT  NOT NULL REFERENCES products (id),
    quantity        INTEGER NOT NULL,
    unit_price      NUMERIC(12, 2) NOT NULL,
    CONSTRAINT sales_order_lines_qty_positive CHECK (quantity > 0)
);

CREATE TABLE purchase_orders (
    id              BIGSERIAL PRIMARY KEY,
    status          VARCHAR(32) NOT NULL,
    created_by      BIGINT      NOT NULL REFERENCES users (id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE purchase_order_lines (
    id                  BIGSERIAL PRIMARY KEY,
    purchase_order_id   BIGINT  NOT NULL REFERENCES purchase_orders (id) ON DELETE CASCADE,
    product_id          BIGINT  NOT NULL REFERENCES products (id),
    quantity            INTEGER NOT NULL,
    unit_cost           NUMERIC(12, 2) NOT NULL,
    CONSTRAINT purchase_order_lines_qty_positive CHECK (quantity > 0)
);

CREATE TABLE stock_movements (
    id              BIGSERIAL PRIMARY KEY,
    product_id      BIGINT      NOT NULL REFERENCES products (id),
    movement_type   VARCHAR(32) NOT NULL,
    quantity_delta  INTEGER     NOT NULL,
    quantity_after  INTEGER     NOT NULL,
    reference       VARCHAR(128),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_stock_movements_product ON stock_movements (product_id, created_at DESC);
