package com.resos.modules.audit.dto;

import com.resos.modules.audit.domain.AuditAction;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        UUID userId,
        AuditAction action,
        String entityType,
        UUID entityId,
        Map<String, Object> oldValues,
        Map<String, Object> newValues,
        Instant createdAt
) {}
