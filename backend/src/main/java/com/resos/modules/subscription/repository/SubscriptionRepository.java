package com.resos.modules.subscription.repository;

import com.resos.modules.subscription.domain.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
}
