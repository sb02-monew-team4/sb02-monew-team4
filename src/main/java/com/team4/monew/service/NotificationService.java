package com.team4.monew.service;

import com.team4.monew.dto.notifications.CursorPageResponseNotificationDto;
import com.team4.monew.entity.Notification;
import com.team4.monew.entity.ResourceType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface NotificationService {
  CursorPageResponseNotificationDto findUnconfirmedByCursor(String cursor, Instant after, int limit, UUID userId);
//  Notification create(String keyword, int registerCnt, ResourceType resourceType, UUID resourceId, UUID userId);
  List<Notification> updateAll(UUID userId);
  Notification update(UUID notificationId, UUID userId);
  long deleteConfirmedNotificationsOlderThan7Days();
}
