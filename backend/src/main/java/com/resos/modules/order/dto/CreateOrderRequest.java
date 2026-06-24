package com.resos.modules.order.dto;

import com.resos.modules.order.domain.OrderType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
        @NotNull UUID restaurantId,
        UUID tableId,
        UUID reservationId,
        String customerName,
        OrderType orderType,
        @NotEmpty @Valid List<CreateOrderItemRequest> items,
        String notes
) {}
