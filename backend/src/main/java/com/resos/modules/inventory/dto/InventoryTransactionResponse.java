package com.resos.modules.inventory.dto;

import com.resos.modules.inventory.domain.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record InventoryTransactionResponse(
        UUID id,
        UUID inventoryItemId,
        TransactionType type,
        BigDecimal quantity,
        BigDecimal unitCost,
        String reference,
        String notes,
        UUID performedBy,
        Instant createdAt
) {}
