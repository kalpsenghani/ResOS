package com.resos.modules.order.dto;

import com.resos.modules.order.domain.OrderItemStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderItemResponse(
        UUID id,
        UUID menuItemId,
        String menuItemName,
        int quantity,
        BigDecimal unitPrice,
        List<OrderItemModifierResponse> modifiers,
        String specialInstructions,
        OrderItemStatus status,
        Instant createdAt
) {}
