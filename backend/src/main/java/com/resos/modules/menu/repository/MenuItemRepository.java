package com.resos.modules.menu.repository;

import com.resos.modules.menu.domain.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MenuItemRepository extends JpaRepository<MenuItem, UUID>, JpaSpecificationExecutor<MenuItem> {

    Optional<MenuItem> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, UUID tenantId);

    List<MenuItem> findByTenantIdAndCategoryIdAndDeletedAtIsNullOrderBySortOrderAscNameAsc(
            UUID tenantId, UUID categoryId);
}
