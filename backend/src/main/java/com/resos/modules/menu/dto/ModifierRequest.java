package com.resos.modules.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

public record ModifierRequest(
        @NotBlank String name,
        @NotNull @PositiveOrZero BigDecimal priceAdjustment,
        Boolean required
) {}
