package com.resos.modules.employee.repository;

import com.resos.modules.employee.domain.EmployeeSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeScheduleRepository extends JpaRepository<EmployeeSchedule, UUID> {

    Optional<EmployeeSchedule> findByIdAndTenantId(UUID id, UUID tenantId);

    List<EmployeeSchedule> findByEmployeeIdAndTenantIdAndShiftDateBetweenOrderByShiftDateAscStartTimeAsc(
            UUID employeeId, UUID tenantId, LocalDate startDate, LocalDate endDate);

    List<EmployeeSchedule> findByTenantIdAndRestaurantIdAndShiftDateBetween(
            UUID tenantId, UUID restaurantId, LocalDate startDate, LocalDate endDate);
}
