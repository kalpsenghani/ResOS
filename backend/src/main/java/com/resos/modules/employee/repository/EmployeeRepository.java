package com.resos.modules.employee.repository;

import com.resos.modules.employee.domain.Employee;
import com.resos.modules.employee.domain.EmployeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID>, JpaSpecificationExecutor<Employee> {

    Optional<Employee> findByIdAndTenantIdAndDeletedAtIsNull(UUID id, UUID tenantId);

    long countByTenantIdAndRestaurantIdAndStatusAndDeletedAtIsNull(
            UUID tenantId, UUID restaurantId, EmployeeStatus status);

    List<Employee> findByTenantIdAndRestaurantIdAndDeletedAtIsNull(UUID tenantId, UUID restaurantId);
}
