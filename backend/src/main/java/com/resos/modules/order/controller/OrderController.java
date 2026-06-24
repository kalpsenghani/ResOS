package com.resos.modules.order.controller;

import com.resos.modules.order.domain.OrderStatus;
import com.resos.modules.order.dto.*;
import com.resos.modules.order.service.OrderService;
import com.resos.shared.api.ApiResponse;
import com.resos.shared.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    @PreAuthorize("hasAuthority('orders:read')")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> list(
            @RequestParam(required = false) UUID restaurantId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @org.springframework.data.web.PageableDefault(size = 50)
                    org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(orderService.listOrders(restaurantId, status, date, pageable));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('orders:write')")
    public ResponseEntity<ApiResponse<OrderResponse>> create(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(orderService.createOrder(request, principal)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('orders:read')")
    public ResponseEntity<ApiResponse<OrderResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.of(orderService.getOrder(id)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('orders:write')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.of(orderService.updateOrderStatus(id, request, principal)));
    }

    @PatchMapping("/{id}/items/{itemId}/status")
    @PreAuthorize("hasAuthority('orders:write')")
    public ResponseEntity<ApiResponse<OrderItemResponse>> updateItemStatus(
            @PathVariable UUID id,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateOrderItemStatusRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.of(orderService.updateOrderItemStatus(id, itemId, request, principal)));
    }
}
