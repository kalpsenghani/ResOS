package com.resos.modules.dashboard.service;

import com.resos.modules.dashboard.dto.DashboardKpiResponse;
import com.resos.modules.dashboard.dto.KpiMetric;
import com.resos.modules.dashboard.dto.RecentOrderResponse;
import com.resos.modules.dashboard.dto.RevenueChartResponse;
import com.resos.modules.employee.domain.EmployeeStatus;
import com.resos.modules.employee.repository.EmployeeRepository;
import com.resos.modules.inventory.repository.InventoryItemRepository;
import com.resos.modules.order.service.OrderService;
import com.resos.modules.reservation.service.ReservationService;
import com.resos.modules.restaurant.repository.RestaurantRepository;
import com.resos.shared.exception.BusinessException;
import com.resos.shared.tenant.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final RestaurantRepository restaurantRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final EmployeeRepository employeeRepository;
    private final ReservationService reservationService;
    private final OrderService orderService;

    @Transactional(readOnly = true)
    public DashboardKpiResponse getKpis(UUID restaurantId, String period) {
        validateRestaurantAccess(restaurantId);
        long lowStockCount = inventoryItemRepository.countLowStockByRestaurant(
                TenantContextHolder.requireTenantId(), restaurantId);
        long activeEmployees = employeeRepository.countByTenantIdAndRestaurantIdAndStatusAndDeletedAtIsNull(
                TenantContextHolder.requireTenantId(), restaurantId, EmployeeStatus.ACTIVE);
        long todayReservations = reservationService.countTodayReservations(restaurantId);
        long todayOrders = orderService.countTodayOrders(restaurantId);
        BigDecimal todayRevenue = orderService.sumTodayRevenue(restaurantId);
        BigDecimal avgOrderValue = orderService.averageOrderValue(restaurantId);

        return new DashboardKpiResponse(
                metric(todayRevenue),
                metric(BigDecimal.valueOf(todayOrders)),
                metric(BigDecimal.valueOf(todayReservations)),
                metric(BigDecimal.valueOf(lowStockCount)),
                metric(BigDecimal.valueOf(activeEmployees)),
                metric(avgOrderValue)
        );
    }

    @Transactional(readOnly = true)
    public List<RecentOrderResponse> getRecentOrders(UUID restaurantId, int limit) {
        validateRestaurantAccess(restaurantId);
        return orderService.getRecentOrders(restaurantId, limit);
    }

    @Transactional(readOnly = true)
    public RevenueChartResponse getRevenueChart(UUID restaurantId, String period, String groupBy) {
        validateRestaurantAccess(restaurantId);
        return new RevenueChartResponse(
                orderService.getDailyRevenueLabels(7),
                orderService.getDailyRevenue(restaurantId, 7));
    }

    private void validateRestaurantAccess(UUID restaurantId) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        restaurantRepository.findByIdAndTenantIdAndDeletedAtIsNull(restaurantId, tenantId)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "Restaurant not found"));
    }

    private KpiMetric metric(BigDecimal value) {
        return new KpiMetric(value, 0.0, KpiMetric.Trend.FLAT);
    }
}
