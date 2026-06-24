package com.resos.modules.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CreateMenuItemRequest(
        @NotNull UUID categoryId,
        @NotBlank String name,
        String description,
        @NotNull @Positive BigDecimal price,
        @PositiveOrZero BigDecimal cost,
        String imageUrl,
        Integer preparationTime,
        List<String> allergens,
        Integer sortOrder,
        List<ModifierRequest> modifiers
) {}
