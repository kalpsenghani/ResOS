package com.resos.modules.employee.service;

import com.resos.modules.audit.service.AuditLogService;
import com.resos.modules.employee.domain.Employee;
import com.resos.modules.employee.domain.EmployeeStatus;
import com.resos.modules.employee.dto.CreateEmployeeRequest;
import com.resos.modules.employee.dto.CreateScheduleRequest;
import com.resos.modules.employee.repository.EmployeeRepository;
import com.resos.modules.employee.repository.EmployeeScheduleRepository;
import com.resos.modules.restaurant.repository.RestaurantRepository;
import com.resos.shared.security.UserPrincipal;
import com.resos.shared.tenant.TenantContext;
import com.resos.shared.tenant.TenantContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private EmployeeScheduleRepository scheduleRepository;
    @Mock
    private RestaurantRepository restaurantRepository;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private UserPrincipal principal;

    @InjectMocks
    private EmployeeService employeeService;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID restaurantId = UUID.randomUUID();
    private final UUID employeeId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TenantContextHolder.set(new TenantContext(tenantId, false));
        when(principal.getId()).thenReturn(userId);
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void createEmployeeSetsActiveStatus() {
        when(restaurantRepository.findByIdAndTenantIdAndDeletedAtIsNull(restaurantId, tenantId))
                .thenReturn(Optional.of(com.resos.modules.restaurant.domain.Restaurant.builder()
                        .id(restaurantId).tenantId(tenantId).build()));
        when(employeeRepository.save(any())).thenAnswer(invocation -> {
            Employee employee = invocation.getArgument(0);
            employee.setId(employeeId);
            return employee;
        });

        var request = new CreateEmployeeRequest(
                restaurantId, "Mike", "Johnson", "mike@test.com", "+1234567890",
                "Server", new BigDecimal("15.50"), LocalDate.of(2026, 3, 1));

        var response = employeeService.createEmployee(request, principal);

        assertThat(response.status()).isEqualTo(EmployeeStatus.ACTIVE);
        verify(auditLogService).log(any(), any(), any(), any(), any(), eq(userId));
    }

    @Test
    void createSchedulePersistsShift() {
        Employee employee = Employee.builder()
                .id(employeeId)
                .tenantId(tenantId)
                .restaurantId(restaurantId)
                .firstName("Mike")
                .lastName("Johnson")
                .position("Server")
                .hireDate(LocalDate.of(2026, 3, 1))
                .status(EmployeeStatus.ACTIVE)
                .build();

        when(employeeRepository.findByIdAndTenantIdAndDeletedAtIsNull(employeeId, tenantId))
                .thenReturn(Optional.of(employee));
        when(restaurantRepository.findByIdAndTenantIdAndDeletedAtIsNull(restaurantId, tenantId))
                .thenReturn(Optional.of(com.resos.modules.restaurant.domain.Restaurant.builder()
                        .id(restaurantId).tenantId(tenantId).build()));
        when(scheduleRepository.save(any())).thenAnswer(invocation -> {
            var schedule = invocation.getArgument(0, com.resos.modules.employee.domain.EmployeeSchedule.class);
            schedule.setId(UUID.randomUUID());
            return schedule;
        });

        var request = new CreateScheduleRequest(
                restaurantId,
                LocalDate.now().plusDays(1),
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                "Opening shift");

        var response = employeeService.createSchedule(employeeId, request, principal);

        assertThat(response.startTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(response.endTime()).isEqualTo(LocalTime.of(17, 0));
    }
}
