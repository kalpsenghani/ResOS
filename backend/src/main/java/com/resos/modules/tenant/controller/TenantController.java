package com.resos.modules.tenant.controller;

import com.resos.modules.tenant.dto.TenantResponse;
import com.resos.modules.tenant.dto.UpdateTenantRequest;
import com.resos.modules.tenant.service.TenantService;
import com.resos.shared.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @GetMapping("/current")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TenantResponse>> getCurrentTenant() {
        return ResponseEntity.ok(ApiResponse.of(tenantService.getCurrentTenant()));
    }

    @PutMapping("/current")
    @PreAuthorize("hasAuthority('settings:write')")
    public ResponseEntity<ApiResponse<TenantResponse>> updateCurrentTenant(
            @Valid @RequestBody UpdateTenantRequest request) {
        return ResponseEntity.ok(ApiResponse.of(tenantService.updateCurrentTenant(request)));
    }
}
