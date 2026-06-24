package com.resos.modules.order.dto;

import com.resos.modules.order.domain.OrderStatus;

import java.time.Instant;
import java.util.UUID;

public record OrderStatusHistoryResponse(
        UUID id,
        OrderStatus fromStatus,
        OrderStatus toStatus,
        String notes,
        Instant createdAt
) {}
