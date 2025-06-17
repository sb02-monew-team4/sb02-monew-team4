package com.team4.monew.repository;

import com.team4.monew.entity.Notification;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID>, NotificationRepositoryCustom {
  List<Notification> findByUserIdAndConfirmedFalse(UUID userId);
  long countByUserIdAndConfirmedFalse(UUID userId);
  long deleteByConfirmedTrueAndCreatedAtBefore(Instant cutoffDateTime);
}
