package com.resos.modules.user.service;

import com.resos.modules.tenant.domain.Tenant;
import com.resos.modules.user.domain.Role;
import com.resos.modules.user.domain.User;
import com.resos.modules.user.domain.UserStatus;
import com.resos.modules.user.dto.CreateUserRequest;
import com.resos.modules.user.dto.UpdateUserRequest;
import com.resos.modules.user.dto.UserResponse;
import com.resos.modules.user.repository.RoleRepository;
import com.resos.modules.user.repository.UserRepository;
import com.resos.shared.tenant.TenantContextHolder;
import com.resos.shared.exception.BusinessException;
import com.resos.shared.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public Page<UserResponse> listUsers(UUID tenantId, Pageable pageable) {
        return userRepository.findByTenant_IdAndDeletedAtIsNull(tenantId, pageable)
                .map(userMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(UUID userId, UserPrincipal principal) {
        User user = findUserOrThrow(userId);
        assertTenantAccess(user, principal);
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request, UserPrincipal principal) {
        UUID tenantId = requireTenantId(principal);
        if (userRepository.existsByEmailAndTenant_IdAndDeletedAtIsNull(request.email().toLowerCase(), tenantId)) {
            throw new BusinessException("DUPLICATE_RESOURCE", "A user with this email already exists");
        }

        Role role = roleRepository.findByNameAndTenant_Id(request.role(), tenantId)
                .orElseThrow(() -> new BusinessException("VALIDATION_ERROR", "Invalid role: " + request.role()));

        User user = User.builder()
                .tenant(Tenant.builder().id(tenantId).build())
                .email(request.email().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phone(request.phone())
                .status(UserStatus.ACTIVE)
                .roles(Set.of(role))
                .build();

        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateUser(UUID userId, UpdateUserRequest request, UserPrincipal principal) {
        User user = findUserOrThrow(userId);
        assertTenantAccess(user, principal);

        if (request.email() != null) {
            user.setEmail(request.email().toLowerCase());
        }
        if (request.firstName() != null) {
            user.setFirstName(request.firstName());
        }
        if (request.lastName() != null) {
            user.setLastName(request.lastName());
        }
        if (request.phone() != null) {
            user.setPhone(request.phone());
        }
        if (request.status() != null) {
            user.setStatus(UserStatus.valueOf(request.status()));
        }
        if (request.role() != null) {
            UUID tenantId = requireTenantId(principal);
            Role role = roleRepository.findByNameAndTenant_Id(request.role(), tenantId)
                    .orElseThrow(() -> new BusinessException("VALIDATION_ERROR", "Invalid role"));
            user.setRoles(Set.of(role));
        }

        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(UUID userId, UserPrincipal principal) {
        User user = findUserOrThrow(userId);
        assertTenantAccess(user, principal);
        if (user.getId().equals(principal.getId())) {
            throw new BusinessException("BUSINESS_RULE_VIOLATION", "You cannot delete your own account");
        }
        user.setDeletedAt(Instant.now());
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UserPrincipal principal) {
        User user = userRepository.findByIdWithRoles(principal.getId())
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "User not found"));
        return userMapper.toResponse(user);
    }

    private User findUserOrThrow(UUID userId) {
        UUID tenantId = TenantContextHolder.getTenantId();
        return userRepository.findByIdWithRoles(userId)
                .filter(u -> u.getDeletedAt() == null)
                .filter(u -> tenantId == null || (u.getTenant() != null && tenantId.equals(u.getTenant().getId())))
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "User not found"));
    }

    private void assertTenantAccess(User user, UserPrincipal principal) {
        if (principal.getRoles().contains("SUPER_ADMIN")) {
            return;
        }
        if (user.getTenant() == null || principal.getTenantId() == null
                || !user.getTenant().getId().equals(principal.getTenantId())) {
            throw new BusinessException("TENANT_ACCESS_DENIED", "You do not have access to this user");
        }
    }

    private UUID requireTenantId(UserPrincipal principal) {
        if (principal.getTenantId() == null) {
            throw new BusinessException("BUSINESS_RULE_VIOLATION", "Tenant context is required");
        }
        return principal.getTenantId();
    }
}
