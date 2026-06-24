package com.resos.modules.order.dto;

import com.resos.modules.order.domain.OrderStatus;
import com.resos.modules.order.domain.OrderType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID restaurantId,
        String orderNumber,
        UUID tableId,
        UUID reservationId,
        String customerName,
        OrderType orderType,
        OrderStatus status,
        BigDecimal subtotal,
        BigDecimal taxAmount,
        BigDecimal tipAmount,
        BigDecimal totalAmount,
        String notes,
        List<OrderItemResponse> items,
        List<OrderStatusHistoryResponse> statusHistory,
        Instant createdAt,
        Instant updatedAt,
        Instant completedAt,
        int version
) {}
