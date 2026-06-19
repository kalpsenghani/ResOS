package com.resos.shared.tenant;

import java.util.UUID;

/**
 * Marker for entities scoped by {@code tenant_id}.
 */
public interface TenantAware {

    UUID getTenantId();
}
