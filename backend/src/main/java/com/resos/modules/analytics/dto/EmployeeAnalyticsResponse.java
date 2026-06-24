package com.resos.modules.analytics.dto;

import java.math.BigDecimal;
import java.util.List;

public record EmployeeAnalyticsResponse(
        long activeEmployees,
        long scheduledShifts,
        BigDecimal totalHours,
        BigDecimal estimatedLaborCost,
        List<PositionSummary> byPosition
) {}
