package com.resos.modules.analytics.dto;

import java.math.BigDecimal;
import java.util.List;

public record RevenueAnalyticsResponse(
        BigDecimal totalRevenue,
        long orderCount,
        BigDecimal avgOrderValue,
        double changePercent,
        List<String> labels,
        List<BigDecimal> values
) {}
