package com.resos.modules.inventory.repository;

import com.resos.modules.inventory.domain.InventoryTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, UUID> {

    Page<InventoryTransaction> findByInventoryItemIdAndTenantIdOrderByCreatedAtDesc(
            UUID inventoryItemId, UUID tenantId, Pageable pageable);
}
