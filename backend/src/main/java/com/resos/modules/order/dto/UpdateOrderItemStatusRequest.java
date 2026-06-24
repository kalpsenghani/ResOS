package com.resos.modules.order.dto;

import com.resos.modules.order.domain.OrderItemStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderItemStatusRequest(@NotNull OrderItemStatus status) {}
