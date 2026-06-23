package com.resos.modules.inventory.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record InventoryItemResponse(
        UUID id,
        String name,
        String sku,
        String category,
        String unit,
        BigDecimal currentStock,
        BigDecimal minimumStock,
        BigDecimal maximumStock,
        BigDecimal unitCost,
        String supplier,
        LocalDate expiryDate,
        boolean isLowStock,
        UUID restaurantId,
        Instant createdAt,
        Instant updatedAt,
        int version
) {}
