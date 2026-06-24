package com.resos.modules.order.repository;

import com.resos.modules.order.domain.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    Optional<OrderItem> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<OrderItem> findByIdAndTenantIdAndOrder_Id(UUID id, UUID tenantId, UUID orderId);
}
