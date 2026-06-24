package com.resos.modules.reservation.dto;

import com.resos.modules.reservation.domain.ReservationStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ReservationResponse(
        UUID id,
        UUID restaurantId,
        UUID tableId,
        String tableNumber,
        String guestName,
        String guestPhone,
        String guestEmail,
        int partySize,
        LocalDate reservationDate,
        LocalTime startTime,
        LocalTime endTime,
        ReservationStatus status,
        String specialRequests,
        Instant createdAt,
        Instant updatedAt,
        int version
) {}
