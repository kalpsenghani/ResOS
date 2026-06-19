-- Phase 3: tenant isolation performance indexes

CREATE INDEX IF NOT EXISTS idx_users_tenant_active ON users(tenant_id) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_roles_tenant ON roles(tenant_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_tenant ON subscriptions(tenant_id);
