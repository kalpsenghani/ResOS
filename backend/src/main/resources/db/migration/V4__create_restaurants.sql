CREATE TABLE restaurants (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    name            VARCHAR(255) NOT NULL,
    address         TEXT NOT NULL,
    phone           VARCHAR(20),
    email           VARCHAR(255),
    capacity        INT NOT NULL DEFAULT 50,
    opening_hours   JSONB DEFAULT '{}',
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ,
    version         INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_restaurants_tenant ON restaurants(tenant_id) WHERE deleted_at IS NULL;
