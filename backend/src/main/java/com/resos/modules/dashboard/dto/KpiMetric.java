package com.resos.modules.dashboard.dto;

import java.math.BigDecimal;

public record KpiMetric(
        BigDecimal value,
        double change,
        Trend trend
) {
    public enum Trend {
        UP, DOWN, FLAT
    }
}
