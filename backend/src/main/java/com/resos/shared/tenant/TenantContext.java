package com.resos.shared.tenant;

import java.util.UUID;

public record TenantContext(UUID tenantId, boolean superAdmin) {

    public static TenantContext forTenant(UUID tenantId) {
        return new TenantContext(tenantId, false);
    }

    public static TenantContext forSuperAdmin() {
        return new TenantContext(null, true);
    }

    public static TenantContext forSuperAdminWithTenant(UUID tenantId) {
        return new TenantContext(tenantId, true);
    }
}
