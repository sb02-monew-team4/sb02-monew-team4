package com.team4.monew.service.basic;

import com.team4.monew.dto.notifications.CursorPageResponseNotificationDto;
import com.team4.monew.dto.notifications.NotificationDto;
import com.team4.monew.entity.Notification;
import com.team4.monew.exception.notification.NotificationNotFoundException;
import com.team4.monew.mapper.NotificationMapper;
import com.team4.monew.repository.NotificationRepository;
import com.team4.monew.service.NotificationService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
public class BasicNotificationService implements NotificationService {

  private final NotificationRepository notificationRepository;
  private final NotificationMapper notificationMapper;

  @Override
  public CursorPageResponseNotificationDto findUnconfirmedByCursor(
      String cursor,
      Instant after,
      int limit,
      UUID userId
  ) {
    Instant cursorInstant = (cursor != null && !cursor.isBlank()) ? Instant.parse(cursor) : null;

    // hasNext 판별 위해 limit+1 개로 조회
    List<Notification> notifications = notificationRepository
        .findUnconfirmedByCursor(cursorInstant, after, limit + 1, userId);

    boolean hasNext = notifications.size() > limit;

    List<Notification> limitedNotifications = hasNext ? notifications.subList(0, limit) : notifications;

    List<NotificationDto> contents = notificationMapper.toDtoList(limitedNotifications);

    String nextCursor = null;
    Instant nextAfter = null;
    if (!contents.isEmpty()) {
      Instant lastCreatedAt = contents.get(contents.size() - 1).createdAt();
      nextCursor = lastCreatedAt.toString();
      nextAfter = lastCreatedAt;
    }

    long totalElements = notificationRepository.countByUserIdAndConfirmedFalse(userId);

    return new CursorPageResponseNotificationDto(
        contents,
        nextCursor,
        nextAfter,
        limit,
        totalElements,
        hasNext
    );
  }

  @Override
  public List<Notification> updateAll(UUID userId) {
    List<Notification> notifications = notificationRepository.findByUserIdAndConfirmedFalse(userId);
    if (notifications.isEmpty()) {
      throw NotificationNotFoundException.byUserId(userId);
    }
    notifications.forEach(Notification::updateConfirmed);

    return notifications;
  }

  @Override
  public Notification update(UUID notificationId, UUID userId) {
    Notification notification = notificationRepository.findById(notificationId)
        .orElseThrow(() -> NotificationNotFoundException.byId(notificationId));
    if(!notification.getUser().getId().equals(userId)) {
      throw NotificationNotFoundException.byId(notificationId);
    }
    notification.updateConfirmed();

    return notification;
  }


}
