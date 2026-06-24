package com.resos.modules.order.repository;

import com.resos.modules.order.domain.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, UUID> {

    List<OrderStatusHistory> findByOrderIdAndTenantIdOrderByCreatedAtAsc(UUID orderId, UUID tenantId);
}
