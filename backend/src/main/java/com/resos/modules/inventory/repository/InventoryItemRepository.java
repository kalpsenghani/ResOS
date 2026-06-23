package com.resos.modules.inventory.repository;

import com.resos.modules.inventory.domain.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, UUID>, JpaSpecificationExecutor<InventoryItem> {

    Optional<InventoryItem> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, UUID tenantId);

    @Query("""
            SELECT COUNT(i) FROM InventoryItem i
            WHERE i.tenantId = :tenantId
              AND i.deletedAt IS NULL
              AND i.restaurantId = :restaurantId
              AND i.currentStock <= i.minimumStock
            """)
    long countLowStockByRestaurant(@Param("tenantId") UUID tenantId, @Param("restaurantId") UUID restaurantId);
}
