package com.team4.monew.repository;

import com.team4.monew.entity.Notification;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface NotificationRepositoryCustom {
  List<Notification> findUnconfirmedByCursor(LocalDateTime cursor, LocalDateTime after, int limit, UUID userId);
}
