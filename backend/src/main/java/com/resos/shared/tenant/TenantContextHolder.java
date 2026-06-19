package com.resos.shared.tenant;

import java.util.Optional;
import java.util.UUID;

public final class TenantContextHolder {

    private static final ThreadLocal<TenantContext> CONTEXT = new ThreadLocal<>();

    private TenantContextHolder() {}

    public static void set(TenantContext context) {
        CONTEXT.set(context);
    }

    public static Optional<TenantContext> getContext() {
        return Optional.ofNullable(CONTEXT.get());
    }

    public static UUID getTenantId() {
        TenantContext context = CONTEXT.get();
        return context != null ? context.tenantId() : null;
    }

    public static UUID requireTenantId() {
        UUID tenantId = getTenantId();
        if (tenantId == null) {
            throw new com.resos.shared.exception.BusinessException(
                    "BUSINESS_RULE_VIOLATION", "Tenant context is required");
        }
        return tenantId;
    }

    public static boolean isSuperAdmin() {
        TenantContext context = CONTEXT.get();
        return context != null && context.superAdmin();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
