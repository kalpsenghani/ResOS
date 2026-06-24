package com.resos.modules.menu.repository;

import com.resos.modules.menu.domain.MenuItem;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.UUID;

public final class MenuItemSpecifications {

    private MenuItemSpecifications() {}

    public static Specification<MenuItem> forTenant(UUID tenantId) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("tenantId"), tenantId),
                cb.isNull(root.get("deletedAt")));
    }

    public static Specification<MenuItem> forCategory(UUID categoryId) {
        return (root, query, cb) -> cb.equal(root.get("categoryId"), categoryId);
    }

    public static Specification<MenuItem> forCategories(Collection<UUID> categoryIds) {
        return (root, query, cb) -> root.get("categoryId").in(categoryIds);
    }

    public static Specification<MenuItem> available(Boolean available) {
        return (root, query, cb) -> cb.equal(root.get("available"), available);
    }

    public static Specification<MenuItem> search(String term) {
        String pattern = "%" + term.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), pattern),
                cb.like(cb.lower(cb.coalesce(root.get("description"), "")), pattern));
    }
}
