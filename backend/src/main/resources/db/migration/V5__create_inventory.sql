CREATE TABLE inventory_items (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    restaurant_id   UUID NOT NULL REFERENCES restaurants(id),
    name            VARCHAR(255) NOT NULL,
    sku             VARCHAR(50),
    category        VARCHAR(100),
    unit            VARCHAR(20) NOT NULL,
    current_stock   DECIMAL(10,3) NOT NULL DEFAULT 0,
    minimum_stock   DECIMAL(10,3) NOT NULL DEFAULT 0,
    maximum_stock   DECIMAL(10,3),
    unit_cost       DECIMAL(10,2),
    supplier        VARCHAR(255),
    expiry_date     DATE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ,
    version         INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_inventory_tenant ON inventory_items(tenant_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_inventory_restaurant ON inventory_items(restaurant_id);
CREATE INDEX idx_inventory_low_stock ON inventory_items(tenant_id)
    WHERE current_stock <= minimum_stock AND deleted_at IS NULL;

CREATE TABLE inventory_transactions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID NOT NULL REFERENCES tenants(id),
    inventory_item_id   UUID NOT NULL REFERENCES inventory_items(id),
    type                VARCHAR(20) NOT NULL,
    quantity            DECIMAL(10,3) NOT NULL,
    unit_cost           DECIMAL(10,2),
    reference           VARCHAR(255),
    notes               TEXT,
    performed_by        UUID REFERENCES users(id),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_inv_txn_tenant ON inventory_transactions(tenant_id);
CREATE INDEX idx_inv_txn_item ON inventory_transactions(inventory_item_id);

CREATE TABLE stock_alerts (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID NOT NULL REFERENCES tenants(id),
    inventory_item_id   UUID NOT NULL REFERENCES inventory_items(id),
    alert_type          VARCHAR(20) NOT NULL,
    message             TEXT NOT NULL,
    is_acknowledged     BOOLEAN NOT NULL DEFAULT FALSE,
    acknowledged_by     UUID REFERENCES users(id),
    acknowledged_at     TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_stock_alerts_tenant ON stock_alerts(tenant_id, is_acknowledged);

CREATE TABLE audit_logs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID REFERENCES tenants(id),
    user_id         UUID REFERENCES users(id),
    action          VARCHAR(50) NOT NULL,
    entity_type     VARCHAR(100) NOT NULL,
    entity_id       UUID,
    old_values      JSONB,
    new_values      JSONB,
    ip_address      VARCHAR(45),
    user_agent      VARCHAR(500),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_tenant ON audit_logs(tenant_id, created_at DESC);
CREATE INDEX idx_audit_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_user ON audit_logs(user_id, created_at DESC);
