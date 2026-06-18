package com.resos.shared.security;

import com.resos.config.JwtProperties;
import com.resos.config.JwtKeyProvider;
import com.resos.modules.user.domain.Permission;
import com.resos.modules.user.domain.Role;
import com.resos.modules.user.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtKeyProvider keyProvider;
    private final JwtProperties jwtProperties;

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plus(jwtProperties.getAccessTokenExpiration());

        UUID tenantId = user.getTenant() != null ? user.getTenant().getId() : null;
        Set<String> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        Set<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .collect(Collectors.toSet());

        var builder = Jwts.builder()
                .issuer(jwtProperties.getIssuer())
                .subject(user.getId().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claim("email", user.getEmail())
                .claim("roles", roles)
                .claim("permissions", permissions);

        if (tenantId != null) {
            builder.claim("tenant_id", tenantId.toString());
        }

        return builder.signWith(keyProvider.getPrivateKey(), Jwts.SIG.RS256).compact();
    }

    public Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(keyProvider.getPublicKey())
                    .requireIssuer(jwtProperties.getIssuer())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException ex) {
            throw new com.resos.shared.exception.BusinessException("TOKEN_EXPIRED", "Access token has expired");
        } catch (Exception ex) {
            throw new com.resos.shared.exception.BusinessException("UNAUTHENTICATED", "Invalid access token");
        }
    }

    public long getAccessTokenExpirationSeconds() {
        return jwtProperties.getAccessTokenExpiration().getSeconds();
    }
}
