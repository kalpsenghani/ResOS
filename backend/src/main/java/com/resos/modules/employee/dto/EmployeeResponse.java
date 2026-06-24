package com.resos.modules.employee.dto;

import com.resos.modules.employee.domain.EmployeeStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record EmployeeResponse(
        UUID id,
        UUID restaurantId,
        String firstName,
        String lastName,
        String email,
        String phone,
        String position,
        BigDecimal hourlyRate,
        LocalDate hireDate,
        EmployeeStatus status,
        Instant createdAt,
        Instant updatedAt,
        int version
) {}
