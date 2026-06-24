package com.resos.modules.reservation.controller;

import com.resos.modules.reservation.domain.ReservationStatus;
import com.resos.modules.reservation.dto.*;
import com.resos.modules.reservation.service.ReservationService;
import com.resos.shared.api.ApiResponse;
import com.resos.shared.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping("/availability")
    @PreAuthorize("hasAuthority('reservations:read')")
    public ResponseEntity<ApiResponse<AvailabilityResponse>> availability(
            @RequestParam UUID restaurantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam int partySize,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime) {
        return ResponseEntity.ok(
                ApiResponse.of(reservationService.checkAvailability(restaurantId, date, partySize, startTime)));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('reservations:read')")
    public ResponseEntity<ApiResponse<List<ReservationResponse>>> list(
            @RequestParam(required = false) UUID restaurantId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) String search,
            @org.springframework.data.web.PageableDefault(size = 50)
                    org.springframework.data.domain.Pageable pageable) {
        return ResponseEntity.ok(
                reservationService.listReservations(restaurantId, date, startDate, endDate, status, search, pageable));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('reservations:write')")
    public ResponseEntity<ApiResponse<ReservationResponse>> create(
            @Valid @RequestBody CreateReservationRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(reservationService.createReservation(request, principal)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('reservations:read')")
    public ResponseEntity<ApiResponse<ReservationResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.of(reservationService.getReservation(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('reservations:write')")
    public ResponseEntity<ApiResponse<ReservationResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateReservationRequest request,
            @RequestHeader(value = "If-Match", required = false) Integer ifMatch,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                ApiResponse.of(reservationService.updateReservation(id, request, ifMatch, principal)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('reservations:write')")
    public ResponseEntity<ApiResponse<ReservationResponse>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateReservationStatusRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ApiResponse.of(reservationService.updateStatus(id, request, principal)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('reservations:write')")
    public ResponseEntity<Void> cancel(
            @PathVariable UUID id, @AuthenticationPrincipal UserPrincipal principal) {
        reservationService.cancelReservation(id, principal);
        return ResponseEntity.noContent().build();
    }
}
