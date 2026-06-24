package com.resos.modules.employee.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateEmployeeRequest(
        @NotNull UUID restaurantId,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @Email String email,
        String phone,
        @NotBlank String position,
        @PositiveOrZero BigDecimal hourlyRate,
        @NotNull LocalDate hireDate
) {}
