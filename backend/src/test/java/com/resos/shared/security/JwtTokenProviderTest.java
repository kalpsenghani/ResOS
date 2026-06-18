package com.resos.shared.security;

import com.resos.config.JwtKeyProvider;
import com.resos.config.JwtProperties;
import com.resos.modules.tenant.domain.Tenant;
import com.resos.modules.user.domain.Role;
import com.resos.modules.user.domain.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() throws Exception {
        JwtProperties properties = new JwtProperties();
        properties.setIssuer("resos-test");
        properties.setGenerateKeys(true);
        properties.setAccessTokenExpiration(java.time.Duration.ofMinutes(15));

        JwtKeyProvider keyProvider = new JwtKeyProvider(properties);
        keyProvider.init();

        jwtTokenProvider = new JwtTokenProvider(keyProvider, properties);
    }

    @Test
    void generatesAndParsesAccessTokenWithTenantClaims() {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Tenant tenant = Tenant.builder().id(tenantId).name("Test").slug("test").email("a@b.com").build();
        Role role = Role.builder().name("TENANT_OWNER").permissions(Set.of()).build();
        User user = User.builder()
                .id(userId)
                .email("owner@test.com")
                .tenant(tenant)
                .roles(Set.of(role))
                .build();

        String token = jwtTokenProvider.generateAccessToken(user);
        Claims claims = jwtTokenProvider.parseClaims(token);

        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        assertThat(claims.get("tenant_id", String.class)).isEqualTo(tenantId.toString());
        assertThat(claims.get("email", String.class)).isEqualTo("owner@test.com");
    }
}
