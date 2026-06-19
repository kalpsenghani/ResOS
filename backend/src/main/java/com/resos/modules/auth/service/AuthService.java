package com.resos.modules.auth.service;

import com.resos.modules.auth.dto.AuthResponse;
import com.resos.modules.auth.dto.LoginRequest;
import com.resos.modules.auth.dto.RegisterRequest;
import com.resos.modules.auth.domain.RefreshToken;
import com.resos.modules.restaurant.service.RestaurantService;
import com.resos.modules.subscription.domain.Subscription;
import com.resos.modules.subscription.domain.SubscriptionPlan;
import com.resos.modules.subscription.domain.SubscriptionStatus;
import com.resos.modules.subscription.repository.SubscriptionPlanRepository;
import com.resos.modules.subscription.repository.SubscriptionRepository;
import com.resos.modules.tenant.domain.Tenant;
import com.resos.modules.tenant.domain.TenantStatus;
import com.resos.modules.tenant.repository.TenantRepository;
import com.resos.modules.user.domain.Role;
import com.resos.modules.user.domain.User;
import com.resos.modules.user.domain.UserStatus;
import com.resos.modules.user.repository.UserRepository;
import com.resos.modules.user.service.TenantRoleProvisioner;
import com.resos.modules.user.service.UserMapper;
import com.resos.shared.exception.BusinessException;
import com.resos.shared.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 15;

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final TenantRoleProvisioner tenantRoleProvisioner;
    private final RestaurantService restaurantService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper;

    @Transactional
    public AuthResult register(RegisterRequest request, String userAgent, String ipAddress) {
        if (tenantRepository.existsBySlugAndDeletedAtIsNull(request.tenantSlug())) {
            throw new BusinessException("DUPLICATE_RESOURCE", "Tenant slug is already taken");
        }

        Tenant tenant = tenantRepository.save(Tenant.builder()
                .name(request.tenantName())
                .slug(request.tenantSlug())
                .email(request.email())
                .phone(request.phone())
                .status(TenantStatus.TRIAL)
                .build());

        Role ownerRole = tenantRoleProvisioner.provisionTenantRoles(tenant);

        SubscriptionPlan starterPlan = subscriptionPlanRepository.findBySlugAndActiveTrue("starter")
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "Starter plan not configured"));

        subscriptionRepository.save(Subscription.builder()
                .tenant(tenant)
                .plan(starterPlan)
                .status(SubscriptionStatus.TRIAL)
                .trialEndsAt(Instant.now().plus(14, ChronoUnit.DAYS))
                .build());

        restaurantService.createDefaultRestaurant(tenant.getId(), tenant.getName());

        User user = userRepository.save(User.builder()
                .tenant(tenant)
                .email(request.email().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phone(request.phone())
                .status(UserStatus.ACTIVE)
                .roles(Set.of(ownerRole))
                .build());

        user = userRepository.findByIdWithRoles(user.getId()).orElseThrow();
        return buildAuthResult(user, userAgent, ipAddress);
    }

    @Transactional
    public AuthResult login(LoginRequest request, String userAgent, String ipAddress) {
        Tenant tenant = tenantRepository.findBySlugAndDeletedAtIsNull(request.tenantSlug())
                .orElseThrow(() -> new BusinessException("UNAUTHENTICATED", "Invalid email or password"));

        User user = userRepository.findByEmailAndTenantIdWithRoles(
                        request.email().toLowerCase(), tenant.getId())
                .orElseThrow(() -> new BusinessException("UNAUTHENTICATED", "Invalid email or password"));

        if (user.isLocked()) {
            throw new BusinessException("UNAUTHENTICATED", "Account is temporarily locked. Try again later.");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            recordFailedLogin(user.getId());
            throw new BusinessException("UNAUTHENTICATED", "Invalid email or password");
        }

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        return buildAuthResult(user, userAgent, ipAddress);
    }

    @Transactional
    public AuthResult refresh(String rawRefreshToken, String userAgent, String ipAddress) {
        RefreshToken existing = refreshTokenService.validateAndGet(rawRefreshToken);
        String newRefreshToken = refreshTokenService.rotateRefreshToken(existing, userAgent, ipAddress);
        User user = userRepository.findByIdWithRoles(existing.getUser().getId()).orElseThrow();
        return buildAuthResult(user, newRefreshToken);
    }

    @Transactional
    public void logout(String rawRefreshToken, UserPrincipalHolder principal) {
        if (rawRefreshToken != null && !rawRefreshToken.isBlank()) {
            try {
                RefreshToken token = refreshTokenService.validateAndGet(rawRefreshToken);
                token.revoke();
            } catch (BusinessException ignored) {
                // Token already invalid — logout is idempotent
            }
        }
        if (principal != null && principal.userId() != null) {
            refreshTokenService.revokeAllForUser(principal.userId());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailedLogin(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow();
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setLockedUntil(Instant.now().plus(LOCK_DURATION_MINUTES, ChronoUnit.MINUTES));
            user.setStatus(UserStatus.LOCKED);
        }
        userRepository.save(user);
    }

    private AuthResult buildAuthResult(User user, String userAgent, String ipAddress) {
        String refreshToken = refreshTokenService.createRefreshToken(user, userAgent, ipAddress);
        return buildAuthResult(user, refreshToken);
    }

    private AuthResult buildAuthResult(User user, String refreshToken) {
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        AuthResponse response = new AuthResponse(
                accessToken,
                jwtTokenProvider.getAccessTokenExpirationSeconds(),
                "Bearer",
                new AuthResponse.UserSummary(
                        user.getId(),
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getRoles().stream().map(Role::getName).sorted().toList(),
                        userMapper.extractPermissions(user)
                ),
                user.getTenant() != null
                        ? new AuthResponse.TenantSummary(
                                user.getTenant().getId(),
                                user.getTenant().getName(),
                                user.getTenant().getSlug())
                        : null
        );
        return new AuthResult(response, refreshToken);
    }

    public record AuthResult(AuthResponse response, String refreshToken) {}

    public record UserPrincipalHolder(java.util.UUID userId) {}
}
