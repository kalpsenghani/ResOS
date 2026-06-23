package com.resos.modules.inventory.service;

import com.resos.modules.audit.service.AuditLogService;
import com.resos.modules.inventory.domain.InventoryItem;
import com.resos.modules.inventory.domain.InventoryTransaction;
import com.resos.modules.inventory.domain.TransactionType;
import com.resos.modules.inventory.dto.CreateInventoryItemRequest;
import com.resos.modules.inventory.dto.CreateTransactionRequest;
import com.resos.modules.inventory.repository.InventoryItemRepository;
import com.resos.modules.inventory.repository.InventoryTransactionRepository;
import com.resos.modules.inventory.repository.StockAlertRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InventoryServiceTest {

    @Mock
    private InventoryItemRepository inventoryItemRepository;
    @Mock
    private InventoryTransactionRepository transactionRepository;
    @Mock
    private StockAlertRepository stockAlertRepository;
    @Mock
    private RestaurantRepository restaurantRepository;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private UserPrincipal principal;

    @InjectMocks
    private InventoryService inventoryService;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID restaurantId = UUID.randomUUID();
    private final UUID itemId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        TenantContextHolder.set(new TenantContext(tenantId, false));
    }

    private void stubPrincipal() {
        when(principal.getId()).thenReturn(userId);
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void createItemFlagsLowStock() {
        stubPrincipal();
        when(restaurantRepository.findByIdAndTenantIdAndDeletedAtIsNull(restaurantId, tenantId))
                .thenReturn(Optional.of(com.resos.modules.restaurant.domain.Restaurant.builder()
                        .id(restaurantId).tenantId(tenantId).build()));
        when(inventoryItemRepository.save(any())).thenAnswer(invocation -> {
            InventoryItem item = invocation.getArgument(0);
            item.setId(itemId);
            return item;
        });
        when(stockAlertRepository.findByTenantIdAndAcknowledgedOrderByCreatedAtDesc(tenantId, false))
                .thenReturn(java.util.List.of());

        var request = new CreateInventoryItemRequest(
                restaurantId, "Tomatoes", "PROD-001", "Produce", "kg",
                new BigDecimal("2.5"), new BigDecimal("5.0"), new BigDecimal("50.0"),
                new BigDecimal("3.50"), "Fresh Farms");

        var response = inventoryService.createItem(request, principal);

        assertThat(response.isLowStock()).isTrue();
        verify(auditLogService).log(any(), any(), any(), any(), any(), eq(userId));
    }

    @Test
    void recordTransactionUpdatesStock() {
        stubPrincipal();
        InventoryItem item = InventoryItem.builder()
                .id(itemId)
                .tenantId(tenantId)
                .restaurantId(restaurantId)
                .name("Tomatoes")
                .unit("kg")
                .currentStock(new BigDecimal("10"))
                .minimumStock(new BigDecimal("5"))
                .build();

        when(inventoryItemRepository.findByIdAndTenantIdAndDeletedAtIsNull(itemId, tenantId))
                .thenReturn(Optional.of(item));
        when(transactionRepository.save(any())).thenAnswer(invocation -> {
            InventoryTransaction transaction = invocation.getArgument(0);
            transaction.setId(UUID.randomUUID());
            return transaction;
        });
        when(inventoryItemRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(stockAlertRepository.findByTenantIdAndAcknowledgedOrderByCreatedAtDesc(tenantId, false))
                .thenReturn(java.util.List.of());

        var request = new CreateTransactionRequest(
                TransactionType.USAGE, new BigDecimal("3"), null, null, "Kitchen prep");

        var response = inventoryService.recordTransaction(itemId, request, principal);

        assertThat(response.quantity()).isEqualByComparingTo("-3");
        assertThat(item.getCurrentStock()).isEqualByComparingTo("7");
    }
}
