package com.resos.modules.user.controller;

import com.resos.modules.user.dto.CreateUserRequest;
import com.resos.modules.user.dto.UpdateUserRequest;
import com.resos.modules.user.dto.UserResponse;
import com.resos.modules.user.service.UserService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('users:read')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> listUsers(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<UserResponse> users = userService.listUsers(principal.getTenantId(), pageable);
        return ResponseEntity.ok(ApiResponse.of(users));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('users:write')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        UserResponse user = userService.createUser(request, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(user));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('users:read') or #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {

        return ResponseEntity.ok(ApiResponse.of(userService.getUser(id, principal)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('users:write') or #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        return ResponseEntity.ok(ApiResponse.of(userService.updateUser(id, request, principal)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('users:write')")
    public ResponseEntity<Void> deleteUser(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal) {

        userService.deleteUser(id, principal);
        return ResponseEntity.noContent().build();
    }
}
