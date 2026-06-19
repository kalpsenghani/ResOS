package com.resos.modules.tenant.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record TenantResponse(
        UUID id,
        String name,
        String slug,
        String email,
        String phone,
        String timezone,
        String currency,
        String locale,
        String status,
        Map<String, Object> settings,
        SubscriptionSummary subscription
) {
    public record SubscriptionSummary(
            String plan,
            String status,
            Instant currentPeriodEnd,
            Instant trialEndsAt
    ) {}
}
