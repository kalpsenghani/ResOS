package com.resos.modules.menu.dto;

import java.time.Instant;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        UUID restaurantId,
        String name,
        String description,
        int sortOrder,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {}
