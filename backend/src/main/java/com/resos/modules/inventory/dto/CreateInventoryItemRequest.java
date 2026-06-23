package com.resos.modules.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateInventoryItemRequest(
        @NotNull UUID restaurantId,
        @NotBlank String name,
        String sku,
        String category,
        @NotBlank String unit,
        @PositiveOrZero BigDecimal currentStock,
        @PositiveOrZero BigDecimal minimumStock,
        @PositiveOrZero BigDecimal maximumStock,
        @PositiveOrZero BigDecimal unitCost,
        String supplier
) {}
