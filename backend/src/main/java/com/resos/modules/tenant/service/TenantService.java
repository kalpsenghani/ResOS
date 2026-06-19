package com.resos.modules.tenant.service;

import com.resos.modules.subscription.domain.Subscription;
import com.resos.modules.subscription.repository.SubscriptionRepository;
import com.resos.modules.tenant.domain.Tenant;
import com.resos.modules.tenant.domain.TenantStatus;
import com.resos.modules.tenant.dto.TenantResponse;
import com.resos.modules.tenant.dto.UpdateTenantRequest;
import com.resos.modules.tenant.repository.TenantRepository;
import com.resos.shared.exception.BusinessException;
import com.resos.shared.tenant.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Transactional(readOnly = true)
    public TenantResponse getCurrentTenant() {
        UUID tenantId = TenantContextHolder.requireTenantId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "Tenant not found"));

        Subscription subscription = subscriptionRepository.findByTenantIdWithPlan(tenantId).orElse(null);
        return toResponse(tenant, subscription);
    }

    @Transactional
    public TenantResponse updateCurrentTenant(UpdateTenantRequest request) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "Tenant not found"));

        if (request.name() != null) {
            tenant.setName(request.name());
        }
        if (request.phone() != null) {
            tenant.setPhone(request.phone());
        }
        if (request.timezone() != null) {
            tenant.setTimezone(request.timezone());
        }
        if (request.currency() != null) {
            tenant.setCurrency(request.currency());
        }
        if (request.settings() != null) {
            tenant.setSettings(new HashMap<>(request.settings()));
        }

        tenantRepository.save(tenant);
        Subscription subscription = subscriptionRepository.findByTenantIdWithPlan(tenantId).orElse(null);
        return toResponse(tenant, subscription);
    }

    private TenantResponse toResponse(Tenant tenant, Subscription subscription) {
        TenantResponse.SubscriptionSummary subscriptionSummary = null;
        if (subscription != null && subscription.getPlan() != null) {
            subscriptionSummary = new TenantResponse.SubscriptionSummary(
                    subscription.getPlan().getName(),
                    subscription.getStatus().name(),
                    subscription.getCurrentPeriodEnd(),
                    subscription.getTrialEndsAt()
            );
        }

        return new TenantResponse(
                tenant.getId(),
                tenant.getName(),
                tenant.getSlug(),
                tenant.getEmail(),
                tenant.getPhone(),
                tenant.getTimezone(),
                tenant.getCurrency(),
                tenant.getLocale(),
                tenant.getStatus().name(),
                tenant.getSettings(),
                subscriptionSummary
        );
    }
}
