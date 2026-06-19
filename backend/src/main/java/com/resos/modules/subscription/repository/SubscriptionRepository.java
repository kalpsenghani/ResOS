package com.resos.modules.subscription.repository;

import com.resos.modules.subscription.domain.Subscription;
import com.resos.shared.tenant.TenantAwareRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends TenantAwareRepository<Subscription, UUID> {

    @Query("""
            SELECT s FROM Subscription s
            JOIN FETCH s.plan
            WHERE s.tenant.id = :tenantId
            """)
    Optional<Subscription> findByTenantIdWithPlan(@Param("tenantId") UUID tenantId);
}
