package com.resos.modules.order.dto;

import java.math.BigDecimal;

public record OrderItemModifierResponse(String name, BigDecimal priceAdjustment) {}
