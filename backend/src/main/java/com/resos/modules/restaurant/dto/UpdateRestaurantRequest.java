package com.resos.modules.restaurant.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record UpdateRestaurantRequest(
        @Size(max = 255) String name,
        String address,
        @Size(max = 20) String phone,
        @Email @Size(max = 255) String email,
        @Min(1) Integer capacity,
        Map<String, Object> openingHours,
        Boolean active
) {}
