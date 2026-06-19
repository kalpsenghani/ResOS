package com.resos.shared.tenant;

import com.resos.shared.exception.BusinessException;
import com.resos.shared.security.UserPrincipal;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Component
@Slf4j
public class TenantFilter extends OncePerRequestFilter {

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/actuator/health",
            "/actuator/info",
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh"
    );

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (isPublicPath(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            resolveAndSetTenantContext(request);
            enableHibernateFilter();
            filterChain.doFilter(request, response);
        } catch (BusinessException ex) {
            response.setStatus(mapStatus(ex.getCode()));
            response.setContentType("application/json");
            response.getWriter().write("""
                    {"error":{"code":"%s","message":"%s"}}\
                    """.formatted(ex.getCode(), ex.getMessage()));
        } finally {
            disableHibernateFilter();
            TenantContextHolder.clear();
        }
    }

    private void resolveAndSetTenantContext(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            return;
        }

        String headerTenantId = request.getHeader(TenantConstants.TENANT_HEADER);
        boolean isSuperAdmin = principal.getRoles().contains("SUPER_ADMIN");

        if (isSuperAdmin) {
            if (headerTenantId != null && !headerTenantId.isBlank()) {
                TenantContextHolder.set(TenantContext.forSuperAdminWithTenant(parseTenantId(headerTenantId)));
            } else {
                TenantContextHolder.set(TenantContext.forSuperAdmin());
            }
            return;
        }

        UUID jwtTenantId = principal.getTenantId();
        if (jwtTenantId == null) {
            throw new BusinessException("TENANT_ACCESS_DENIED", "Tenant context is missing from token");
        }

        if (headerTenantId == null || headerTenantId.isBlank()) {
            throw new BusinessException("VALIDATION_ERROR", "X-Tenant-ID header is required");
        }

        UUID requestTenantId = parseTenantId(headerTenantId);
        if (!jwtTenantId.equals(requestTenantId)) {
            throw new BusinessException("TENANT_MISMATCH", "X-Tenant-ID does not match authenticated tenant");
        }

        TenantContextHolder.set(TenantContext.forTenant(jwtTenantId));
    }

    private UUID parseTenantId(String raw) {
        try {
            return UUID.fromString(raw.trim());
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("VALIDATION_ERROR", "X-Tenant-ID must be a valid UUID");
        }
    }

    private void enableHibernateFilter() {
        UUID tenantId = TenantContextHolder.getTenantId();
        if (tenantId == null || entityManager == null) {
            return;
        }
        Session session = entityManager.unwrap(Session.class);
        session.enableFilter("tenantFilter").setParameter("tenantId", tenantId);
    }

    private void disableHibernateFilter() {
        if (entityManager == null) {
            return;
        }
        try {
            Session session = entityManager.unwrap(Session.class);
            if (session.getEnabledFilter("tenantFilter") != null) {
                session.disableFilter("tenantFilter");
            }
        } catch (Exception ex) {
            log.trace("Could not disable tenant filter", ex);
        }
    }

    private boolean isPublicPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith)
                || "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    private int mapStatus(String code) {
        return switch (code) {
            case "VALIDATION_ERROR" -> HttpServletResponse.SC_BAD_REQUEST;
            case "TENANT_MISMATCH", "TENANT_ACCESS_DENIED", "FORBIDDEN" -> HttpServletResponse.SC_FORBIDDEN;
            default -> 422;
        };
    }
}
