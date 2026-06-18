package com.resos.modules.user.repository;

import com.resos.modules.user.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByNameAndTenantIsNull(String name);

    Optional<Role> findByNameAndTenantId(String name, UUID tenantId);

    @Query("""
            SELECT r FROM Role r
            LEFT JOIN FETCH r.permissions
            WHERE r.name = :name AND r.tenant.id = :tenantId
            """)
    Optional<Role> findByNameAndTenantIdWithPermissions(@Param("name") String name, @Param("tenantId") UUID tenantId);

    List<Role> findByTenantId(UUID tenantId);
}
