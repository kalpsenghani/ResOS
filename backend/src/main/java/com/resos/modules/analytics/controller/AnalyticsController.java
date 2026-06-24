package com.resos.modules.analytics.controller;

import com.resos.modules.analytics.dto.*;
import com.resos.modules.analytics.service.AnalyticsService;
import com.resos.shared.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/revenue")
    @PreAuthorize("hasAuthority('analytics:read')")
    public ResponseEntity<ApiResponse<RevenueAnalyticsResponse>> revenue(
            @RequestParam UUID restaurantId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "DAY") String groupBy) {
        return ResponseEntity.ok(
                ApiResponse.of(analyticsService.getRevenueAnalytics(restaurantId, startDate, endDate, groupBy)));
    }

    @GetMapping("/inventory")
    @PreAuthorize("hasAuthority('analytics:read')")
    public ResponseEntity<ApiResponse<InventoryAnalyticsResponse>> inventory(
            @RequestParam UUID restaurantId, @RequestParam(defaultValue = "MONTH") String period) {
        return ResponseEntity.ok(ApiResponse.of(analyticsService.getInventoryAnalytics(restaurantId, period)));
    }

    @GetMapping("/employees")
    @PreAuthorize("hasAuthority('analytics:read')")
    public ResponseEntity<ApiResponse<EmployeeAnalyticsResponse>> employees(
            @RequestParam UUID restaurantId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(
                ApiResponse.of(analyticsService.getEmployeeAnalytics(restaurantId, startDate, endDate)));
    }

    @GetMapping("/orders")
    @PreAuthorize("hasAuthority('analytics:read')")
    public ResponseEntity<ApiResponse<OrderAnalyticsResponse>> orders(
            @RequestParam UUID restaurantId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(
                ApiResponse.of(analyticsService.getOrderAnalytics(restaurantId, startDate, endDate)));
    }
}
