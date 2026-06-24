package com.resos.modules.order.dto;

import com.resos.modules.order.domain.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(@NotNull OrderStatus status, String notes) {}
