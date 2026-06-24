package com.resos.modules.reservation.dto;

import jakarta.validation.constraints.Min;

public record UpdateTableRequest(
        String tableNumber,
        @Min(1) Integer capacity,
        String location,
        Boolean active
) {}
