package com.resos.modules.order.service;

import com.resos.modules.audit.service.AuditLogService;
import com.resos.modules.menu.domain.MenuItem;
import com.resos.modules.menu.repository.MenuItemRepository;
import com.resos.modules.order.domain.OrderStatus;
import com.resos.modules.order.domain.RestaurantOrder;
import com.resos.modules.order.dto.CreateOrderItemRequest;
import com.resos.modules.order.dto.CreateOrderRequest;
import com.resos.modules.order.dto.UpdateOrderStatusRequest;
import com.resos.modules.order.repository.OrderItemRepository;
import com.resos.modules.order.repository.OrderStatusHistoryRepository;
import com.resos.modules.order.repository.RestaurantOrderRepository;
import com.resos.modules.restaurant.repository.RestaurantRepository;
import com.resos.shared.security.UserPrincipal;
import com.resos.shared.tenant.TenantContext;
import com.resos.shared.tenant.TenantContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceTest {

    @Mock
    private RestaurantOrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private OrderStatusHistoryRepository historyRepository;
    @Mock
    private MenuItemRepository menuItemRepository;
    @Mock
    private RestaurantRepository restaurantRepository;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private UserPrincipal principal;

    @InjectMocks
    private OrderService orderService;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID restaurantId = UUID.randomUUID();
    private final UUID menuItemId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TenantContextHolder.set(new TenantContext(tenantId, false));
        when(principal.getId()).thenReturn(userId);
        when(restaurantRepository.findByIdAndTenantIdAndDeletedAtIsNull(restaurantId, tenantId))
                .thenReturn(Optional.of(com.resos.modules.restaurant.domain.Restaurant.builder()
                        .id(restaurantId)
                        .tenantId(tenantId)
                        .build()));
        when(orderRepository.countByTenantIdAndRestaurantIdAndCreatedAtBetween(any(), any(), any(), any()))
                .thenReturn(0L);
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void createOrderCalculatesTotals() {
        when(menuItemRepository.findByIdAndTenantIdAndDeletedAtIsNull(menuItemId, tenantId))
                .thenReturn(Optional.of(MenuItem.builder()
                        .id(menuItemId)
                        .tenantId(tenantId)
                        .name("Burger")
                        .price(new BigDecimal("10.00"))
                        .available(true)
                        .build()));
        when(orderRepository.save(any())).thenAnswer(invocation -> {
            RestaurantOrder order = invocation.getArgument(0);
            order.setId(orderId);
            return order;
        });
        when(historyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var request = new CreateOrderRequest(
                restaurantId,
                null,
                null,
                "Table 5",
                null,
                List.of(new CreateOrderItemRequest(menuItemId, 2, List.of(), null)),
                null);

        var response = orderService.createOrder(request, principal);

        assertThat(response.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.subtotal()).isEqualByComparingTo("20.00");
        assertThat(response.totalAmount()).isGreaterThan(response.subtotal());
    }

    @Test
    void updateOrderStatusTransitionsToConfirmed() {
        RestaurantOrder order = RestaurantOrder.builder()
                .id(orderId)
                .tenantId(tenantId)
                .restaurantId(restaurantId)
                .orderNumber("ORD-1")
                .status(OrderStatus.PENDING)
                .subtotal(BigDecimal.TEN)
                .taxAmount(BigDecimal.ONE)
                .tipAmount(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("11.00"))
                .build();

        when(orderRepository.findByIdAndTenantId(orderId, tenantId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(historyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = orderService.updateOrderStatus(
                orderId, new UpdateOrderStatusRequest(OrderStatus.CONFIRMED, "Accepted"), principal);

        assertThat(response.status()).isEqualTo(OrderStatus.CONFIRMED);
    }
}
