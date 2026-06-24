package com.resos.modules.employee.dto;

import com.resos.modules.employee.domain.EmployeeStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateEmployeeRequest(
        String firstName,
        String lastName,
        @Email String email,
        String phone,
        String position,
        @PositiveOrZero BigDecimal hourlyRate,
        LocalDate hireDate,
        EmployeeStatus status
) {}
