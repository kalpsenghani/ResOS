package com.resos.modules.user.dto;

import java.util.List;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String phone,
        String status,
        List<String> roles,
        UUID tenantId
) {}
