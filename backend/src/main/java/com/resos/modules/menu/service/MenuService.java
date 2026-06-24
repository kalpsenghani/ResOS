package com.resos.modules.menu.service;

import com.resos.modules.audit.domain.AuditAction;
import com.resos.modules.audit.service.AuditLogService;
import com.resos.modules.menu.domain.MenuCategory;
import com.resos.modules.menu.domain.MenuItem;
import com.resos.modules.menu.domain.MenuItemModifier;
import com.resos.modules.menu.dto.*;
import com.resos.modules.menu.repository.MenuCategoryRepository;
import com.resos.modules.menu.repository.MenuItemRepository;
import com.resos.modules.menu.repository.MenuItemSpecifications;
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

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MenuService {

    private static final String CATEGORY_ENTITY = "MenuCategory";
    private static final String ITEM_ENTITY = "MenuItem";

    private final MenuCategoryRepository categoryRepository;
    private final MenuItemRepository itemRepository;
    private final RestaurantRepository restaurantRepository;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories(UUID restaurantId) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        validateRestaurant(tenantId, restaurantId);
        return categoryRepository
                .findByTenantIdAndRestaurantIdAndDeletedAtIsNullOrderBySortOrderAscNameAsc(tenantId, restaurantId)
                .stream()
                .map(this::toCategoryResponse)
                .toList();
    }

    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request, UserPrincipal principal) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        validateRestaurant(tenantId, request.restaurantId());

        MenuCategory category = MenuCategory.builder()
                .tenantId(tenantId)
                .restaurantId(request.restaurantId())
                .name(request.name())
                .description(request.description())
                .sortOrder(request.sortOrder() != null ? request.sortOrder() : 0)
                .active(true)
                .build();

        category = categoryRepository.save(category);
        auditLogService.log(
                AuditAction.CREATE, CATEGORY_ENTITY, category.getId(), null, categorySnapshot(category), principal.getId());
        return toCategoryResponse(category);
    }

    @Transactional
    public CategoryResponse updateCategory(UUID id, UpdateCategoryRequest request, UserPrincipal principal) {
        MenuCategory category = findCategoryOrThrow(id);
        Map<String, Object> before = categorySnapshot(category);

        if (request.name() != null) category.setName(request.name());
        if (request.description() != null) category.setDescription(request.description());
        if (request.sortOrder() != null) category.setSortOrder(request.sortOrder());
        if (request.active() != null) category.setActive(request.active());

        category = categoryRepository.save(category);
        auditLogService.log(
                AuditAction.UPDATE, CATEGORY_ENTITY, category.getId(), before, categorySnapshot(category), principal.getId());
        return toCategoryResponse(category);
    }

    @Transactional
    public void deleteCategory(UUID id, UserPrincipal principal) {
        MenuCategory category = findCategoryOrThrow(id);
        auditLogService.log(
                AuditAction.DELETE, CATEGORY_ENTITY, category.getId(), categorySnapshot(category), null, principal.getId());
        category.setDeletedAt(Instant.now());
        category.setActive(false);
        categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<MenuItemResponse>> listItems(
            UUID restaurantId, UUID categoryId, Boolean available, String search, Pageable pageable) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        Specification<MenuItem> spec = MenuItemSpecifications.forTenant(tenantId);

        if (categoryId != null) {
            findCategoryOrThrow(categoryId);
            spec = spec.and(MenuItemSpecifications.forCategory(categoryId));
        } else if (restaurantId != null) {
            validateRestaurant(tenantId, restaurantId);
            Set<UUID> categoryIds = categoryRepository
                    .findByTenantIdAndRestaurantIdAndDeletedAtIsNullOrderBySortOrderAscNameAsc(tenantId, restaurantId)
                    .stream()
                    .map(MenuCategory::getId)
                    .collect(java.util.stream.Collectors.toSet());
            if (categoryIds.isEmpty()) {
                return ApiResponse.page(List.of(), new ApiResponse.PageMeta(0, pageable.getPageSize(), 0, 0));
            }
            spec = spec.and(MenuItemSpecifications.forCategories(categoryIds));
        }

        if (available != null) {
            spec = spec.and(MenuItemSpecifications.available(available));
        }
        if (search != null && !search.isBlank()) {
            spec = spec.and(MenuItemSpecifications.search(search.trim()));
        }

        Page<MenuItem> page = itemRepository.findAll(spec, pageable);
        List<MenuItemResponse> data = page.getContent().stream().map(this::toItemResponse).toList();
        return ApiResponse.page(data, new ApiResponse.PageMeta(
                page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages()));
    }

    @Transactional(readOnly = true)
    public MenuItemResponse getItem(UUID id) {
        return toItemResponse(findItemOrThrow(id));
    }

    @Transactional
    public MenuItemResponse createItem(CreateMenuItemRequest request, UserPrincipal principal) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        MenuCategory category = findCategoryOrThrow(request.categoryId());

        MenuItem item = MenuItem.builder()
                .tenantId(tenantId)
                .categoryId(category.getId())
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .cost(request.cost())
                .imageUrl(request.imageUrl())
                .preparationTime(request.preparationTime())
                .allergens(request.allergens() != null ? new ArrayList<>(request.allergens()) : new ArrayList<>())
                .sortOrder(request.sortOrder() != null ? request.sortOrder() : 0)
                .available(true)
                .build();

        applyModifiers(item, request.modifiers(), tenantId);
        item = itemRepository.save(item);
        auditLogService.log(AuditAction.CREATE, ITEM_ENTITY, item.getId(), null, itemSnapshot(item), principal.getId());
        return toItemResponse(item);
    }

    @Transactional
    public MenuItemResponse updateItem(
            UUID id, UpdateMenuItemRequest request, Integer expectedVersion, UserPrincipal principal) {
        MenuItem item = findItemOrThrow(id);
        checkVersion(item, expectedVersion);
        Map<String, Object> before = itemSnapshot(item);

        if (request.categoryId() != null) {
            findCategoryOrThrow(request.categoryId());
            item.setCategoryId(request.categoryId());
        }
        if (request.name() != null) item.setName(request.name());
        if (request.description() != null) item.setDescription(request.description());
        if (request.price() != null) item.setPrice(request.price());
        if (request.cost() != null) item.setCost(request.cost());
        if (request.imageUrl() != null) item.setImageUrl(request.imageUrl());
        if (request.preparationTime() != null) item.setPreparationTime(request.preparationTime());
        if (request.allergens() != null) item.setAllergens(new ArrayList<>(request.allergens()));
        if (request.sortOrder() != null) item.setSortOrder(request.sortOrder());
        if (request.available() != null) item.setAvailable(request.available());
        if (request.modifiers() != null) {
            item.getModifiers().clear();
            applyModifiers(item, request.modifiers(), item.getTenantId());
        }

        item = itemRepository.save(item);
        auditLogService.log(AuditAction.UPDATE, ITEM_ENTITY, item.getId(), before, itemSnapshot(item), principal.getId());
        return toItemResponse(item);
    }

    @Transactional
    public MenuItemResponse updateAvailability(UUID id, UpdateAvailabilityRequest request, UserPrincipal principal) {
        MenuItem item = findItemOrThrow(id);
        Map<String, Object> before = itemSnapshot(item);
        item.setAvailable(request.isAvailable());
        item = itemRepository.save(item);
        auditLogService.log(AuditAction.UPDATE, ITEM_ENTITY, item.getId(), before, itemSnapshot(item), principal.getId());
        return toItemResponse(item);
    }

    @Transactional
    public void deleteItem(UUID id, UserPrincipal principal) {
        MenuItem item = findItemOrThrow(id);
        auditLogService.log(AuditAction.DELETE, ITEM_ENTITY, item.getId(), itemSnapshot(item), null, principal.getId());
        item.setDeletedAt(Instant.now());
        item.setAvailable(false);
        itemRepository.save(item);
    }

    private void applyModifiers(MenuItem item, List<ModifierRequest> modifiers, UUID tenantId) {
        if (modifiers == null) return;
        for (ModifierRequest modifier : modifiers) {
            MenuItemModifier entity = MenuItemModifier.builder()
                    .tenantId(tenantId)
                    .menuItem(item)
                    .name(modifier.name())
                    .priceAdjustment(modifier.priceAdjustment())
                    .required(modifier.required() != null && modifier.required())
                    .build();
            item.getModifiers().add(entity);
        }
    }

    private MenuCategory findCategoryOrThrow(UUID id) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        return categoryRepository
                .findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "Menu category not found"));
    }

    private MenuItem findItemOrThrow(UUID id) {
        UUID tenantId = TenantContextHolder.requireTenantId();
        MenuItem item = itemRepository
                .findByIdAndTenantIdAndDeletedAtIsNull(id, tenantId)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "Menu item not found"));
        item.getModifiers().size();
        return item;
    }

    private void validateRestaurant(UUID tenantId, UUID restaurantId) {
        restaurantRepository
                .findByIdAndTenantIdAndDeletedAtIsNull(restaurantId, tenantId)
                .orElseThrow(() -> new BusinessException("NOT_FOUND", "Restaurant not found"));
    }

    private void checkVersion(MenuItem item, Integer expectedVersion) {
        if (expectedVersion != null && item.getVersion() != expectedVersion) {
            throw new BusinessException("OPTIMISTIC_LOCK", "Menu item was modified by another user");
        }
    }

    private Map<String, Object> categorySnapshot(MenuCategory category) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", category.getName());
        map.put("sortOrder", category.getSortOrder());
        map.put("active", category.isActive());
        return map;
    }

    private Map<String, Object> itemSnapshot(MenuItem item) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", item.getName());
        map.put("price", item.getPrice());
        map.put("available", item.isAvailable());
        return map;
    }

    private CategoryResponse toCategoryResponse(MenuCategory category) {
        return new CategoryResponse(
                category.getId(),
                category.getRestaurantId(),
                category.getName(),
                category.getDescription(),
                category.getSortOrder(),
                category.isActive(),
                category.getCreatedAt(),
                category.getUpdatedAt());
    }

    private MenuItemResponse toItemResponse(MenuItem item) {
        List<ModifierResponse> modifiers = item.getModifiers().stream()
                .map(m -> new ModifierResponse(m.getId(), m.getName(), m.getPriceAdjustment(), m.isRequired()))
                .toList();
        return new MenuItemResponse(
                item.getId(),
                item.getCategoryId(),
                item.getName(),
                item.getDescription(),
                item.getPrice(),
                item.getCost(),
                item.getImageUrl(),
                item.isAvailable(),
                item.getPreparationTime(),
                item.getAllergens(),
                item.getSortOrder(),
                modifiers,
                item.getCreatedAt(),
                item.getUpdatedAt(),
                item.getVersion());
    }
}
