package com.team4.monew.service;

import com.team4.monew.dto.notifications.CursorPageResponseNotificationDto;
import com.team4.monew.entity.Notification;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface NotificationService {
  Notification createForCommentLike(UUID commentId, UUID likerId, UUID commentOwnerId);
  Notification createForNewArticles(UUID interestId, String interestName, int articleCount, UUID subscriberId);
  CursorPageResponseNotificationDto findUnconfirmedByCursor(String cursor, Instant after, int limit, UUID userId);
  List<Notification> updateAll(UUID userId);
  Notification update(UUID notificationId, UUID userId);
  long deleteConfirmedNotificationsOlderThan7Days();
}
