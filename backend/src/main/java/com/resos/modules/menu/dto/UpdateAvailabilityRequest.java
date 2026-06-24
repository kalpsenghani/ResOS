package com.resos.modules.menu.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateAvailabilityRequest(@NotNull Boolean isAvailable) {}
