package com.resos.modules.dashboard.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RecentOrderResponse(
        UUID id,
        String orderNumber,
        String customerName,
        String status,
        BigDecimal totalAmount,
        Instant createdAt
) {}
