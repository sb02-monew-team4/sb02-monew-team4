package com.team4.monew.repository;

import com.team4.monew.entity.Subscription;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
  Optional<Subscription> findByUserIdAndInterestId(UUID userId, UUID interestId);
  List<Subscription> findByUserId(UUID userId);
}


