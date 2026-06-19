package com.resos.modules.restaurant.service;

import com.resos.modules.restaurant.domain.Restaurant;
import com.resos.modules.restaurant.dto.CreateRestaurantRequest;
import com.resos.modules.restaurant.dto.RestaurantResponse;
import com.resos.modules.restaurant.dto.UpdateRestaurantRequest;
import com.resos.modules.restaurant.repository.RestaurantRepository;
import com.resos.shared.exception.BusinessException;
import com.resos.shared.tenant.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    @Transactional(readOnly = true)
    public List<RestaurantResponse> listRestaurants() {
        UUID tenantId = TenantContextHolder.requireTenantId();
        return restaurantRepository.findByTenantIdAndDeletedAtIsNull(tenantId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public RestaurantResponse getRestaurant(UUID id) {
        return toResponse(findRestaurantOrThrow(id));
    }

    @Transactional
    public RestaurantResponse createRestaurant(CreateRestaurantRequest request) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        Restaurant restaurant = Restaurant.builder()
                .tenantId(tenantId)
                .name(request.name())
                .address(request.address())
                .phone(request.phone())
                .email(request.email())
                .capacity(request.capacity())
                .openingHours(request.openingHours() != null ? request.openingHours() : Map.of())
                .build();
        return toResponse(restaurantRepository.save(restaurant));
    }

    @Transactional
    public RestaurantResponse createDefaultRestaurant(UUID tenantId, String tenantName) {
        Restaurant restaurant = Restaurant.builder()
                .tenantId(tenantId)
                .name(tenantName)
                .address("Add your restaurant address in settings")
                .capacity(50)
                .openingHours(defaultOpeningHours())
                .build();
        return toResponse(restaurantRepository.save(restaurant));
    }

    @Transactional
    public RestaurantResponse updateRestaurant(UUID id, UpdateRestaurantRequest request) {
        Restaurant restaurant = findRestaurantOrThrow(id);
        if (request.name() != null) {
            restaurant.setName(request.name());
        }
        if (request.address() != null) {
            restaurant.setAddress(request.address());
        }
        if (request.phone() != null) {
            restaurant.setPhone(request.phone());
        }
        if (request.email() != null) {
            restaurant.setEmail(request.email());
        }
        if (request.capacity() != null) {
            restaurant.setCapacity(request.capacity());
        }
        if (request.openingHours() != null) {
            restaurant.setOpeningHours(new HashMap<>(request.openingHours()));
        }
        if (request.active() != null) {
            restaurant.setActive(request.active());
        }
        return toResponse(restaurantRepository.save(restaurant));
    }

    @Transactional
    public void deleteRestaurant(UUID id) {
        Restaurant restaurant = findRestaurantOrThrow(id);
        restaurant.setDeletedAt(Instant.now());
        restaurant.setActive(false);
        restaurantRepository.save(restaurant);
    }

    private Restaurant findRestaurantOrThrow(UUID id) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        return restaurantRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "Restaurant not found"));
    }

    private RestaurantResponse toResponse(Restaurant restaurant) {
        return new RestaurantResponse(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getAddress(),
                restaurant.getPhone(),
                restaurant.getEmail(),
                restaurant.getCapacity(),
                restaurant.getOpeningHours(),
                restaurant.isActive(),
                restaurant.getTenantId(),
                restaurant.getCreatedAt(),
                restaurant.getVersion()
        );
    }

    private Map<String, Object> defaultOpeningHours() {
        Map<String, Object> day = Map.of("open", "09:00", "close", "22:00");
        return Map.of(
                "monday", day,
                "tuesday", day,
                "wednesday", day,
                "thursday", day,
                "friday", day,
                "saturday", day,
                "sunday", day
        );
    }
}
