package com.resos.modules.inventory.dto;

import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateInventoryItemRequest(
        String name,
        String sku,
        String category,
        String unit,
        @PositiveOrZero BigDecimal currentStock,
        @PositiveOrZero BigDecimal minimumStock,
        @PositiveOrZero BigDecimal maximumStock,
        @PositiveOrZero BigDecimal unitCost,
        String supplier,
        LocalDate expiryDate
) {}
