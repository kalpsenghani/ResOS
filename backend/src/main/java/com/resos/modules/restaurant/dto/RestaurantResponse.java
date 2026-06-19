package com.resos.modules.restaurant.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record RestaurantResponse(
        UUID id,
        String name,
        String address,
        String phone,
        String email,
        int capacity,
        Map<String, Object> openingHours,
        boolean active,
        UUID tenantId,
        Instant createdAt,
        int version
) {}
