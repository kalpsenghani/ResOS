package com.resos.modules.auth.dto;

import java.util.List;
import java.util.UUID;

public record AuthResponse(
        String accessToken,
        long expiresIn,
        String tokenType,
        UserSummary user,
        TenantSummary tenant
) {
    public record UserSummary(
            UUID id,
            String email,
            String firstName,
            String lastName,
            List<String> roles,
            List<String> permissions
    ) {}

    public record TenantSummary(
            UUID id,
            String name,
            String slug
    ) {}
}
