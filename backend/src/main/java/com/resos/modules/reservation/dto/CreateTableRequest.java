package com.resos.modules.reservation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateTableRequest(
        @NotNull UUID restaurantId,
        @NotBlank String tableNumber,
        @NotNull @Min(1) Integer capacity,
        String location
) {}
