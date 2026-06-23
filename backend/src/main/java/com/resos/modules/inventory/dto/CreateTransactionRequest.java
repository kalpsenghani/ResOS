package com.resos.modules.inventory.dto;

import com.resos.modules.inventory.domain.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateTransactionRequest(
        @NotNull TransactionType type,
        @NotNull @Positive BigDecimal quantity,
        BigDecimal unitCost,
        String reference,
        String notes
) {}
