package com.resos.modules.menu.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record UpdateMenuItemRequest(
        UUID categoryId,
        String name,
        String description,
        @Positive BigDecimal price,
        @PositiveOrZero BigDecimal cost,
        String imageUrl,
        Integer preparationTime,
        List<String> allergens,
        Integer sortOrder,
        Boolean available,
        List<ModifierRequest> modifiers
) {}
