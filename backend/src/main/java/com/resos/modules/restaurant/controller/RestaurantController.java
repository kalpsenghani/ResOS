package com.resos.modules.restaurant.controller;

import com.resos.modules.restaurant.dto.CreateRestaurantRequest;
import com.resos.modules.restaurant.dto.RestaurantResponse;
import com.resos.modules.restaurant.dto.UpdateRestaurantRequest;
import com.resos.modules.restaurant.service.RestaurantService;
import com.resos.shared.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<RestaurantResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.of(restaurantService.listRestaurants()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<RestaurantResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.of(restaurantService.getRestaurant(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('settings:write')")
    public ResponseEntity<ApiResponse<RestaurantResponse>> create(@Valid @RequestBody CreateRestaurantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(restaurantService.createRestaurant(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('settings:write')")
    public ResponseEntity<ApiResponse<RestaurantResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRestaurantRequest request) {
        return ResponseEntity.ok(ApiResponse.of(restaurantService.updateRestaurant(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('settings:write')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        restaurantService.deleteRestaurant(id);
        return ResponseEntity.noContent().build();
    }
}
