package com.resos.modules.order.repository;

import com.resos.modules.order.domain.OrderStatus;
import com.resos.modules.order.domain.RestaurantOrder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RestaurantOrderRepository
        extends JpaRepository<RestaurantOrder, UUID>, JpaSpecificationExecutor<RestaurantOrder> {

    Optional<RestaurantOrder> findByIdAndTenantId(UUID id, UUID tenantId);

    List<RestaurantOrder> findByTenantIdAndRestaurantIdOrderByCreatedAtDesc(
            UUID tenantId, UUID restaurantId, Pageable pageable);

    long countByTenantIdAndRestaurantIdAndCreatedAtBetweenAndStatusNotIn(
            UUID tenantId,
            UUID restaurantId,
            Instant start,
            Instant end,
            Collection<OrderStatus> excludedStatuses);

    @Query("""
            SELECT COALESCE(SUM(o.totalAmount), 0)
            FROM RestaurantOrder o
            WHERE o.tenantId = :tenantId
              AND o.restaurantId = :restaurantId
              AND o.status = 'COMPLETED'
              AND o.createdAt >= :start
              AND o.createdAt < :end
            """)
    BigDecimal sumCompletedRevenue(
            @Param("tenantId") UUID tenantId,
            @Param("restaurantId") UUID restaurantId,
            @Param("start") Instant start,
            @Param("end") Instant end);

    long countByTenantIdAndRestaurantIdAndCreatedAtBetween(
            UUID tenantId, UUID restaurantId, Instant start, Instant end);
}
