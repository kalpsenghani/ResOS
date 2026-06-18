package com.resos.modules.auth.service;

import com.resos.config.JwtProperties;
import com.resos.modules.auth.domain.RefreshToken;
import com.resos.modules.auth.repository.RefreshTokenRepository;
import com.resos.modules.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    @Transactional
    public String createRefreshToken(User user, String userAgent, String ipAddress) {
        String rawToken = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(hashToken(rawToken))
                .expiresAt(Instant.now().plus(jwtProperties.getRefreshTokenExpiration()))
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .build();
        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    @Transactional
    public RefreshToken validateAndGet(String rawToken) {
        return refreshTokenRepository.findValidByTokenHash(hashToken(rawToken))
                .filter(RefreshToken::isValid)
                .orElseThrow(() -> new com.resos.shared.exception.BusinessException(
                        "UNAUTHENTICATED", "Invalid or expired refresh token"));
    }

    @Transactional
    public String rotateRefreshToken(RefreshToken existing, String userAgent, String ipAddress) {
        existing.revoke();
        refreshTokenRepository.save(existing);
        return createRefreshToken(existing.getUser(), userAgent, ipAddress);
    }

    @Transactional
    public void revokeAllForUser(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    public String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
