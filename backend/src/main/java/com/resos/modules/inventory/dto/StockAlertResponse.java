package com.resos.modules.inventory.dto;

import com.resos.modules.inventory.domain.AlertType;

import java.time.Instant;
import java.util.UUID;

public record StockAlertResponse(
        UUID id,
        UUID inventoryItemId,
        String itemName,
        AlertType alertType,
        String message,
        boolean acknowledged,
        UUID acknowledgedBy,
        Instant acknowledgedAt,
        Instant createdAt
) {}
