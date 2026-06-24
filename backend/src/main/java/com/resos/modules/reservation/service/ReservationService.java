package com.resos.modules.reservation.service;

import com.resos.modules.audit.domain.AuditAction;
import com.resos.modules.audit.service.AuditLogService;
import com.resos.modules.reservation.domain.Reservation;
import com.resos.modules.reservation.domain.ReservationStatus;
import com.resos.modules.reservation.domain.RestaurantTable;
import com.resos.modules.reservation.dto.*;
import com.resos.modules.reservation.repository.ReservationRepository;
import com.resos.modules.reservation.repository.ReservationSpecifications;
import com.resos.modules.reservation.repository.RestaurantTableRepository;
import com.resos.modules.restaurant.repository.RestaurantRepository;
import com.resos.shared.api.ApiResponse;
import com.resos.shared.exception.BusinessException;
import com.resos.shared.security.UserPrincipal;
import com.resos.shared.tenant.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private static final String ENTITY_TYPE = "Reservation";
    private static final int DEFAULT_DURATION_HOURS = 2;

    private static final Map<ReservationStatus, Set<ReservationStatus>> STATUS_TRANSITIONS = Map.of(
            ReservationStatus.PENDING, Set.of(ReservationStatus.CONFIRMED, ReservationStatus.CANCELLED),
            ReservationStatus.CONFIRMED,
                    Set.of(ReservationStatus.SEATED, ReservationStatus.CANCELLED, ReservationStatus.NO_SHOW),
            ReservationStatus.SEATED, Set.of(ReservationStatus.COMPLETED),
            ReservationStatus.COMPLETED, Set.of(),
            ReservationStatus.CANCELLED, Set.of(),
            ReservationStatus.NO_SHOW, Set.of());


    private static final List<ReservationStatus> DASHBOARD_STATUSES =
            List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED, ReservationStatus.SEATED);

    private final ReservationRepository reservationRepository;
    private final RestaurantTableRepository tableRepository;
    private final RestaurantRepository restaurantRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public ApiResponse<List<ReservationResponse>> listReservations(
            UUID restaurantId,
            LocalDate date,
            LocalDate startDate,
            LocalDate endDate,
            ReservationStatus status,
            String search,
            Pageable pageable) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        Specification<Reservation> spec = ReservationSpecifications.forTenant(tenantId);
        if (restaurantId != null) {
            validateRestaurant(tenantId, restaurantId);
            spec = spec.and(ReservationSpecifications.forRestaurant(restaurantId));
        }
        if (date != null) {
            spec = spec.and(ReservationSpecifications.forDate(date));
        } else if (startDate != null && endDate != null) {
            spec = spec.and(ReservationSpecifications.forDateRange(startDate, endDate));
        }
        if (status != null) {
            spec = spec.and(ReservationSpecifications.forStatus(status));
        }
        if (search != null && !search.isBlank()) {
            spec = spec.and(ReservationSpecifications.searchGuest(search.trim()));
        }

        Page<Reservation> page = reservationRepository.findAll(spec, pageable);
        Map<UUID, String> tableNumbers = loadTableNumbers(page.getContent());
        List<ReservationResponse> data =
                page.getContent().stream().map(r -> toResponse(r, tableNumbers)).toList();
        return ApiResponse.page(data, new ApiResponse.PageMeta(
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages()));
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservation(UUID id) {
        Reservation reservation = findReservationOrThrow(id);
        Map<UUID, String> tableNumbers = loadTableNumbers(List.of(reservation));
        return toResponse(reservation, tableNumbers);
    }

    @Transactional
    public ReservationResponse createReservation(CreateReservationRequest request, UserPrincipal principal) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        validateRestaurant(tenantId, request.restaurantId());

        LocalTime endTime = resolveEndTime(request.startTime(), request.endTime());
        RestaurantTable table = resolveTable(tenantId, request.restaurantId(), request.tableId(), request.partySize());

        if (table != null) {
            ensureTableAvailable(table.getId(), request.reservationDate(), request.startTime(), endTime, null);
        } else {
            ensureCapacityAvailable(
                    tenantId, request.restaurantId(), request.reservationDate(), request.startTime(), endTime, request.partySize());
        }

        Reservation reservation = Reservation.builder()
                .tenantId(tenantId)
                .restaurantId(request.restaurantId())
                .tableId(table != null ? table.getId() : null)
                .guestName(request.guestName())
                .guestPhone(request.guestPhone())
                .guestEmail(request.guestEmail())
                .partySize(request.partySize())
                .reservationDate(request.reservationDate())
                .startTime(request.startTime())
                .endTime(endTime)
                .status(ReservationStatus.CONFIRMED)
                .specialRequests(request.specialRequests())
                .createdBy(principal.getId())
                .build();

        reservation = reservationRepository.save(reservation);
        auditLogService.log(
                AuditAction.CREATE, ENTITY_TYPE, reservation.getId(), null, snapshot(reservation), principal.getId());
        return toResponse(reservation, loadTableNumbers(List.of(reservation)));
    }

    @Transactional
    public ReservationResponse updateReservation(
            UUID id, UpdateReservationRequest request, Integer expectedVersion, UserPrincipal principal) {
        Reservation reservation = findReservationOrThrow(id);
        ensureModifiable(reservation);
        checkVersion(reservation, expectedVersion);
        Map<String, Object> before = snapshot(reservation);

        UUID tableId = request.tableId() != null ? request.tableId() : reservation.getTableId();
        LocalDate date = request.reservationDate() != null ? request.reservationDate() : reservation.getReservationDate();
        LocalTime startTime = request.startTime() != null ? request.startTime() : reservation.getStartTime();
        LocalTime endTime = request.endTime() != null ? request.endTime() : reservation.getEndTime();
        endTime = resolveEndTime(startTime, endTime);
        int partySize = request.partySize() != null ? request.partySize() : reservation.getPartySize();

        if (request.tableId() != null || request.partySize() != null) {
            RestaurantTable table = resolveTable(reservation.getTenantId(), reservation.getRestaurantId(), tableId, partySize);
            tableId = table != null ? table.getId() : null;
        }

        if (tableId != null) {
            ensureTableAvailable(tableId, date, startTime, endTime, reservation.getId());
        } else if (request.partySize() != null || request.reservationDate() != null || request.startTime() != null) {
            ensureCapacityAvailable(
                    reservation.getTenantId(), reservation.getRestaurantId(), date, startTime, endTime, partySize);
        }

        if (request.guestName() != null) reservation.setGuestName(request.guestName());
        if (request.guestPhone() != null) reservation.setGuestPhone(request.guestPhone());
        if (request.guestEmail() != null) reservation.setGuestEmail(request.guestEmail());
        if (request.partySize() != null) reservation.setPartySize(request.partySize());
        if (request.reservationDate() != null) reservation.setReservationDate(request.reservationDate());
        if (request.startTime() != null) reservation.setStartTime(request.startTime());
        reservation.setEndTime(endTime);
        reservation.setTableId(tableId);
        if (request.specialRequests() != null) reservation.setSpecialRequests(request.specialRequests());
        if (request.status() != null) {
            transitionStatus(reservation, request.status());
        }

        reservation = reservationRepository.save(reservation);
        auditLogService.log(
                AuditAction.UPDATE, ENTITY_TYPE, reservation.getId(), before, snapshot(reservation), principal.getId());
        return toResponse(reservation, loadTableNumbers(List.of(reservation)));
    }

    @Transactional
    public ReservationResponse updateStatus(UUID id, UpdateReservationStatusRequest request, UserPrincipal principal) {
        Reservation reservation = findReservationOrThrow(id);
        Map<String, Object> before = snapshot(reservation);
        transitionStatus(reservation, request.status());
        reservation = reservationRepository.save(reservation);
        auditLogService.log(
                AuditAction.UPDATE, ENTITY_TYPE, reservation.getId(), before, snapshot(reservation), principal.getId());
        return toResponse(reservation, loadTableNumbers(List.of(reservation)));
    }

    @Transactional
    public void cancelReservation(UUID id, UserPrincipal principal) {
        Reservation reservation = findReservationOrThrow(id);
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            return;
        }
        Map<String, Object> before = snapshot(reservation);
        transitionStatus(reservation, ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
        auditLogService.log(
                AuditAction.DELETE, ENTITY_TYPE, reservation.getId(), before, snapshot(reservation), principal.getId());
    }

    @Transactional(readOnly = true)
    public AvailabilityResponse checkAvailability(
            UUID restaurantId, LocalDate date, int partySize, LocalTime startTime) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        validateRestaurant(tenantId, restaurantId);

        LocalTime endTime = resolveEndTime(startTime, null);
        List<SuggestedTableResponse> suggested = findAvailableTables(
                tenantId, restaurantId, date, startTime, endTime, partySize, null);
        return new AvailabilityResponse(!suggested.isEmpty(), suggested);
    }

    @Transactional(readOnly = true)
    public long countTodayReservations(UUID restaurantId) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        validateRestaurant(tenantId, restaurantId);
        return reservationRepository.countByTenantIdAndRestaurantIdAndReservationDateAndStatusIn(
                tenantId, restaurantId, LocalDate.now(), DASHBOARD_STATUSES);
    }

    private List<SuggestedTableResponse> findAvailableTables(
            UUID tenantId,
            UUID restaurantId,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            int partySize,
            UUID excludeReservationId) {
        LocalTime defaultEnd = defaultEndTime(startTime);
        List<UUID> bookedTableIds = reservationRepository.findBookedTableIds(
                tenantId, restaurantId, date, startTime, endTime, defaultEnd);
        Set<UUID> booked = new HashSet<>(bookedTableIds);

        return tableRepository
                .findByTenantIdAndRestaurantIdAndActiveTrueOrderByTableNumberAsc(tenantId, restaurantId)
                .stream()
                .filter(table -> table.getCapacity() >= partySize)
                .filter(table -> !booked.contains(table.getId()))
                .map(table -> new SuggestedTableResponse(table.getId(), table.getTableNumber(), table.getCapacity()))
                .toList();
    }

    private void ensureTableAvailable(
            UUID tableId, LocalDate date, LocalTime startTime, LocalTime endTime, UUID excludeId) {
        LocalTime defaultEnd = defaultEndTime(startTime);
        if (reservationRepository.existsOverlapping(tableId, date, startTime, endTime, defaultEnd, excludeId)) {
            throw new BusinessException("VALIDATION_ERROR", "Table is not available at the requested time");
        }
    }

    private void ensureCapacityAvailable(
            UUID tenantId, UUID restaurantId, LocalDate date, LocalTime startTime, LocalTime endTime, int partySize) {
        List<SuggestedTableResponse> available =
                findAvailableTables(tenantId, restaurantId, date, startTime, endTime, partySize, null);
        if (available.isEmpty()) {
            throw new BusinessException("VALIDATION_ERROR", "No tables available for the requested party size and time");
        }
    }

    private RestaurantTable resolveTable(UUID tenantId, UUID restaurantId, UUID tableId, int partySize) {
        if (tableId == null) {
            return null;
        }
        RestaurantTable table = tableRepository
                .findByIdAndTenantId(tableId, tenantId)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "Table not found"));
        if (!table.getRestaurantId().equals(restaurantId)) {
            throw new BusinessException("VALIDATION_ERROR", "Table does not belong to this restaurant");
        }
        if (!table.isActive()) {
            throw new BusinessException("VALIDATION_ERROR", "Table is not active");
        }
        if (table.getCapacity() < partySize) {
            throw new BusinessException("VALIDATION_ERROR", "Party size exceeds table capacity");
        }
        return table;
    }

    private LocalTime resolveEndTime(LocalTime startTime, LocalTime endTime) {
        if (endTime != null) {
            if (!endTime.isAfter(startTime)) {
                throw new BusinessException("VALIDATION_ERROR", "End time must be after start time");
            }
            return endTime;
        }
        return defaultEndTime(startTime);
    }

    private LocalTime defaultEndTime(LocalTime startTime) {
        return startTime.plusHours(DEFAULT_DURATION_HOURS);
    }

    private void transitionStatus(Reservation reservation, ReservationStatus newStatus) {
        Set<ReservationStatus> allowed = STATUS_TRANSITIONS.getOrDefault(reservation.getStatus(), Set.of());
        if (!allowed.contains(newStatus)) {
            throw new BusinessException(
                    "VALIDATION_ERROR",
                    "Cannot transition from " + reservation.getStatus() + " to " + newStatus);
        }
        reservation.setStatus(newStatus);
    }

    private void ensureModifiable(Reservation reservation) {
        if (reservation.getStatus() == ReservationStatus.CANCELLED
                || reservation.getStatus() == ReservationStatus.COMPLETED
                || reservation.getStatus() == ReservationStatus.NO_SHOW) {
            throw new BusinessException("VALIDATION_ERROR", "Reservation cannot be modified in status " + reservation.getStatus());
        }
    }

    private Reservation findReservationOrThrow(UUID id) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        return reservationRepository
                .findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "Reservation not found"));
    }

    private void validateRestaurant(UUID tenantId, UUID restaurantId) {
        restaurantRepository
                .findByIdAndTenantIdAndDeletedAtIsNull(restaurantId, tenantId)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "Restaurant not found"));
    }

    private void checkVersion(Reservation reservation, Integer expectedVersion) {
        if (expectedVersion != null && reservation.getVersion() != expectedVersion) {
            throw new BusinessException("OPTIMISTIC_LOCK", "Reservation was modified by another user");
        }
    }

    private Map<UUID, String> loadTableNumbers(List<Reservation> reservations) {
        Set<UUID> tableIds = reservations.stream()
                .map(Reservation::getTableId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (tableIds.isEmpty()) {
            return Map.of();
        }
        UUID tenantId = TenantContextHolder.requireTenantId();
        return tableRepository.findAllById(tableIds).stream()
                .filter(table -> table.getTenantId().equals(tenantId))
                .collect(Collectors.toMap(RestaurantTable::getId, RestaurantTable::getTableNumber));
    }

    private Map<String, Object> snapshot(Reservation reservation) {
        Map<String, Object> map = new HashMap<>();
        map.put("guestName", reservation.getGuestName());
        map.put("partySize", reservation.getPartySize());
        map.put("reservationDate", reservation.getReservationDate().toString());
        map.put("startTime", reservation.getStartTime().toString());
        map.put("status", reservation.getStatus().name());
        map.put("tableId", reservation.getTableId());
        return map;
    }

    private ReservationResponse toResponse(Reservation reservation, Map<UUID, String> tableNumbers) {
        String tableNumber =
                reservation.getTableId() != null ? tableNumbers.get(reservation.getTableId()) : null;
        return new ReservationResponse(
                reservation.getId(),
                reservation.getRestaurantId(),
                reservation.getTableId(),
                tableNumber,
                reservation.getGuestName(),
                reservation.getGuestPhone(),
                reservation.getGuestEmail(),
                reservation.getPartySize(),
                reservation.getReservationDate(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getStatus(),
                reservation.getSpecialRequests(),
                reservation.getCreatedAt(),
                reservation.getUpdatedAt(),
                reservation.getVersion());
    }
}
