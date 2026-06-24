package com.resos.modules.employee.dto;

import com.resos.modules.employee.domain.ScheduleStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ScheduleResponse(
        UUID id,
        UUID employeeId,
        UUID restaurantId,
        LocalDate shiftDate,
        LocalTime startTime,
        LocalTime endTime,
        ScheduleStatus status,
        String notes,
        Instant createdAt
) {}
