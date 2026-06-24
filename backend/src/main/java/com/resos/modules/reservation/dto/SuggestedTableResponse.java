package com.resos.modules.reservation.dto;

import java.util.UUID;

public record SuggestedTableResponse(UUID id, String tableNumber, int capacity) {}
