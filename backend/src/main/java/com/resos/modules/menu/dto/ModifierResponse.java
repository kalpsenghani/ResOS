package com.resos.modules.menu.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ModifierResponse(UUID id, String name, BigDecimal priceAdjustment, boolean required) {}
