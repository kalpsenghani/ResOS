package com.resos.modules.employee.service;

import com.resos.modules.audit.domain.AuditAction;
import com.resos.modules.audit.service.AuditLogService;
import com.resos.modules.employee.domain.Employee;
import com.resos.modules.employee.domain.EmployeeSchedule;
import com.resos.modules.employee.domain.EmployeeStatus;
import com.resos.modules.employee.domain.ScheduleStatus;
import com.resos.modules.employee.dto.*;
import com.resos.modules.employee.repository.EmployeeRepository;
import com.resos.modules.employee.repository.EmployeeScheduleRepository;
import com.resos.modules.employee.repository.EmployeeSpecifications;
import com.resos.modules.restaurant.repository.RestaurantRepository;
import com.resos.shared.api.ApiResponse;
import com.resos.shared.exception.BusinessException;
import com.resos.shared.security.UserPrincipal;
import com.resos.shared.tenant.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private static final String ENTITY_TYPE = "Employee";
    private static final String SCHEDULE_ENTITY_TYPE = "EmployeeSchedule";

    private final EmployeeRepository employeeRepository;
    private final EmployeeScheduleRepository scheduleRepository;
    private final RestaurantRepository restaurantRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public ApiResponse<List<EmployeeResponse>> listEmployees(
            UUID restaurantId,
            EmployeeStatus status,
            String position,
            String search,
            Pageable pageable) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        Specification<Employee> spec = EmployeeSpecifications.forTenant(tenantId);
        if (restaurantId != null) {
            spec = spec.and(EmployeeSpecifications.forRestaurant(restaurantId));
        }
        if (status != null) {
            spec = spec.and(EmployeeSpecifications.forStatus(status));
        }
        if (position != null) {
            spec = spec.and(EmployeeSpecifications.forPosition(position));
        }
        if (search != null && !search.isBlank()) {
            spec = spec.and(EmployeeSpecifications.search(search.trim()));
        }

        Page<Employee> page = employeeRepository.findAll(spec, pageable);
        List<EmployeeResponse> data = page.getContent().stream().map(this::toResponse).toList();
        return ApiResponse.page(data, new ApiResponse.PageMeta(
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages()));
    }

    @Transactional(readOnly = true)
    public EmployeeResponse getEmployee(UUID id) {
        return toResponse(findEmployeeOrThrow(id));
    }

    @Transactional
    public EmployeeResponse createEmployee(CreateEmployeeRequest request, UserPrincipal principal) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        validateRestaurant(tenantId, request.restaurantId());

        Employee employee = Employee.builder()
                .tenantId(tenantId)
                .restaurantId(request.restaurantId())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .phone(request.phone())
                .position(request.position())
                .hourlyRate(request.hourlyRate())
                .hireDate(request.hireDate())
                .status(EmployeeStatus.ACTIVE)
                .build();

        employee = employeeRepository.save(employee);
        auditLogService.log(AuditAction.CREATE, ENTITY_TYPE, employee.getId(), null, snapshot(employee), principal.getId());
        return toResponse(employee);
    }

    @Transactional
    public EmployeeResponse updateEmployee(
            UUID id,
            UpdateEmployeeRequest request,
            Integer expectedVersion,
            UserPrincipal principal) {
        Employee employee = findEmployeeOrThrow(id);
        checkVersion(employee, expectedVersion);
        Map<String, Object> before = snapshot(employee);

        if (request.firstName() != null) employee.setFirstName(request.firstName());
        if (request.lastName() != null) employee.setLastName(request.lastName());
        if (request.email() != null) employee.setEmail(request.email());
        if (request.phone() != null) employee.setPhone(request.phone());
        if (request.position() != null) employee.setPosition(request.position());
        if (request.hourlyRate() != null) employee.setHourlyRate(request.hourlyRate());
        if (request.hireDate() != null) employee.setHireDate(request.hireDate());
        if (request.status() != null) employee.setStatus(request.status());

        employee = employeeRepository.save(employee);
        auditLogService.log(AuditAction.UPDATE, ENTITY_TYPE, employee.getId(), before, snapshot(employee), principal.getId());
        return toResponse(employee);
    }

    @Transactional
    public void deleteEmployee(UUID id, UserPrincipal principal) {
        Employee employee = findEmployeeOrThrow(id);
        Map<String, Object> before = snapshot(employee);
        employee.setDeletedAt(Instant.now());
        employee.setStatus(EmployeeStatus.TERMINATED);
        employeeRepository.save(employee);
        auditLogService.log(AuditAction.DELETE, ENTITY_TYPE, employee.getId(), before, null, principal.getId());
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponse> listSchedules(UUID employeeId, LocalDate startDate, LocalDate endDate) {
        Employee employee = findEmployeeOrThrow(employeeId);
        LocalDate start = startDate != null ? startDate : LocalDate.now().minusDays(7);
        LocalDate end = endDate != null ? endDate : LocalDate.now().plusDays(30);

        return scheduleRepository
                .findByEmployeeIdAndTenantIdAndShiftDateBetweenOrderByShiftDateAscStartTimeAsc(
                        employee.getId(), employee.getTenantId(), start, end)
                .stream()
                .map(this::toScheduleResponse)
                .toList();
    }

    @Transactional
    public ScheduleResponse createSchedule(
            UUID employeeId,
            CreateScheduleRequest request,
            UserPrincipal principal) {
        Employee employee = findEmployeeOrThrow(employeeId);
        validateRestaurant(employee.getTenantId(), request.restaurantId());
        validateShiftTimes(request.startTime(), request.endTime());

        EmployeeSchedule schedule = EmployeeSchedule.builder()
                .tenantId(employee.getTenantId())
                .employeeId(employee.getId())
                .restaurantId(request.restaurantId())
                .shiftDate(request.shiftDate())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .notes(request.notes())
                .status(ScheduleStatus.SCHEDULED)
                .build();

        schedule = scheduleRepository.save(schedule);
        auditLogService.log(
                AuditAction.CREATE,
                SCHEDULE_ENTITY_TYPE,
                schedule.getId(),
                null,
                scheduleSnapshot(schedule),
                principal.getId());
        return toScheduleResponse(schedule);
    }

    @Transactional
    public ScheduleResponse updateSchedule(UUID scheduleId, UpdateScheduleRequest request, UserPrincipal principal) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        EmployeeSchedule schedule = scheduleRepository.findByIdAndTenantId(scheduleId, tenantId)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "Schedule not found"));

        Map<String, Object> before = scheduleSnapshot(schedule);

        if (request.shiftDate() != null) schedule.setShiftDate(request.shiftDate());
        if (request.startTime() != null) schedule.setStartTime(request.startTime());
        if (request.endTime() != null) schedule.setEndTime(request.endTime());
        if (request.status() != null) schedule.setStatus(request.status());
        if (request.notes() != null) schedule.setNotes(request.notes());

        validateShiftTimes(schedule.getStartTime(), schedule.getEndTime());
        schedule = scheduleRepository.save(schedule);
        auditLogService.log(
                AuditAction.UPDATE,
                SCHEDULE_ENTITY_TYPE,
                schedule.getId(),
                before,
                scheduleSnapshot(schedule),
                principal.getId());
        return toScheduleResponse(schedule);
    }

    @Transactional
    public void deleteSchedule(UUID scheduleId, UserPrincipal principal) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        EmployeeSchedule schedule = scheduleRepository.findByIdAndTenantId(scheduleId, tenantId)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "Schedule not found"));

        auditLogService.log(
                AuditAction.DELETE,
                SCHEDULE_ENTITY_TYPE,
                schedule.getId(),
                scheduleSnapshot(schedule),
                null,
                principal.getId());
        scheduleRepository.delete(schedule);
    }

    @Transactional(readOnly = true)
    public long countActiveEmployees(UUID restaurantId) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        validateRestaurant(tenantId, restaurantId);
        return employeeRepository.countByTenantIdAndRestaurantIdAndStatusAndDeletedAtIsNull(
                tenantId, restaurantId, EmployeeStatus.ACTIVE);
    }

    private Employee findEmployeeOrThrow(UUID id) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        return employeeRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "Employee not found"));
    }

    private void validateRestaurant(UUID tenantId, UUID restaurantId) {
        restaurantRepository.findByIdAndTenantIdAndDeletedAtIsNull(restaurantId, tenantId)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "Restaurant not found"));
    }

    private void validateShiftTimes(java.time.LocalTime start, java.time.LocalTime end) {
        if (start != null && end != null && !end.isAfter(start)) {
            throw new BusinessException("VALIDATION_ERROR", "End time must be after start time");
        }
    }

    private void checkVersion(Employee employee, Integer expectedVersion) {
        if (expectedVersion != null && employee.getVersion() != expectedVersion) {
            throw new BusinessException("OPTIMISTIC_LOCK", "Employee was modified by another user");
        }
    }

    private Map<String, Object> snapshot(Employee employee) {
        Map<String, Object> map = new HashMap<>();
        map.put("firstName", employee.getFirstName());
        map.put("lastName", employee.getLastName());
        map.put("position", employee.getPosition());
        map.put("status", employee.getStatus().name());
        map.put("hourlyRate", employee.getHourlyRate());
        return map;
    }

    private Map<String, Object> scheduleSnapshot(EmployeeSchedule schedule) {
        Map<String, Object> map = new HashMap<>();
        map.put("employeeId", schedule.getEmployeeId());
        map.put("shiftDate", schedule.getShiftDate().toString());
        map.put("startTime", schedule.getStartTime().toString());
        map.put("endTime", schedule.getEndTime().toString());
        map.put("status", schedule.getStatus().name());
        return map;
    }

    private EmployeeResponse toResponse(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getRestaurantId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getPhone(),
                employee.getPosition(),
                employee.getHourlyRate(),
                employee.getHireDate(),
                employee.getStatus(),
                employee.getCreatedAt(),
                employee.getUpdatedAt(),
                employee.getVersion());
    }

    private ScheduleResponse toScheduleResponse(EmployeeSchedule schedule) {
        return new ScheduleResponse(
                schedule.getId(),
                schedule.getEmployeeId(),
                schedule.getRestaurantId(),
                schedule.getShiftDate(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getStatus(),
                schedule.getNotes(),
                schedule.getCreatedAt());
    }
}
