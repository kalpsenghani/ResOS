package com.resos.shared.tenant;

import com.resos.modules.tenant.domain.Tenant;
import com.resos.modules.user.domain.Role;
import com.resos.modules.user.domain.User;
import com.resos.shared.security.UserPrincipal;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TenantFilterTest {

    @Mock
    private FilterChain filterChain;

    private TenantFilter tenantFilter;

    @BeforeEach
    void setUp() {
        tenantFilter = new TenantFilter();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        TenantContextHolder.clear();
    }

    @Test
    void allowsPublicAuthPathsWithoutTenantHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        tenantFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(TenantContextHolder.getContext()).isEmpty();
    }

    @Test
    void rejectsMissingTenantHeaderForAuthenticatedTenantUser() throws Exception {
        UUID tenantId = UUID.randomUUID();
        setAuthenticatedUser(tenantId, "TENANT_OWNER");

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/auth/me");
        MockHttpServletResponse response = new MockHttpServletResponse();

        tenantFilter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getContentAsString()).contains("VALIDATION_ERROR");
    }

    @Test
    void rejectsMismatchedTenantHeader() throws Exception {
        UUID tenantId = UUID.randomUUID();
        setAuthenticatedUser(tenantId, "TENANT_OWNER");

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/auth/me");
        request.addHeader(TenantConstants.TENANT_HEADER, UUID.randomUUID().toString());
        MockHttpServletResponse response = new MockHttpServletResponse();

        tenantFilter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains("TENANT_MISMATCH");
    }

    @Test
    void proceedsWhenHeaderMatchesJwtTenant() throws Exception {
        UUID tenantId = UUID.randomUUID();
        setAuthenticatedUser(tenantId, "TENANT_OWNER");

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/auth/me");
        request.addHeader(TenantConstants.TENANT_HEADER, tenantId.toString());
        MockHttpServletResponse response = new MockHttpServletResponse();

        tenantFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(TenantContextHolder.getContext()).isEmpty();
    }

    private void setAuthenticatedUser(UUID tenantId, String roleName) {
        Tenant tenant = Tenant.builder().id(tenantId).name("Test").slug("test").email("a@b.com").build();
        Role role = Role.builder().name(roleName).permissions(Set.of()).build();
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("user@test.com")
                .passwordHash("hash")
                .firstName("Test")
                .lastName("User")
                .tenant(tenant)
                .roles(Set.of(role))
                .build();

        UserPrincipal principal = new UserPrincipal(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
}
