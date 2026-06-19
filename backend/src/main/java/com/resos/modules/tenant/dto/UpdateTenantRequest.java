package com.resos.modules.tenant.dto;

import jakarta.validation.constraints.Size;

import java.util.Map;

public record UpdateTenantRequest(
        @Size(min = 2, max = 255) String name,
        @Size(max = 20) String phone,
        @Size(max = 50) String timezone,
        @Size(min = 3, max = 3) String currency,
        Map<String, Object> settings
) {}
