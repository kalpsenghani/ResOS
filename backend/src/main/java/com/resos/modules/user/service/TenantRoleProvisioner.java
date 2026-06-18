package com.resos.modules.user.service;

import com.resos.modules.tenant.domain.Tenant;
import com.resos.modules.user.domain.Permission;
import com.resos.modules.user.domain.Role;
import com.resos.modules.user.repository.PermissionRepository;
import com.resos.modules.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TenantRoleProvisioner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    private static final Set<String> OWNER_PERMISSIONS = Set.of(
            "inventory:read", "inventory:write", "inventory:delete",
            "orders:read", "orders:write", "orders:delete",
            "employees:read", "employees:write",
            "reservations:read", "reservations:write",
            "menu:read", "menu:write",
            "analytics:read",
            "settings:read", "settings:write",
            "users:read", "users:write"
    );

    private static final Set<String> MANAGER_PERMISSIONS = Set.of(
            "inventory:read", "inventory:write",
            "orders:read", "orders:write",
            "employees:read", "employees:write",
            "reservations:read", "reservations:write",
            "menu:read", "menu:write",
            "analytics:read",
            "settings:read",
            "users:read"
    );

    private static final Set<String> STAFF_PERMISSIONS = Set.of(
            "orders:read", "orders:write",
            "reservations:read",
            "menu:read"
    );

    @Transactional
    public Role provisionTenantRoles(Tenant tenant) {
        createRole(tenant, "TENANT_OWNER", "Restaurant owner with full tenant access", OWNER_PERMISSIONS, false);
        createRole(tenant, "MANAGER", "Restaurant manager", MANAGER_PERMISSIONS, false);
        createRole(tenant, "STAFF", "Restaurant staff member", STAFF_PERMISSIONS, false);
        return roleRepository.findByNameAndTenantIdWithPermissions("TENANT_OWNER", tenant.getId())
                .orElseThrow();
    }

    private void createRole(Tenant tenant, String name, String description, Set<String> permissionNames, boolean system) {
        Set<Permission> permissions = new HashSet<>(permissionRepository.findAll().stream()
                .filter(p -> permissionNames.contains(p.getName()))
                .toList());

        Role role = Role.builder()
                .tenant(tenant)
                .name(name)
                .description(description)
                .system(system)
                .permissions(permissions)
                .build();
        roleRepository.save(role);
    }

    public List<Role> getTenantRoles(Tenant tenant) {
        return roleRepository.findByTenantId(tenant.getId());
    }
}
