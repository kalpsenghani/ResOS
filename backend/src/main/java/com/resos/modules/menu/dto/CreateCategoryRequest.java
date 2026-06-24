package com.resos.modules.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateCategoryRequest(
        @NotNull UUID restaurantId,
        @NotBlank String name,
        String description,
        Integer sortOrder
) {}
