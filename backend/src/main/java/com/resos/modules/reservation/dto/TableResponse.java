package com.resos.modules.reservation.dto;

import java.time.Instant;
import java.util.UUID;

public record TableResponse(
        UUID id,
        UUID restaurantId,
        String tableNumber,
        int capacity,
        String location,
        boolean active,
        Instant createdAt
) {}
