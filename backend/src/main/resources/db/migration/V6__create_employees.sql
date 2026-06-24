CREATE TABLE employees (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    restaurant_id   UUID NOT NULL REFERENCES restaurants(id),
    user_id         UUID REFERENCES users(id),
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    email           VARCHAR(255),
    phone           VARCHAR(20),
    position        VARCHAR(100) NOT NULL,
    hourly_rate     DECIMAL(8,2),
    hire_date       DATE NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ,
    version         INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_employees_tenant ON employees(tenant_id) WHERE deleted_at IS NULL;
CREATE INDEX idx_employees_restaurant ON employees(restaurant_id);

CREATE TABLE employee_schedules (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants(id),
    employee_id     UUID NOT NULL REFERENCES employees(id),
    restaurant_id   UUID NOT NULL REFERENCES restaurants(id),
    shift_date      DATE NOT NULL,
    start_time      TIME NOT NULL,
    end_time        TIME NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    notes           TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_schedules_tenant_date ON employee_schedules(tenant_id, shift_date);
CREATE INDEX idx_schedules_employee ON employee_schedules(employee_id, shift_date);
