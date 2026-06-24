package com.resos.modules.reservation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record CreateReservationRequest(
        @NotNull UUID restaurantId,
        UUID tableId,
        @NotBlank String guestName,
        String guestPhone,
        @Email String guestEmail,
        @NotNull @Min(1) Integer partySize,
        @NotNull LocalDate reservationDate,
        @NotNull LocalTime startTime,
        LocalTime endTime,
        String specialRequests
) {}
