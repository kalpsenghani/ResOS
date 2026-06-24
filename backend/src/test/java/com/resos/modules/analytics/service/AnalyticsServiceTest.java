package com.resos.modules.analytics.service;

import com.resos.modules.analytics.dto.EmployeeAnalyticsResponse;
import com.resos.modules.analytics.dto.InventoryAnalyticsResponse;
import com.resos.modules.analytics.dto.RevenueAnalyticsResponse;
import com.resos.modules.employee.domain.EmployeeStatus;
import com.resos.modules.employee.repository.EmployeeRepository;
import com.resos.modules.employee.repository.EmployeeScheduleRepository;
import com.resos.modules.inventory.domain.TransactionType;
import com.resos.modules.inventory.repository.InventoryItemRepository;
import com.resos.modules.inventory.repository.InventoryTransactionRepository;
import com.resos.modules.order.domain.OrderStatus;
import com.resos.modules.order.repository.RestaurantOrderRepository;
import com.resos.modules.restaurant.repository.RestaurantRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AnalyticsServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;
    @Mock
    private RestaurantOrderRepository orderRepository;
    @Mock
    private InventoryItemRepository inventoryItemRepository;
    @Mock
    private InventoryTransactionRepository inventoryTransactionRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private EmployeeScheduleRepository scheduleRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID restaurantId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TenantContextHolder.set(new TenantContext(tenantId, false));
        when(restaurantRepository.findByIdAndTenantIdAndDeletedAtIsNull(restaurantId, tenantId))
                .thenReturn(Optional.of(com.resos.modules.restaurant.domain.Restaurant.builder()
                        .id(restaurantId)
                        .tenantId(tenantId)
                        .build()));
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void getRevenueAnalyticsAggregatesDailyTotals() {
        when(orderRepository.sumCompletedRevenue(eq(tenantId), eq(restaurantId), any(), any()))
                .thenReturn(new BigDecimal("100.00"));
        when(orderRepository.countByTenantIdAndRestaurantIdAndCreatedAtBetweenAndStatus(
                        eq(tenantId), eq(restaurantId), any(), any(), eq(OrderStatus.COMPLETED)))
                .thenReturn(2L);

        LocalDate today = LocalDate.now();
        RevenueAnalyticsResponse response =
                analyticsService.getRevenueAnalytics(restaurantId, today.minusDays(2), today, "DAY");

        assertThat(response.labels()).hasSize(3);
        assertThat(response.totalRevenue()).isEqualByComparingTo("300.00");
        assertThat(response.orderCount()).isEqualTo(6);
    }

    @Test
    void getInventoryAnalyticsReturnsStockMetrics() {
        when(inventoryItemRepository.countByTenantIdAndRestaurantIdAndDeletedAtIsNull(tenantId, restaurantId))
                .thenReturn(10L);
        when(inventoryItemRepository.countLowStockByRestaurant(tenantId, restaurantId)).thenReturn(2L);
        when(inventoryItemRepository.sumInventoryValue(tenantId, restaurantId)).thenReturn(new BigDecimal("500.00"));
        when(inventoryItemRepository.findByTenantIdAndRestaurantIdAndDeletedAtIsNull(tenantId, restaurantId))
                .thenReturn(List.of());
        when(inventoryTransactionRepository.countByRestaurantAndTypeBetween(
                        eq(tenantId), eq(restaurantId), eq(TransactionType.WASTE), any(), any()))
                .thenReturn(1L);
        when(inventoryTransactionRepository.sumWasteCostByRestaurantBetween(
                        eq(tenantId), eq(restaurantId), any(), any()))
                .thenReturn(new BigDecimal("25.00"));
        when(inventoryTransactionRepository.countByRestaurantAndTypeBetween(
                        eq(tenantId), eq(restaurantId), eq(TransactionType.USAGE), any(), any()))
                .thenReturn(5L);

        InventoryAnalyticsResponse response = analyticsService.getInventoryAnalytics(restaurantId, "MONTH");

        assertThat(response.totalItems()).isEqualTo(10);
        assertThat(response.lowStockItems()).isEqualTo(2);
        assertThat(response.wasteCost()).isEqualByComparingTo("25.00");
    }

    @Test
    void getEmployeeAnalyticsReturnsLaborSummary() {
        when(employeeRepository.countByTenantIdAndRestaurantIdAndStatusAndDeletedAtIsNull(
                        tenantId, restaurantId, EmployeeStatus.ACTIVE))
                .thenReturn(4L);
        when(scheduleRepository.findByTenantIdAndRestaurantIdAndShiftDateBetween(
                        eq(tenantId), eq(restaurantId), any(), any()))
                .thenReturn(List.of());
        when(employeeRepository.findByTenantIdAndRestaurantIdAndDeletedAtIsNull(tenantId, restaurantId))
                .thenReturn(List.of());

        EmployeeAnalyticsResponse response =
                analyticsService.getEmployeeAnalytics(restaurantId, LocalDate.now().minusDays(7), LocalDate.now());

        assertThat(response.activeEmployees()).isEqualTo(4);
        assertThat(response.scheduledShifts()).isZero();
    }
}
