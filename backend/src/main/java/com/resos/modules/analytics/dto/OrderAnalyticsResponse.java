package com.resos.modules.analytics.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderAnalyticsResponse(
        long totalOrders,
        long completedOrders,
        BigDecimal avgTicket,
        List<PeakHourSummary> peakHours,
        List<StatusSummary> byStatus
) {}
