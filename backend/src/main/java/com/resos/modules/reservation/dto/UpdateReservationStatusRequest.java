package com.resos.modules.reservation.dto;

import com.resos.modules.reservation.domain.ReservationStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateReservationStatusRequest(@NotNull ReservationStatus status) {}
