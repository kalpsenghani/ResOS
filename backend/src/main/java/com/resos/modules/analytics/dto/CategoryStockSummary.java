package com.resos.modules.analytics.dto;

import java.math.BigDecimal;

public record CategoryStockSummary(String category, long itemCount, BigDecimal stockValue) {}
