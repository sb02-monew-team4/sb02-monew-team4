package com.team4.monew.service.basic;

import com.team4.monew.dto.notifications.CursorPageResponseNotificationDto;
import com.team4.monew.dto.notifications.NotificationDto;
import com.team4.monew.entity.Notification;
import com.team4.monew.entity.ResourceType;
import com.team4.monew.entity.User;
import com.team4.monew.exception.notification.NotificationNotFoundException;
import com.team4.monew.exception.user.UserNotFoundException;
import com.team4.monew.mapper.NotificationMapper;
import com.team4.monew.repository.NotificationRepository;
import com.team4.monew.repository.UserRepository;
import com.team4.monew.service.NotificationService;
import com.team4.monew.util.DateTimeUtils;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class BasicNotificationService implements NotificationService {

  private final NotificationRepository notificationRepository;
  private final NotificationMapper notificationMapper;
  private final UserRepository userRepository;

  @Override
  public Notification createForCommentLike(UUID commentId, UUID likerId, UUID commentOwnerId) {
    // 알림을 받을 사람 (댓글 작성자)
    User owner = userRepository.findById(commentOwnerId)
        .orElseThrow(() -> UserNotFoundException.byId(commentOwnerId));

    // 좋아요를 누른 사람
    User liker = userRepository.findById(likerId)
        .orElseThrow(() -> UserNotFoundException.byId(likerId));

    String content = liker.getNickname() + "님이 나의 댓글을 좋아합니다.";

    return createAndSaveNotification(owner, content, commentId, ResourceType.COMMENT);
  }

  @Override
  public Notification createForNewArticles(UUID interestId, String interestName, int articleCount,
      UUID subscriberId) {
    // 알림을 받을 사람 (관심사 구독자)
    User owner = userRepository.findById(subscriberId)
        .orElseThrow(() -> UserNotFoundException.byId(subscriberId));

    String content = interestName + "와(과) 관련된 새로운 기사가 " + articleCount + "건 등록되었습니다.";

    return createAndSaveNotification(owner, content, interestId, ResourceType.INTEREST);
  }

  private Notification createAndSaveNotification(User owner, String content, UUID resourceId, ResourceType resourceType) {
    Notification notification = Notification.create(owner, content, resourceId, resourceType);
    return notificationRepository.save(notification);
  }

  @Override
  @Transactional(readOnly = true)
  public CursorPageResponseNotificationDto findUnconfirmedByCursor(
      String cursor,
      String after,
      int limit,
      UUID userId
  ) {
    LocalDateTime cursorLocalDateTime = DateTimeUtils.parseToLocalDateTime(cursor);
    LocalDateTime afterDateTime = DateTimeUtils.parseToLocalDateTime(after);

    // hasNext 판별 위해 limit+1 개로 조회
    List<Notification> notifications = notificationRepository
        .findUnconfirmedByCursor(cursorLocalDateTime, afterDateTime, limit + 1, userId);

    boolean hasNext = notifications.size() > limit;

    List<Notification> limitedNotifications = hasNext ? notifications.subList(0, limit) : notifications;

    List<NotificationDto> contents = notificationMapper.toDtoList(limitedNotifications);

    String nextCursor = null;
    LocalDateTime nextAfter = null;
    if (!contents.isEmpty()) {
      LocalDateTime lastCreatedAt = contents.get(contents.size() - 1).createdAt();
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

  @Override
  public long deleteConfirmedNotificationsOlderThan7Days() {
    Instant cutoffDateTime = Instant.now().minus(7, ChronoUnit.DAYS);
    return notificationRepository.deleteByConfirmedTrueAndCreatedAtBefore(cutoffDateTime);
  }
}
