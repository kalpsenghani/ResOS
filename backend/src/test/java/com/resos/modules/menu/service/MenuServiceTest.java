package com.resos.modules.menu.service;

import com.resos.modules.audit.service.AuditLogService;
import com.resos.modules.menu.domain.MenuCategory;
import com.resos.modules.menu.domain.MenuItem;
import com.resos.modules.menu.dto.CreateCategoryRequest;
import com.resos.modules.menu.dto.CreateMenuItemRequest;
import com.resos.modules.menu.repository.MenuCategoryRepository;
import com.resos.modules.menu.repository.MenuItemRepository;
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
class MenuServiceTest {

    @Mock
    private MenuCategoryRepository categoryRepository;
    @Mock
    private MenuItemRepository itemRepository;
    @Mock
    private RestaurantRepository restaurantRepository;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private UserPrincipal principal;

    @InjectMocks
    private MenuService menuService;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID restaurantId = UUID.randomUUID();
    private final UUID categoryId = UUID.randomUUID();
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
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void createCategoryPersistsCategory() {
        when(categoryRepository.save(any())).thenAnswer(invocation -> {
            MenuCategory category = invocation.getArgument(0);
            category.setId(categoryId);
            return category;
        });

        var response = menuService.createCategory(
                new CreateCategoryRequest(restaurantId, "Appetizers", "Starters", 1), principal);

        assertThat(response.name()).isEqualTo("Appetizers");
        assertThat(response.restaurantId()).isEqualTo(restaurantId);
    }

    @Test
    void createMenuItemPersistsItemWithPrice() {
        when(categoryRepository.findByIdAndTenantIdAndDeletedAtIsNull(categoryId, tenantId))
                .thenReturn(Optional.of(MenuCategory.builder()
                        .id(categoryId)
                        .tenantId(tenantId)
                        .restaurantId(restaurantId)
                        .name("Mains")
                        .build()));
        when(itemRepository.save(any())).thenAnswer(invocation -> {
            MenuItem item = invocation.getArgument(0);
            item.setId(UUID.randomUUID());
            return item;
        });

        var response = menuService.createItem(
                new CreateMenuItemRequest(
                        categoryId,
                        "Margherita Pizza",
                        "Classic",
                        new BigDecimal("14.99"),
                        new BigDecimal("4.50"),
                        null,
                        15,
                        null,
                        0,
                        null),
                principal);

        assertThat(response.name()).isEqualTo("Margherita Pizza");
        assertThat(response.price()).isEqualByComparingTo("14.99");
    }
}
