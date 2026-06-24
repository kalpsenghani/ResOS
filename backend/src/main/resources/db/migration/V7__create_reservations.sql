CREATE TABLE restaurant_tables (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    restaurant_id   UUID NOT NULL REFERENCES restaurants(id),
    table_number    VARCHAR(20) NOT NULL,
    capacity        INT NOT NULL,
    location        VARCHAR(100),
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tables_tenant ON restaurant_tables(tenant_id);
CREATE INDEX idx_tables_restaurant ON restaurant_tables(restaurant_id);
CREATE UNIQUE INDEX idx_tables_restaurant_number ON restaurant_tables(restaurant_id, table_number);

CREATE TABLE reservations (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    restaurant_id   UUID NOT NULL REFERENCES restaurants(id),
    table_id        UUID REFERENCES restaurant_tables(id),
    guest_name      VARCHAR(255) NOT NULL,
    guest_phone     VARCHAR(20),
    guest_email     VARCHAR(255),
    party_size      INT NOT NULL,
    reservation_date DATE NOT NULL,
    start_time      TIME NOT NULL,
    end_time        TIME,
    status          VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
    special_requests TEXT,
    created_by      UUID REFERENCES users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    version         INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_reservations_tenant_date ON reservations(tenant_id, reservation_date);
CREATE INDEX idx_reservations_restaurant ON reservations(restaurant_id, reservation_date);
CREATE INDEX idx_reservations_table ON reservations(table_id, reservation_date);
