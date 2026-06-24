package com.resos.modules.analytics.dto;

import java.math.BigDecimal;
import java.util.List;

public record InventoryAnalyticsResponse(
        long totalItems,
        long lowStockItems,
        BigDecimal inventoryValue,
        long wasteTransactions,
        BigDecimal wasteCost,
        long usageTransactions,
        List<CategoryStockSummary> topCategories
) {}
