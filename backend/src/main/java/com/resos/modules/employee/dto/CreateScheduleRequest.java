package com.resos.modules.employee.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record CreateScheduleRequest(
        @NotNull UUID restaurantId,
        @NotNull LocalDate shiftDate,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        String notes
) {}
