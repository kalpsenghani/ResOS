package com.resos.modules.reservation.controller;

import com.resos.modules.reservation.dto.*;
import com.resos.modules.reservation.service.TableService;
import com.resos.shared.api.ApiResponse;
import com.resos.shared.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tables")
@RequiredArgsConstructor
public class TableController {

    private final TableService tableService;

    @GetMapping
    @PreAuthorize("hasAuthority('reservations:read')")
    public ResponseEntity<ApiResponse<List<TableResponse>>> list(@RequestParam UUID restaurantId) {
        return ResponseEntity.ok(ApiResponse.of(tableService.listTables(restaurantId)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('reservations:write')")
    public ResponseEntity<ApiResponse<TableResponse>> create(
            @Valid @RequestBody CreateTableRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(tableService.createTable(request, principal)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('reservations:read')")
    public ResponseEntity<ApiResponse<TableResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.of(tableService.getTable(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('reservations:write')")
    public ResponseEntity<ApiResponse<TableResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTableRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.of(tableService.updateTable(id, request, principal)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('reservations:write')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id, @AuthenticationPrincipal UserPrincipal principal) {
        tableService.deleteTable(id, principal);
        return ResponseEntity.noContent().build();
    }
}
