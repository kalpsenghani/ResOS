package com.resos.modules.order.domain;

import java.math.BigDecimal;

public record ModifierSnapshot(String name, BigDecimal priceAdjustment) {}
