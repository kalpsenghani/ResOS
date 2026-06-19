package com.resos.modules.dashboard.controller;

import com.resos.modules.dashboard.dto.DashboardKpiResponse;
import com.resos.modules.dashboard.dto.RecentOrderResponse;
import com.resos.modules.dashboard.dto.RevenueChartResponse;
import com.resos.modules.dashboard.service.DashboardService;
import com.resos.shared.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/kpis")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DashboardKpiResponse>> getKpis(
            @RequestParam UUID restaurantId,
            @RequestParam(defaultValue = "WEEK") String period) {
        return ResponseEntity.ok(ApiResponse.of(dashboardService.getKpis(restaurantId, period)));
    }

    @GetMapping("/recent-orders")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<RecentOrderResponse>>> getRecentOrders(
            @RequestParam UUID restaurantId,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.of(dashboardService.getRecentOrders(restaurantId, limit)));
    }

    @GetMapping("/revenue-chart")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<RevenueChartResponse>> getRevenueChart(
            @RequestParam UUID restaurantId,
            @RequestParam(defaultValue = "WEEK") String period,
            @RequestParam(defaultValue = "DAY") String groupBy) {
        return ResponseEntity.ok(ApiResponse.of(dashboardService.getRevenueChart(restaurantId, period, groupBy)));
    }
}
