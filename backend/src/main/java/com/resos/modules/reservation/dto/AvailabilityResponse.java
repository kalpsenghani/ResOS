package com.resos.modules.reservation.dto;

import java.util.List;

public record AvailabilityResponse(boolean available, List<SuggestedTableResponse> suggestedTables) {}
