package com.resos.modules.order.service;

import com.resos.modules.audit.domain.AuditAction;
import com.resos.modules.audit.service.AuditLogService;
import com.resos.modules.dashboard.dto.RecentOrderResponse;
import com.resos.modules.menu.domain.MenuItem;
import com.resos.modules.menu.repository.MenuItemRepository;
import com.resos.modules.order.domain.*;
import com.resos.modules.order.dto.*;
import com.resos.modules.order.repository.OrderItemRepository;
import com.resos.modules.order.repository.OrderSpecifications;
import com.resos.modules.order.repository.OrderStatusHistoryRepository;
import com.resos.modules.order.repository.RestaurantOrderRepository;
import com.resos.modules.restaurant.repository.RestaurantRepository;
import com.resos.shared.api.ApiResponse;
import com.resos.shared.exception.BusinessException;
import com.resos.shared.security.UserPrincipal;
import com.resos.shared.tenant.TenantContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final String ENTITY_TYPE = "Order";
    private static final BigDecimal TAX_RATE = new BigDecimal("0.08");

    private static final Map<OrderStatus, Set<OrderStatus>> STATUS_TRANSITIONS = Map.of(
            OrderStatus.PENDING, Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
            OrderStatus.CONFIRMED, Set.of(OrderStatus.PREPARING, OrderStatus.CANCELLED),
            OrderStatus.PREPARING, Set.of(OrderStatus.READY, OrderStatus.CANCELLED),
            OrderStatus.READY, Set.of(OrderStatus.SERVED, OrderStatus.CANCELLED),
            OrderStatus.SERVED, Set.of(OrderStatus.COMPLETED, OrderStatus.CANCELLED),
            OrderStatus.COMPLETED, Set.of(),
            OrderStatus.CANCELLED, Set.of());

    private static final Map<OrderItemStatus, Set<OrderItemStatus>> ITEM_STATUS_TRANSITIONS = Map.of(
            OrderItemStatus.PENDING, Set.of(OrderItemStatus.PREPARING),
            OrderItemStatus.PREPARING, Set.of(OrderItemStatus.READY),
            OrderItemStatus.READY, Set.of(OrderItemStatus.SERVED),
            OrderItemStatus.SERVED, Set.of());

    private static final List<OrderStatus> ACTIVE_ORDER_STATUSES =
            List.of(OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.PREPARING, OrderStatus.READY, OrderStatus.SERVED);

    private final RestaurantOrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public ApiResponse<List<OrderResponse>> listOrders(
            UUID restaurantId, OrderStatus status, LocalDate date, Pageable pageable) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        Specification<RestaurantOrder> spec = OrderSpecifications.forTenant(tenantId);
        if (restaurantId != null) {
            validateRestaurant(tenantId, restaurantId);
            spec = spec.and(OrderSpecifications.forRestaurant(restaurantId));
        }
        if (status != null) {
            spec = spec.and(OrderSpecifications.forStatus(status));
        }
        if (date != null) {
            spec = spec.and(OrderSpecifications.forDate(date));
        }

        Page<RestaurantOrder> page = orderRepository.findAll(spec, pageable);
        page.getContent().forEach(order -> order.getItems().size());
        Map<UUID, String> menuNames = loadMenuItemNames(page.getContent());
        List<OrderResponse> data = page.getContent().stream()
                .map(order -> toResponse(order, menuNames, List.of()))
                .toList();
        return ApiResponse.page(data, new ApiResponse.PageMeta(
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages()));
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID id) {
        RestaurantOrder order = findOrderOrThrow(id);
        order.getItems().size();
        Map<UUID, String> menuNames = loadMenuItemNames(List.of(order));
        List<OrderStatusHistoryResponse> history = historyRepository
                .findByOrderIdAndTenantIdOrderByCreatedAtAsc(order.getId(), order.getTenantId())
                .stream()
                .map(this::toHistoryResponse)
                .toList();
        return toResponse(order, menuNames, history);
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, UserPrincipal principal) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        validateRestaurant(tenantId, request.restaurantId());

        RestaurantOrder order = RestaurantOrder.builder()
                .tenantId(tenantId)
                .restaurantId(request.restaurantId())
                .orderNumber(generateOrderNumber(tenantId, request.restaurantId()))
                .tableId(request.tableId())
                .reservationId(request.reservationId())
                .customerName(request.customerName())
                .orderType(request.orderType() != null ? request.orderType() : OrderType.DINE_IN)
                .status(OrderStatus.PENDING)
                .notes(request.notes())
                .createdBy(principal.getId())
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CreateOrderItemRequest itemRequest : request.items()) {
            MenuItem menuItem = findAvailableMenuItem(itemRequest.menuItemId(), tenantId);
            List<ModifierSnapshot> modifiers = toModifierSnapshots(itemRequest.modifiers());
            BigDecimal modifierTotal = modifiers.stream()
                    .map(ModifierSnapshot::priceAdjustment)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal unitPrice = menuItem.getPrice().add(modifierTotal);

            OrderItem orderItem = OrderItem.builder()
                    .tenantId(tenantId)
                    .order(order)
                    .menuItemId(menuItem.getId())
                    .quantity(itemRequest.quantity())
                    .unitPrice(unitPrice)
                    .modifiers(modifiers)
                    .specialInstructions(itemRequest.specialInstructions())
                    .status(OrderItemStatus.PENDING)
                    .build();
            order.getItems().add(orderItem);
            subtotal = subtotal.add(unitPrice.multiply(BigDecimal.valueOf(itemRequest.quantity())));
        }

        order.setSubtotal(subtotal);
        order.setTaxAmount(subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP));
        order.setTipAmount(BigDecimal.ZERO);
        order.setTotalAmount(order.getSubtotal().add(order.getTaxAmount()).add(order.getTipAmount()));

        order = orderRepository.save(order);
        recordStatusChange(order, null, OrderStatus.PENDING, principal.getId(), "Order created");
        auditLogService.log(AuditAction.CREATE, ENTITY_TYPE, order.getId(), null, orderSnapshot(order), principal.getId());

        Map<UUID, String> menuNames = loadMenuItemNames(List.of(order));
        return toResponse(order, menuNames, List.of());
    }

    @Transactional
    public OrderResponse updateOrderStatus(UUID id, UpdateOrderStatusRequest request, UserPrincipal principal) {
        RestaurantOrder order = findOrderOrThrow(id);
        Map<String, Object> before = orderSnapshot(order);
        transitionStatus(order, request.status());
        if (request.status() == OrderStatus.COMPLETED) {
            order.setCompletedAt(Instant.now());
        }
        order = orderRepository.save(order);
        recordStatusChange(order, beforeStatus(before), request.status(), principal.getId(), request.notes());
        auditLogService.log(
                AuditAction.UPDATE, ENTITY_TYPE, order.getId(), before, orderSnapshot(order), principal.getId());
        Map<UUID, String> menuNames = loadMenuItemNames(List.of(order));
        return toResponse(order, menuNames, List.of());
    }

    @Transactional
    public OrderItemResponse updateOrderItemStatus(
            UUID orderId, UUID itemId, UpdateOrderItemStatusRequest request, UserPrincipal principal) {
        RestaurantOrder order = findOrderOrThrow(orderId);
        OrderItem item = orderItemRepository
                .findByIdAndTenantIdAndOrder_Id(itemId, order.getTenantId(), orderId)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "Order item not found"));

        Set<OrderItemStatus> allowed = ITEM_STATUS_TRANSITIONS.getOrDefault(item.getStatus(), Set.of());
        if (!allowed.contains(request.status())) {
            throw new BusinessException(
                    "VALIDATION_ERROR",
                    "Cannot transition item from " + item.getStatus() + " to " + request.status());
        }
        item.setStatus(request.status());
        orderItemRepository.save(item);

        if (request.status() == OrderItemStatus.PREPARING && order.getStatus() == OrderStatus.CONFIRMED) {
            transitionStatus(order, OrderStatus.PREPARING);
            orderRepository.save(order);
            recordStatusChange(order, OrderStatus.CONFIRMED, OrderStatus.PREPARING, principal.getId(), "Kitchen started item");
        }

        MenuItem menuItem = menuItemRepository
                .findByIdAndTenantIdAndDeletedAtIsNull(item.getMenuItemId(), order.getTenantId())
                .orElse(null);
        return toItemResponse(item, menuItem != null ? menuItem.getName() : null);
    }

    @Transactional(readOnly = true)
    public long countTodayOrders(UUID restaurantId) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        validateRestaurant(tenantId, restaurantId);
        Instant start = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = LocalDate.now().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        return orderRepository.countByTenantIdAndRestaurantIdAndCreatedAtBetweenAndStatusNotIn(
                tenantId, restaurantId, start, end, List.of(OrderStatus.CANCELLED));
    }

    @Transactional(readOnly = true)
    public BigDecimal sumTodayRevenue(UUID restaurantId) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        validateRestaurant(tenantId, restaurantId);
        Instant start = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = LocalDate.now().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        return orderRepository.sumCompletedRevenue(tenantId, restaurantId, start, end);
    }

    @Transactional(readOnly = true)
    public List<RecentOrderResponse> getRecentOrders(UUID restaurantId, int limit) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        validateRestaurant(tenantId, restaurantId);
        return orderRepository
                .findByTenantIdAndRestaurantIdOrderByCreatedAtDesc(
                        tenantId, restaurantId, PageRequest.of(0, limit))
                .stream()
                .map(order -> new RecentOrderResponse(
                        order.getId(),
                        order.getOrderNumber(),
                        order.getCustomerName(),
                        order.getStatus().name(),
                        order.getTotalAmount(),
                        order.getCreatedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BigDecimal> getDailyRevenue(UUID restaurantId, int days) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        validateRestaurant(tenantId, restaurantId);
        List<BigDecimal> values = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            Instant start = date.atStartOfDay(ZoneOffset.UTC).toInstant();
            Instant end = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
            values.add(orderRepository.sumCompletedRevenue(tenantId, restaurantId, start, end));
        }
        return values;
    }

    @Transactional(readOnly = true)
    public List<String> getDailyRevenueLabels(int days) {
        List<String> labels = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            labels.add(date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
        }
        return labels;
    }

    @Transactional(readOnly = true)
    public BigDecimal averageOrderValue(UUID restaurantId) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        validateRestaurant(tenantId, restaurantId);
        Instant start = LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = LocalDate.now().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        long count = orderRepository.countByTenantIdAndRestaurantIdAndCreatedAtBetweenAndStatusNotIn(
                tenantId, restaurantId, start, end, List.of(OrderStatus.CANCELLED));
        if (count == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal revenue = orderRepository.sumCompletedRevenue(tenantId, restaurantId, start, end);
        return revenue.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
    }

    private String generateOrderNumber(UUID tenantId, UUID restaurantId) {
        LocalDate today = LocalDate.now();
        Instant start = today.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = today.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        long count = orderRepository.countByTenantIdAndRestaurantIdAndCreatedAtBetween(tenantId, restaurantId, start, end);
        return "ORD-" + today.format(DateTimeFormatter.BASIC_ISO_DATE) + "-" + String.format("%04d", count + 1);
    }

    private MenuItem findAvailableMenuItem(UUID menuItemId, UUID tenantId) {
        MenuItem item = menuItemRepository
                .findByIdAndTenantIdAndDeletedAtIsNull(menuItemId, tenantId)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "Menu item not found"));
        if (!item.isAvailable()) {
            throw new BusinessException("VALIDATION_ERROR", "Menu item is not available: " + item.getName());
        }
        return item;
    }

    private List<ModifierSnapshot> toModifierSnapshots(List<OrderItemModifierRequest> modifiers) {
        if (modifiers == null) {
            return List.of();
        }
        return modifiers.stream()
                .map(m -> new ModifierSnapshot(m.name(), m.priceAdjustment()))
                .toList();
    }

    private void transitionStatus(RestaurantOrder order, OrderStatus newStatus) {
        if (newStatus == OrderStatus.CANCELLED) {
            if (!ACTIVE_ORDER_STATUSES.contains(order.getStatus())) {
                throw new BusinessException("VALIDATION_ERROR", "Order cannot be cancelled in status " + order.getStatus());
            }
            order.setStatus(OrderStatus.CANCELLED);
            return;
        }
        Set<OrderStatus> allowed = STATUS_TRANSITIONS.getOrDefault(order.getStatus(), Set.of());
        if (!allowed.contains(newStatus)) {
            throw new BusinessException(
                    "VALIDATION_ERROR", "Cannot transition from " + order.getStatus() + " to " + newStatus);
        }
        order.setStatus(newStatus);
    }

    private void recordStatusChange(
            RestaurantOrder order, OrderStatus from, OrderStatus to, UUID userId, String notes) {
        historyRepository.save(OrderStatusHistory.builder()
                .tenantId(order.getTenantId())
                .orderId(order.getId())
                .fromStatus(from)
                .toStatus(to)
                .changedBy(userId)
                .notes(notes)
                .build());
    }

    private RestaurantOrder findOrderOrThrow(UUID id) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        RestaurantOrder order = orderRepository
                .findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "Order not found"));
        order.getItems().size();
        return order;
    }

    private void validateRestaurant(UUID tenantId, UUID restaurantId) {
        restaurantRepository
                .findByIdAndTenantIdAndDeletedAtIsNull(restaurantId, tenantId)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "Restaurant not found"));
    }

    private Map<UUID, String> loadMenuItemNames(List<RestaurantOrder> orders) {
        Set<UUID> menuItemIds = orders.stream()
                .flatMap(order -> order.getItems().stream())
                .map(OrderItem::getMenuItemId)
                .collect(Collectors.toSet());
        if (menuItemIds.isEmpty()) {
            return Map.of();
        }
        UUID tenantId = TenantContextHolder.requireTenantId();
        return menuItemRepository.findAllById(menuItemIds).stream()
                .filter(item -> item.getTenantId().equals(tenantId))
                .collect(Collectors.toMap(MenuItem::getId, MenuItem::getName));
    }

    private Map<String, Object> orderSnapshot(RestaurantOrder order) {
        Map<String, Object> map = new HashMap<>();
        map.put("orderNumber", order.getOrderNumber());
        map.put("status", order.getStatus().name());
        map.put("totalAmount", order.getTotalAmount());
        return map;
    }

    private OrderStatus beforeStatus(Map<String, Object> before) {
        Object status = before.get("status");
        return status != null ? OrderStatus.valueOf(status.toString()) : null;
    }

    private OrderStatusHistoryResponse toHistoryResponse(OrderStatusHistory history) {
        return new OrderStatusHistoryResponse(
                history.getId(),
                history.getFromStatus(),
                history.getToStatus(),
                history.getNotes(),
                history.getCreatedAt());
    }

    private OrderItemResponse toItemResponse(OrderItem item, String menuItemName) {
        List<OrderItemModifierResponse> modifiers = item.getModifiers().stream()
                .map(m -> new OrderItemModifierResponse(m.name(), m.priceAdjustment()))
                .toList();
        return new OrderItemResponse(
                item.getId(),
                item.getMenuItemId(),
                menuItemName,
                item.getQuantity(),
                item.getUnitPrice(),
                modifiers,
                item.getSpecialInstructions(),
                item.getStatus(),
                item.getCreatedAt());
    }

    private OrderResponse toResponse(
            RestaurantOrder order, Map<UUID, String> menuNames, List<OrderStatusHistoryResponse> statusHistory) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> toItemResponse(item, menuNames.get(item.getMenuItemId())))
                .toList();
        return new OrderResponse(
                order.getId(),
                order.getRestaurantId(),
                order.getOrderNumber(),
                order.getTableId(),
                order.getReservationId(),
                order.getCustomerName(),
                order.getOrderType(),
                order.getStatus(),
                order.getSubtotal(),
                order.getTaxAmount(),
                order.getTipAmount(),
                order.getTotalAmount(),
                order.getNotes(),
                items,
                statusHistory,
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getCompletedAt(),
                order.getVersion());
    }
}
