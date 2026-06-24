package com.resos.modules.reservation.repository;

import com.resos.modules.reservation.domain.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, UUID> {

    List<RestaurantTable> findByTenantIdAndRestaurantIdOrderByTableNumberAsc(UUID tenantId, UUID restaurantId);

    List<RestaurantTable> findByTenantIdAndRestaurantIdAndActiveTrueOrderByTableNumberAsc(
            UUID tenantId, UUID restaurantId);

    Optional<RestaurantTable> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByTenantIdAndRestaurantIdAndTableNumber(UUID tenantId, UUID restaurantId, String tableNumber);

    boolean existsByTenantIdAndRestaurantIdAndTableNumberAndIdNot(
            UUID tenantId, UUID restaurantId, String tableNumber, UUID id);
}
