package com.resos.modules.restaurant.repository;

import com.resos.modules.restaurant.domain.Restaurant;
import com.resos.shared.tenant.TenantAwareRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RestaurantRepository extends TenantAwareRepository<Restaurant, UUID> {

    List<Restaurant> findByTenantIdAndDeletedAtIsNull(UUID tenantId);

    Optional<Restaurant> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, UUID tenantId);

    long countByTenantIdAndDeletedAtIsNullAndActiveTrue(UUID tenantId);
}
