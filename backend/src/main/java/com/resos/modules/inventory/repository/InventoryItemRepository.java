package com.resos.modules.inventory.repository;

import com.resos.modules.inventory.domain.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, UUID>, JpaSpecificationExecutor<InventoryItem> {

    Optional<InventoryItem> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, UUID tenantId);

    long countByTenantIdAndRestaurantIdAndDeletedAtIsNull(UUID tenantId, UUID restaurantId);

    List<InventoryItem> findByTenantIdAndRestaurantIdAndDeletedAtIsNull(UUID tenantId, UUID restaurantId);

    @Query("""
            SELECT COUNT(i) FROM InventoryItem i
            WHERE i.tenantId = :tenantId
              AND i.deletedAt IS NULL
              AND i.restaurantId = :restaurantId
              AND i.currentStock <= i.minimumStock
            """)
    long countLowStockByRestaurant(@Param("tenantId") UUID tenantId, @Param("restaurantId") UUID restaurantId);

    @Query("""
            SELECT COALESCE(SUM(i.currentStock * COALESCE(i.unitCost, 0)), 0)
            FROM InventoryItem i
            WHERE i.tenantId = :tenantId
              AND i.restaurantId = :restaurantId
              AND i.deletedAt IS NULL
            """)
    BigDecimal sumInventoryValue(@Param("tenantId") UUID tenantId, @Param("restaurantId") UUID restaurantId);
}
