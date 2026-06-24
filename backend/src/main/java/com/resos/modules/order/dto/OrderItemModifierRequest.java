package com.resos.modules.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

public record OrderItemModifierRequest(
        @NotBlank String name, @NotNull @PositiveOrZero BigDecimal priceAdjustment) {}
