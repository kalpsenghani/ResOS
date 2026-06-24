package com.resos.modules.menu.repository;

import com.resos.modules.menu.domain.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MenuCategoryRepository extends JpaRepository<MenuCategory, UUID> {

    List<MenuCategory> findByTenantIdAndRestaurantIdAndDeletedAtIsNullOrderBySortOrderAscNameAsc(
            UUID tenantId, UUID restaurantId);

    Optional<MenuCategory> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, UUID tenantId);
}
