package com.resos.modules.user.repository;

import com.resos.modules.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("""
            SELECT u FROM User u
            LEFT JOIN FETCH u.roles r
            LEFT JOIN FETCH r.permissions
            WHERE u.email = :email AND u.tenant.id = :tenantId AND u.deletedAt IS NULL
            """)
    Optional<User> findByEmailAndTenantIdWithRoles(@Param("email") String email, @Param("tenantId") UUID tenantId);

    @Query("""
            SELECT u FROM User u
            LEFT JOIN FETCH u.roles r
            LEFT JOIN FETCH r.permissions
            WHERE u.id = :id AND u.deletedAt IS NULL
            """)
    Optional<User> findByIdWithRoles(@Param("id") UUID id);

    @Query("""
            SELECT u FROM User u
            LEFT JOIN FETCH u.roles r
            LEFT JOIN FETCH r.permissions
            WHERE u.email = :email AND u.tenant IS NULL AND u.deletedAt IS NULL
            """)
    Optional<User> findPlatformUserByEmail(@Param("email") String email);

    Page<User> findByTenantIdAndDeletedAtIsNull(UUID tenantId, Pageable pageable);

    boolean existsByEmailAndTenantIdAndDeletedAtIsNull(String email, UUID tenantId);
}
