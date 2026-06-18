package com.resos.modules.subscription.repository;

import com.resos.modules.subscription.domain.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID> {

    Optional<SubscriptionPlan> findBySlugAndActiveTrue(String slug);
}
