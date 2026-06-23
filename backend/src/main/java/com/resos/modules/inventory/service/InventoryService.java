package com.resos.modules.inventory.service;

import com.resos.modules.audit.domain.AuditAction;
import com.resos.modules.audit.service.AuditLogService;
import com.resos.modules.inventory.domain.*;
import com.resos.modules.inventory.dto.*;
import com.resos.modules.inventory.repository.InventoryItemRepository;
import com.resos.modules.inventory.repository.InventoryItemSpecifications;
import com.resos.modules.inventory.repository.InventoryTransactionRepository;
import com.resos.modules.inventory.repository.StockAlertRepository;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private static final String ENTITY_TYPE = "InventoryItem";

    private final InventoryItemRepository inventoryItemRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final StockAlertRepository stockAlertRepository;
    private final RestaurantRepository restaurantRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public ApiResponse<List<InventoryItemResponse>> listItems(
            UUID restaurantId,
            String category,
            Boolean lowStock,
            String search,
            Pageable pageable) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        Specification<InventoryItem> spec = InventoryItemSpecifications.forTenant(tenantId);
        if (restaurantId != null) {
            spec = spec.and(InventoryItemSpecifications.forRestaurant(restaurantId));
        }
        if (category != null) {
            spec = spec.and(InventoryItemSpecifications.forCategory(category));
        }
        if (Boolean.TRUE.equals(lowStock)) {
            spec = spec.and(InventoryItemSpecifications.lowStockOnly());
        }
        if (search != null && !search.isBlank()) {
            spec = spec.and(InventoryItemSpecifications.search(search.trim()));
        }

        Page<InventoryItem> page = inventoryItemRepository.findAll(spec, pageable);
        List<InventoryItemResponse> data = page.getContent().stream().map(this::toResponse).toList();
        return ApiResponse.page(data, new ApiResponse.PageMeta(
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages()));
    }

    @Transactional(readOnly = true)
    public InventoryItemResponse getItem(UUID id) {
        return toResponse(findItemOrThrow(id));
    }

    @Transactional
    public InventoryItemResponse createItem(CreateInventoryItemRequest request, UserPrincipal principal) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        validateRestaurant(tenantId, request.restaurantId());

        InventoryItem item = InventoryItem.builder()
                .tenantId(tenantId)
                .restaurantId(request.restaurantId())
                .name(request.name())
                .sku(request.sku())
                .category(request.category())
                .unit(request.unit())
                .currentStock(defaultDecimal(request.currentStock()))
                .minimumStock(defaultDecimal(request.minimumStock()))
                .maximumStock(request.maximumStock())
                .unitCost(request.unitCost())
                .supplier(request.supplier())
                .build();

        item = inventoryItemRepository.save(item);
        evaluateStockAlerts(item);
        auditLogService.log(
                AuditAction.CREATE,
                ENTITY_TYPE,
                item.getId(),
                null,
                snapshot(item),
                principal.getId());

        return toResponse(item);
    }

    @Transactional
    public InventoryItemResponse updateItem(
            UUID id,
            UpdateInventoryItemRequest request,
            Integer expectedVersion,
            UserPrincipal principal) {
        InventoryItem item = findItemOrThrow(id);
        checkVersion(item, expectedVersion);

        Map<String, Object> before = snapshot(item);

        if (request.name() != null) {
            item.setName(request.name());
        }
        if (request.sku() != null) {
            item.setSku(request.sku());
        }
        if (request.category() != null) {
            item.setCategory(request.category());
        }
        if (request.unit() != null) {
            item.setUnit(request.unit());
        }
        if (request.currentStock() != null) {
            item.setCurrentStock(request.currentStock());
        }
        if (request.minimumStock() != null) {
            item.setMinimumStock(request.minimumStock());
        }
        if (request.maximumStock() != null) {
            item.setMaximumStock(request.maximumStock());
        }
        if (request.unitCost() != null) {
            item.setUnitCost(request.unitCost());
        }
        if (request.supplier() != null) {
            item.setSupplier(request.supplier());
        }
        if (request.expiryDate() != null) {
            item.setExpiryDate(request.expiryDate());
        }

        item = inventoryItemRepository.save(item);
        evaluateStockAlerts(item);
        auditLogService.log(
                AuditAction.UPDATE,
                ENTITY_TYPE,
                item.getId(),
                before,
                snapshot(item),
                principal.getId());

        return toResponse(item);
    }

    @Transactional
    public void deleteItem(UUID id, UserPrincipal principal) {
        InventoryItem item = findItemOrThrow(id);
        Map<String, Object> before = snapshot(item);
        item.setDeletedAt(Instant.now());
        inventoryItemRepository.save(item);
        auditLogService.log(
                AuditAction.DELETE,
                ENTITY_TYPE,
                item.getId(),
                before,
                null,
                principal.getId());
    }

    @Transactional
    public InventoryTransactionResponse recordTransaction(
            UUID itemId,
            CreateTransactionRequest request,
            UserPrincipal principal) {
        InventoryItem item = findItemOrThrow(itemId);
        BigDecimal signedQuantity = resolveSignedQuantity(request.type(), request.quantity());

        BigDecimal newStock = item.getCurrentStock().add(signedQuantity);
        if (newStock.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("INSUFFICIENT_STOCK", "Transaction would result in negative stock");
        }

        InventoryTransaction transaction = InventoryTransaction.builder()
                .tenantId(item.getTenantId())
                .inventoryItemId(item.getId())
                .type(request.type())
                .quantity(signedQuantity)
                .unitCost(request.unitCost())
                .reference(request.reference())
                .notes(request.notes())
                .performedBy(principal.getId())
                .build();
        transaction = transactionRepository.save(transaction);

        item.setCurrentStock(newStock);
        if (request.unitCost() != null && request.type() == TransactionType.PURCHASE) {
            item.setUnitCost(request.unitCost());
        }
        inventoryItemRepository.save(item);
        evaluateStockAlerts(item);

        auditLogService.log(
                AuditAction.TRANSACTION,
                ENTITY_TYPE,
                item.getId(),
                Map.of("currentStock", item.getCurrentStock().subtract(signedQuantity)),
                Map.of(
                        "transactionId", transaction.getId(),
                        "type", request.type().name(),
                        "quantity", signedQuantity,
                        "currentStock", newStock),
                principal.getId());

        return toTransactionResponse(transaction);
    }

    @Transactional(readOnly = true)
    public Page<InventoryTransactionResponse> listTransactions(UUID itemId, Pageable pageable) {
        InventoryItem item = findItemOrThrow(itemId);
        return transactionRepository
                .findByInventoryItemIdAndTenantIdOrderByCreatedAtDesc(item.getId(), item.getTenantId(), pageable)
                .map(this::toTransactionResponse);
    }

    @Transactional(readOnly = true)
    public List<StockAlertResponse> listAlerts(UUID restaurantId, boolean acknowledged) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        List<StockAlert> alerts = restaurantId != null
                ? stockAlertRepository.findAlertsForRestaurant(tenantId, restaurantId, acknowledged)
                : stockAlertRepository.findByTenantIdAndAcknowledgedOrderByCreatedAtDesc(tenantId, acknowledged);

        return alerts.stream().map(alert -> {
            InventoryItem item = inventoryItemRepository
                    .findByIdAndTenantIdAndDeletedAtIsNull(alert.getInventoryItemId(), tenantId)
                    .orElse(null);
            return toAlertResponse(alert, item != null ? item.getName() : "Unknown item");
        }).toList();
    }

    @Transactional
    public StockAlertResponse acknowledgeAlert(UUID alertId, UserPrincipal principal) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        StockAlert alert = stockAlertRepository.findByIdAndTenantId(alertId, tenantId)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "Stock alert not found"));

        if (alert.isAcknowledged()) {
            return toAlertResponse(alert, resolveItemName(alert.getInventoryItemId(), tenantId));
        }

        alert.setAcknowledged(true);
        alert.setAcknowledgedBy(principal.getId());
        alert.setAcknowledgedAt(Instant.now());
        alert = stockAlertRepository.save(alert);

        return toAlertResponse(alert, resolveItemName(alert.getInventoryItemId(), tenantId));
    }

    @Transactional(readOnly = true)
    public long countLowStockItems(UUID restaurantId) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        validateRestaurant(tenantId, restaurantId);
        return inventoryItemRepository.countLowStockByRestaurant(tenantId, restaurantId);
    }

    private void evaluateStockAlerts(InventoryItem item) {
        if (item.getCurrentStock().compareTo(BigDecimal.ZERO) <= 0) {
            createAlertIfMissing(item, AlertType.OUT_OF_STOCK,
                    "%s is out of stock".formatted(item.getName()));
        } else if (item.isLowStock()) {
            createAlertIfMissing(item, AlertType.LOW_STOCK,
                    "%s is below minimum stock (%.2f %s remaining)".formatted(
                            item.getName(), item.getCurrentStock(), item.getUnit()));
        }

        if (item.getExpiryDate() != null && !item.getExpiryDate().isAfter(LocalDate.now().plusDays(7))) {
            createAlertIfMissing(item, AlertType.EXPIRING,
                    "%s expires on %s".formatted(item.getName(), item.getExpiryDate()));
        }
    }

    private void createAlertIfMissing(InventoryItem item, AlertType type, String message) {
        boolean exists = stockAlertRepository
                .findByTenantIdAndAcknowledgedOrderByCreatedAtDesc(item.getTenantId(), false)
                .stream()
                .anyMatch(a -> a.getInventoryItemId().equals(item.getId()) && a.getAlertType() == type);

        if (!exists) {
            stockAlertRepository.save(StockAlert.builder()
                    .tenantId(item.getTenantId())
                    .inventoryItemId(item.getId())
                    .alertType(type)
                    .message(message)
                    .build());
        }
    }

    private BigDecimal resolveSignedQuantity(TransactionType type, BigDecimal quantity) {
        return switch (type) {
            case PURCHASE, ADJUSTMENT -> quantity;
            case USAGE, WASTE, TRANSFER -> quantity.negate();
        };
    }

    private InventoryItem findItemOrThrow(UUID id) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        return inventoryItemRepository.findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "Inventory item not found"));
    }

    private void validateRestaurant(UUID tenantId, UUID restaurantId) {
        restaurantRepository.findByIdAndTenantIdAndDeletedAtIsNull(restaurantId, tenantId)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "Restaurant not found"));
    }

    private void checkVersion(InventoryItem item, Integer expectedVersion) {
        if (expectedVersion != null && item.getVersion() != expectedVersion) {
            throw new BusinessException("OPTIMISTIC_LOCK", "Inventory item was modified by another user");
        }
    }

    private String resolveItemName(UUID itemId, UUID tenantId) {
        return inventoryItemRepository.findByIdAndTenantIdAndDeletedAtIsNull(itemId, tenantId)
                .map(InventoryItem::getName)
                .orElse("Unknown item");
    }

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private Map<String, Object> snapshot(InventoryItem item) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", item.getName());
        map.put("sku", item.getSku());
        map.put("category", item.getCategory());
        map.put("currentStock", item.getCurrentStock());
        map.put("minimumStock", item.getMinimumStock());
        map.put("unitCost", item.getUnitCost());
        return map;
    }

    private InventoryItemResponse toResponse(InventoryItem item) {
        return new InventoryItemResponse(
                item.getId(),
                item.getName(),
                item.getSku(),
                item.getCategory(),
                item.getUnit(),
                item.getCurrentStock(),
                item.getMinimumStock(),
                item.getMaximumStock(),
                item.getUnitCost(),
                item.getSupplier(),
                item.getExpiryDate(),
                item.isLowStock(),
                item.getRestaurantId(),
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getVersion());
    }

    private InventoryTransactionResponse toTransactionResponse(InventoryTransaction transaction) {
        return new InventoryTransactionResponse(
                transaction.getId(),
                transaction.getInventoryItemId(),
                transaction.getType(),
                transaction.getQuantity(),
                transaction.getUnitCost(),
                transaction.getReference(),
                transaction.getNotes(),
                transaction.getPerformedBy(),
                transaction.getCreatedAt());
    }

    private StockAlertResponse toAlertResponse(StockAlert alert, String itemName) {
        return new StockAlertResponse(
                alert.getId(),
                alert.getInventoryItemId(),
                itemName,
                alert.getAlertType(),
                alert.getMessage(),
                alert.isAcknowledged(),
                alert.getAcknowledgedBy(),
                alert.getAcknowledgedAt(),
                alert.getCreatedAt());
    }
}
