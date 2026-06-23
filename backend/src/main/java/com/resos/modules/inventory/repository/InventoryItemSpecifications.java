package com.resos.modules.inventory.repository;

import com.resos.modules.inventory.domain.InventoryItem;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class InventoryItemSpecifications {

    private InventoryItemSpecifications() {}

    public static Specification<InventoryItem> forTenant(UUID tenantId) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("tenantId"), tenantId),
                cb.isNull(root.get("deletedAt")));
    }

    public static Specification<InventoryItem> forRestaurant(UUID restaurantId) {
        return (root, query, cb) -> cb.equal(root.get("restaurantId"), restaurantId);
    }

    public static Specification<InventoryItem> forCategory(String category) {
        return (root, query, cb) -> cb.equal(root.get("category"), category);
    }

    public static Specification<InventoryItem> lowStockOnly() {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("currentStock"), root.get("minimumStock"));
    }

    public static Specification<InventoryItem> search(String term) {
        String pattern = "%" + term.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), pattern),
                cb.like(cb.lower(cb.coalesce(root.get("sku"), "")), pattern));
    }
}
