package com.resos.modules.menu.controller;

import com.resos.modules.menu.dto.*;
import com.resos.modules.menu.service.MenuService;
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
@RequestMapping("/api/v1/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/categories")
    @PreAuthorize("hasAuthority('menu:read')")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> listCategories(@RequestParam UUID restaurantId) {
        return ResponseEntity.ok(ApiResponse.of(menuService.listCategories(restaurantId)));
    }

    @PostMapping("/categories")
    @PreAuthorize("hasAuthority('menu:write')")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(menuService.createCategory(request, principal)));
    }

    @PutMapping("/categories/{id}")
    @PreAuthorize("hasAuthority('menu:write')")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.of(menuService.updateCategory(id, request, principal)));
    }

    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasAuthority('menu:write')")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable UUID id, @AuthenticationPrincipal UserPrincipal principal) {
        menuService.deleteCategory(id, principal);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/items")
    @PreAuthorize("hasAuthority('menu:read')")
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> listItems(
            @RequestParam(required = false) UUID restaurantId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) String search,
            @org.springframework.data.web.PageableDefault(size = 50)
                    org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(menuService.listItems(restaurantId, categoryId, available, search, pageable));
    }

    @PostMapping("/items")
    @PreAuthorize("hasAuthority('menu:write')")
    public ResponseEntity<ApiResponse<MenuItemResponse>> createItem(
            @Valid @RequestBody CreateMenuItemRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(menuService.createItem(request, principal)));
    }

    @GetMapping("/items/{id}")
    @PreAuthorize("hasAuthority('menu:read')")
    public ResponseEntity<ApiResponse<MenuItemResponse>> getItem(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.of(menuService.getItem(id)));
    }

    @PutMapping("/items/{id}")
    @PreAuthorize("hasAuthority('menu:write')")
    public ResponseEntity<ApiResponse<MenuItemResponse>> updateItem(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMenuItemRequest request,
            @RequestHeader(value = "If-Match", required = false) Integer ifMatch,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.of(menuService.updateItem(id, request, ifMatch, principal)));
    }

    @PatchMapping("/items/{id}/availability")
    @PreAuthorize("hasAuthority('menu:write')")
    public ResponseEntity<ApiResponse<MenuItemResponse>> updateAvailability(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAvailabilityRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.of(menuService.updateAvailability(id, request, principal)));
    }

    @DeleteMapping("/items/{id}")
    @PreAuthorize("hasAuthority('menu:write')")
    public ResponseEntity<Void> deleteItem(
            @PathVariable UUID id, @AuthenticationPrincipal UserPrincipal principal) {
        menuService.deleteItem(id, principal);
        return ResponseEntity.noContent().build();
    }
}
