package com.resos.modules.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

public record RevenueChartResponse(
        List<String> labels,
        List<BigDecimal> values
) {}
