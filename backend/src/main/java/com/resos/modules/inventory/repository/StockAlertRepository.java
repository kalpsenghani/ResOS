package com.resos.modules.inventory.repository;

import com.resos.modules.inventory.domain.AlertType;
import com.resos.modules.inventory.domain.StockAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockAlertRepository extends JpaRepository<StockAlert, UUID> {

    Optional<StockAlert> findByIdAndTenantId(UUID id, UUID tenantId);

    List<StockAlert> findByTenantIdAndAcknowledgedOrderByCreatedAtDesc(UUID tenantId, boolean acknowledged);

    @org.springframework.data.jpa.repository.Query("""
            SELECT a FROM StockAlert a
            JOIN InventoryItem i ON i.id = a.inventoryItemId
            WHERE a.tenantId = :tenantId
              AND i.restaurantId = :restaurantId
              AND a.acknowledged = :acknowledged
            ORDER BY a.createdAt DESC
            """)
    List<StockAlert> findAlertsForRestaurant(
            @org.springframework.data.repository.query.Param("tenantId") UUID tenantId,
            @org.springframework.data.repository.query.Param("restaurantId") UUID restaurantId,
            @org.springframework.data.repository.query.Param("acknowledged") boolean acknowledged);
}
