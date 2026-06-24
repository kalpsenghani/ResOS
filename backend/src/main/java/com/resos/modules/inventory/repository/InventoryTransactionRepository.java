package com.resos.modules.inventory.repository;

import com.resos.modules.inventory.domain.InventoryTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, UUID> {

    Page<InventoryTransaction> findByInventoryItemIdAndTenantIdOrderByCreatedAtDesc(
            UUID inventoryItemId, UUID tenantId, Pageable pageable);

    @Query("""
            SELECT COUNT(t) FROM InventoryTransaction t
            JOIN InventoryItem i ON i.id = t.inventoryItemId
            WHERE t.tenantId = :tenantId
              AND i.restaurantId = :restaurantId
              AND t.type = :type
              AND t.createdAt >= :start
              AND t.createdAt < :end
            """)
    long countByRestaurantAndTypeBetween(
            @Param("tenantId") UUID tenantId,
            @Param("restaurantId") UUID restaurantId,
            @Param("type") com.resos.modules.inventory.domain.TransactionType type,
            @Param("start") Instant start,
            @Param("end") Instant end);

    @Query("""
            SELECT COALESCE(SUM(ABS(t.quantity) * COALESCE(t.unitCost, 0)), 0)
            FROM InventoryTransaction t
            JOIN InventoryItem i ON i.id = t.inventoryItemId
            WHERE t.tenantId = :tenantId
              AND i.restaurantId = :restaurantId
              AND t.type = com.resos.modules.inventory.domain.TransactionType.WASTE
              AND t.createdAt >= :start
              AND t.createdAt < :end
            """)
    BigDecimal sumWasteCostByRestaurantBetween(
            @Param("tenantId") UUID tenantId,
            @Param("restaurantId") UUID restaurantId,
            @Param("start") Instant start,
            @Param("end") Instant end);
}
