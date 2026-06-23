package com.resos.modules.audit.service;

import com.resos.modules.audit.domain.AuditAction;
import com.resos.modules.audit.domain.AuditLog;
import com.resos.modules.audit.dto.AuditLogResponse;
import com.resos.modules.audit.repository.AuditLogRepository;
import com.resos.modules.audit.repository.AuditLogSpecifications;
import com.resos.shared.api.ApiResponse;
import com.resos.shared.tenant.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void log(
            AuditAction action,
            String entityType,
            UUID entityId,
            Map<String, Object> oldValues,
            Map<String, Object> newValues,
            UUID userId) {
        UUID tenantId = TenantContextHolder.getTenantId();
        AuditLog entry = AuditLog.builder()
                .tenantId(tenantId)
                .userId(userId)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .oldValues(oldValues)
                .newValues(newValues)
                .build();
        auditLogRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public ApiResponse<java.util.List<AuditLogResponse>> list(
            String entityType,
            UUID entityId,
            AuditAction action,
            Instant startDate,
            Pageable pageable) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        Specification<AuditLog> spec = AuditLogSpecifications.forTenant(tenantId);
        if (entityType != null) {
            spec = spec.and(AuditLogSpecifications.forEntityType(entityType));
        }
        if (entityId != null) {
            spec = spec.and(AuditLogSpecifications.forEntityId(entityId));
        }
        if (action != null) {
            spec = spec.and(AuditLogSpecifications.forAction(action));
        }
        if (startDate != null) {
            spec = spec.and(AuditLogSpecifications.since(startDate));
        }

        Sort sort = pageable.getSort().isSorted()
                ? pageable.getSort().and(Sort.by(Sort.Direction.DESC, "createdAt"))
                : Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable sorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
        Page<AuditLog> page = auditLogRepository.findAll(spec, sorted);
        var data = page.getContent().stream().map(this::toResponse).toList();
        return ApiResponse.page(data, new ApiResponse.PageMeta(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()));
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getUserId(),
                log.getAction(),
                log.getEntityType(),
                log.getEntityId(),
                log.getOldValues(),
                log.getNewValues(),
                log.getCreatedAt());
    }
}
