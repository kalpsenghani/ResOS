package com.resos.modules.inventory.controller;

import com.resos.modules.inventory.dto.*;
import com.resos.modules.inventory.service.InventoryService;
import com.resos.shared.api.ApiResponse;
import com.resos.shared.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/alerts")
    @PreAuthorize("hasAuthority('inventory:read')")
    public ResponseEntity<ApiResponse<List<StockAlertResponse>>> listAlerts(
            @RequestParam(required = false) UUID restaurantId,
            @RequestParam(defaultValue = "false") boolean acknowledged) {
        return ResponseEntity.ok(ApiResponse.of(inventoryService.listAlerts(restaurantId, acknowledged)));
    }

    @PatchMapping("/alerts/{id}/acknowledge")
    @PreAuthorize("hasAuthority('inventory:write')")
    public ResponseEntity<ApiResponse<StockAlertResponse>> acknowledgeAlert(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.of(inventoryService.acknowledgeAlert(id, principal)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('inventory:read')")
    public ResponseEntity<ApiResponse<List<InventoryItemResponse>>> list(
            @RequestParam(required = false) UUID restaurantId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean lowStock,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(inventoryService.listItems(restaurantId, category, lowStock, search, pageable));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('inventory:write')")
    public ResponseEntity<ApiResponse<InventoryItemResponse>> create(
            @Valid @RequestBody CreateInventoryItemRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(inventoryService.createItem(request, principal)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory:read')")
    public ResponseEntity<ApiResponse<InventoryItemResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.of(inventoryService.getItem(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory:write')")
    public ResponseEntity<ApiResponse<InventoryItemResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateInventoryItemRequest request,
            @RequestHeader(value = "If-Match", required = false) Integer ifMatch,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.of(inventoryService.updateItem(id, request, ifMatch, principal)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('inventory:delete')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {
        inventoryService.deleteItem(id, principal);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/transactions")
    @PreAuthorize("hasAuthority('inventory:write')")
    public ResponseEntity<ApiResponse<InventoryTransactionResponse>> recordTransaction(
            @PathVariable UUID id,
            @Valid @RequestBody CreateTransactionRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(inventoryService.recordTransaction(id, request, principal)));
    }

    @GetMapping("/{id}/transactions")
    @PreAuthorize("hasAuthority('inventory:read')")
    public ResponseEntity<ApiResponse<Page<InventoryTransactionResponse>>> listTransactions(
            @PathVariable UUID id,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.of(inventoryService.listTransactions(id, pageable)));
    }
}
