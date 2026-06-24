package com.resos.modules.employee.repository;

import com.resos.modules.employee.domain.Employee;
import com.resos.modules.employee.domain.EmployeeStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class EmployeeSpecifications {

    private EmployeeSpecifications() {}

    public static Specification<Employee> forTenant(UUID tenantId) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("tenantId"), tenantId),
                cb.isNull(root.get("deletedAt")));
    }

    public static Specification<Employee> forRestaurant(UUID restaurantId) {
        return (root, query, cb) -> cb.equal(root.get("restaurantId"), restaurantId);
    }

    public static Specification<Employee> forStatus(EmployeeStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Employee> forPosition(String position) {
        return (root, query, cb) -> cb.equal(root.get("position"), position);
    }

    public static Specification<Employee> search(String term) {
        String pattern = "%" + term.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("firstName")), pattern),
                cb.like(cb.lower(root.get("lastName")), pattern),
                cb.like(cb.lower(cb.coalesce(root.get("email"), "")), pattern));
    }
}
