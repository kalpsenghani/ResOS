package com.resos.modules.menu.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MenuItemResponse(
        UUID id,
        UUID categoryId,
        String name,
        String description,
        BigDecimal price,
        BigDecimal cost,
        String imageUrl,
        boolean available,
        Integer preparationTime,
        List<String> allergens,
        int sortOrder,
        List<ModifierResponse> modifiers,
        Instant createdAt,
        Instant updatedAt,
        int version
) {}
