package com.resos.modules.employee.dto;

import com.resos.modules.employee.domain.ScheduleStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record UpdateScheduleRequest(
        LocalDate shiftDate,
        LocalTime startTime,
        LocalTime endTime,
        ScheduleStatus status,
        String notes
) {}
