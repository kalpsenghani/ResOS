package com.resos.modules.dashboard.dto;

public record DashboardKpiResponse(
        KpiMetric revenue,
        KpiMetric orders,
        KpiMetric reservations,
        KpiMetric lowStockItems,
        KpiMetric activeEmployees,
        KpiMetric avgOrderValue
) {}
