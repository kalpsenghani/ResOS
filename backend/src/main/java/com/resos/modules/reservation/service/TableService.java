package com.resos.modules.reservation.service;

import com.resos.modules.audit.domain.AuditAction;
import com.resos.modules.audit.service.AuditLogService;
import com.resos.modules.reservation.domain.RestaurantTable;
import com.resos.modules.reservation.dto.*;
import com.resos.modules.reservation.repository.ReservationRepository;
import com.resos.modules.reservation.repository.RestaurantTableRepository;
import com.resos.modules.restaurant.repository.RestaurantRepository;
import com.resos.modules.reservation.domain.ReservationStatus;
import com.resos.shared.exception.BusinessException;
import com.resos.shared.security.UserPrincipal;
import com.resos.shared.tenant.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TableService {

    private static final String ENTITY_TYPE = "RestaurantTable";

    private final RestaurantTableRepository tableRepository;
    private final RestaurantRepository restaurantRepository;
    private final ReservationRepository reservationRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<TableResponse> listTables(UUID restaurantId) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        validateRestaurant(tenantId, restaurantId);
        return tableRepository.findByTenantIdAndRestaurantIdOrderByTableNumberAsc(tenantId, restaurantId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TableResponse getTable(UUID id) {
        return toResponse(findTableOrThrow(id));
    }

    @Transactional
    public TableResponse createTable(CreateTableRequest request, UserPrincipal principal) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        validateRestaurant(tenantId, request.restaurantId());

        if (tableRepository.existsByTenantIdAndRestaurantIdAndTableNumber(
                tenantId, request.restaurantId(), request.tableNumber())) {
            throw new BusinessException("VALIDATION_ERROR", "Table number already exists");
        }

        RestaurantTable table = RestaurantTable.builder()
                .tenantId(tenantId)
                .restaurantId(request.restaurantId())
                .tableNumber(request.tableNumber())
                .capacity(request.capacity())
                .location(request.location())
                .active(true)
                .build();

        table = tableRepository.save(table);
        auditLogService.log(AuditAction.CREATE, ENTITY_TYPE, table.getId(), null, snapshot(table), principal.getId());
        return toResponse(table);
    }

    @Transactional
    public TableResponse updateTable(UUID id, UpdateTableRequest request, UserPrincipal principal) {
        RestaurantTable table = findTableOrThrow(id);
        Map<String, Object> before = snapshot(table);

        if (request.tableNumber() != null && !request.tableNumber().equals(table.getTableNumber())) {
            if (tableRepository.existsByTenantIdAndRestaurantIdAndTableNumberAndIdNot(
                    table.getTenantId(), table.getRestaurantId(), request.tableNumber(), table.getId())) {
                throw new BusinessException("VALIDATION_ERROR", "Table number already exists");
            }
            table.setTableNumber(request.tableNumber());
        }
        if (request.capacity() != null) table.setCapacity(request.capacity());
        if (request.location() != null) table.setLocation(request.location());
        if (request.active() != null) table.setActive(request.active());

        table = tableRepository.save(table);
        auditLogService.log(AuditAction.UPDATE, ENTITY_TYPE, table.getId(), before, snapshot(table), principal.getId());
        return toResponse(table);
    }

    @Transactional
    public void deleteTable(UUID id, UserPrincipal principal) {
        RestaurantTable table = findTableOrThrow(id);
        long activeReservations = reservationRepository.countByTenantIdAndRestaurantIdAndTableIdAndStatusNotIn(
                table.getTenantId(),
                table.getRestaurantId(),
                table.getId(),
                List.of(ReservationStatus.CANCELLED, ReservationStatus.NO_SHOW, ReservationStatus.COMPLETED));
        if (activeReservations > 0) {
            throw new BusinessException("VALIDATION_ERROR", "Cannot delete table with active reservations");
        }

        auditLogService.log(AuditAction.DELETE, ENTITY_TYPE, table.getId(), snapshot(table), null, principal.getId());
        tableRepository.delete(table);
    }

    private RestaurantTable findTableOrThrow(UUID id) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        return tableRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "Table not found"));
    }

    private void validateRestaurant(UUID tenantId, UUID restaurantId) {
        restaurantRepository.findByIdAndTenantIdAndDeletedAtIsNull(restaurantId, tenantId)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "Restaurant not found"));
    }

    private Map<String, Object> snapshot(RestaurantTable table) {
        Map<String, Object> map = new HashMap<>();
        map.put("tableNumber", table.getTableNumber());
        map.put("capacity", table.getCapacity());
        map.put("location", table.getLocation());
        map.put("active", table.isActive());
        return map;
    }

    private TableResponse toResponse(RestaurantTable table) {
        return new TableResponse(
                table.getId(),
                table.getRestaurantId(),
                table.getTableNumber(),
                table.getCapacity(),
                table.getLocation(),
                table.isActive(),
                table.getCreatedAt());
    }
}
