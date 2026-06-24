package com.resos.modules.dashboard.service;

import com.resos.modules.dashboard.dto.DashboardKpiResponse;
import com.resos.modules.dashboard.dto.KpiMetric;
import com.resos.modules.employee.repository.EmployeeRepository;
import com.resos.modules.inventory.repository.InventoryItemRepository;
import com.resos.modules.reservation.service.ReservationService;
import com.resos.modules.restaurant.domain.Restaurant;
import com.resos.modules.restaurant.repository.RestaurantRepository;
import com.resos.shared.exception.BusinessException;
import com.resos.shared.tenant.TenantContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private DashboardService dashboardService;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID restaurantId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TenantContextHolder.set(new com.resos.shared.tenant.TenantContext(tenantId, false));
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void getKpisReturnsZeroMetricsWhenOperationalDataUnavailable() {
        when(restaurantRepository.findByIdAndTenantIdAndDeletedAtIsNull(restaurantId, tenantId))
                .thenReturn(Optional.of(Restaurant.builder().id(restaurantId).tenantId(tenantId).name("Test").address("A").build()));
        when(inventoryItemRepository.countLowStockByRestaurant(tenantId, restaurantId)).thenReturn(3L);
        when(employeeRepository.countByTenantIdAndRestaurantIdAndStatusAndDeletedAtIsNull(
                tenantId, restaurantId, com.resos.modules.employee.domain.EmployeeStatus.ACTIVE)).thenReturn(5L);
        when(reservationService.countTodayReservations(restaurantId)).thenReturn(8L);

        DashboardKpiResponse response = dashboardService.getKpis(restaurantId, "WEEK");

        assertThat(response.revenue().value()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.revenue().trend()).isEqualTo(KpiMetric.Trend.FLAT);
        assertThat(response.reservations().value()).isEqualByComparingTo(BigDecimal.valueOf(8));
        assertThat(response.lowStockItems().value()).isEqualByComparingTo(BigDecimal.valueOf(3));
        assertThat(response.activeEmployees().value()).isEqualByComparingTo(BigDecimal.valueOf(5));
    }

    @Test
    void getKpisThrowsWhenRestaurantNotInTenant() {
        when(restaurantRepository.findByIdAndTenantIdAndDeletedAtIsNull(restaurantId, tenantId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> dashboardService.getKpis(restaurantId, "WEEK"))
                .isInstanceOf(BusinessException.class);
    }
}
