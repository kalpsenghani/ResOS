package com.resos.modules.reservation.dto;

import com.resos.modules.reservation.domain.ReservationStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record UpdateReservationRequest(
        UUID tableId,
        String guestName,
        String guestPhone,
        @Email String guestEmail,
        @Min(1) Integer partySize,
        LocalDate reservationDate,
        LocalTime startTime,
        LocalTime endTime,
        ReservationStatus status,
        String specialRequests
) {}
