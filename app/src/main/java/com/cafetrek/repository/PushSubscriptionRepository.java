package com.cafetrek.repository;

import com.cafetrek.domain.PushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {
    List<PushSubscription> findAll();
    List<PushSubscription> findByUserId(Long userId);
    Optional<PushSubscription> findByEndpoint(String endpoint);
}
