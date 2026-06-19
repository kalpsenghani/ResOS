package com.resos.shared.tenant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Base repository for tenant-scoped aggregates.
 * Hibernate {@code tenantFilter} auto-applies {@code tenant_id} predicates at runtime.
 */
@NoRepositoryBean
public interface TenantAwareRepository<T, ID> extends JpaRepository<T, ID> {
}
