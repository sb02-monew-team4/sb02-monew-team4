package com.team4.monew.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.team4.monew.dto.notifications.CursorPageResponseNotificationDto;
import com.team4.monew.dto.notifications.NotificationDto;
import com.team4.monew.entity.Notification;
import com.team4.monew.entity.ResourceType;
import com.team4.monew.entity.User;
import com.team4.monew.exception.ErrorCode;
import com.team4.monew.exception.notification.NotificationNotFoundException;
import com.team4.monew.mapper.NotificationMapper;
import com.team4.monew.repository.NotificationRepository;
import com.team4.monew.service.basic.BasicNotificationService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

  @InjectMocks
  private BasicNotificationService notificationService;

  @Mock
  private NotificationRepository notificationRepository;

  @Mock
  private NotificationMapper notificationMapper;

  @Captor
  private ArgumentCaptor<Instant> instantCaptor;

  private UUID notificationId;
  private UUID userId;

  private User user;
  private Notification notification;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    notificationId = UUID.randomUUID();
    String content = "ㅇㅇ님이 나의 댓글을 좋아합니다.";
    UUID resourceId = UUID.randomUUID();
    ResourceType resourceType = ResourceType.COMMENT;

    user = User.create("test@example.com", "testUser", "password123");
    ReflectionTestUtils.setField(user, "id", userId);
    ReflectionTestUtils.setField(user, "createdAt", Instant.now());

    notification = Notification.create(user, content, resourceId, resourceType);
    ReflectionTestUtils.setField(notification, "id", notificationId);
    ReflectionTestUtils.setField(notification, "createdAt", Instant.now());
    ReflectionTestUtils.setField(notification, "updatedAt", Instant.now());
  }

  @Test
  @DisplayName("알림 목록 조회_성공")
  void findUnconfirmedByCursor_Success_ShouldReturnCursorPage(){
    Instant now = Instant.now();
    Instant after = now.minusSeconds(300);
    int limit = 2;
    String cursorString = null;
    Instant cursorInstant = null;

    Notification n1 = Notification.builder()
        .id(UUID.randomUUID())
        .user(user)
        .confirmed(false)
        .createdAt(now.minusSeconds(60))
        .updatedAt(now.minusSeconds(30))
        .content("알림 1")
        .resourceType(ResourceType.COMMENT)
        .resourceId(UUID.randomUUID())
        .build();

    Notification n2 = Notification.builder()
        .id(UUID.randomUUID())
        .user(user)
        .confirmed(false)
        .createdAt(now.minusSeconds(120))
        .updatedAt(now.minusSeconds(90))
        .content("알림 2")
        .resourceType(ResourceType.COMMENT)
        .resourceId(UUID.randomUUID())
        .build();

    Notification n3 = Notification.builder()
        .id(UUID.randomUUID())
        .user(user)
        .confirmed(false)
        .createdAt(now.minusSeconds(180))
        .updatedAt(now.minusSeconds(150))
        .content("알림 3")
        .resourceType(ResourceType.COMMENT)
        .resourceId(UUID.randomUUID())
        .build();

    List<Notification> entityList = List.of(n1, n2, n3);
    List<NotificationDto> dtoList = List.of(n1, n2).stream()
        .map(n -> new NotificationDto(
            n.getId(),
            n.getCreatedAt(),
            n.getUpdatedAt(),
            n.isConfirmed(),
            n.getUser().getId(),
            n.getContent(),
            n.getResourceType(),
            n.getResourceId()
        )).toList();

    given(notificationRepository.findUnconfirmedByCursor(cursorInstant, after, limit + 1, userId))
        .willReturn(entityList);

    given(notificationMapper.toDtoList(List.of(n1, n2)))
        .willReturn(dtoList);

    given(notificationRepository.countByUserIdAndConfirmedFalse(userId))
        .willReturn(10L);

    // when
    CursorPageResponseNotificationDto result = notificationService.findUnconfirmedByCursor(
        cursorString, after, limit, userId
    );

    // then
    assertThat(result.content()).hasSize(2);
    assertThat(result.hasNext()).isTrue();
    assertThat(result.size()).isEqualTo(limit);
    assertThat(result.totalElements()).isEqualTo(10L);
    assertThat(result.nextAfter()).isEqualTo(n2.getCreatedAt());
    assertThat(result.nextCursor()).isEqualTo(n2.getCreatedAt().toString());

    then(notificationRepository).should().findUnconfirmedByCursor(cursorInstant, after, limit + 1, userId);
    then(notificationMapper).should().toDtoList(List.of(n1, n2));
    then(notificationRepository).should().countByUserIdAndConfirmedFalse(userId);
  }

  @Test
  @DisplayName("전체 알림 확인_성공")
  void updateAll_Success_ShouldReturnUpdatedNotifications() {
    // given
    given(notificationRepository.findByUserIdAndConfirmedFalse(userId)).willReturn(List.of(notification));

    // when
    List<Notification> result = notificationService.updateAll(userId);

    // then
    assertThat(result.get(0).getUser().getId()).isEqualTo(userId);
    assertThat(result.get(0).isConfirmed()).isTrue();

    then(notificationRepository).should().findByUserIdAndConfirmedFalse(userId);
  }

  @Test
  @DisplayName("전체 알림 확인_실패_알림이 존재하지 않는 경우")
  void updateAll_Failure_WhenNotificationsNotFound() {
    // given
    given(notificationRepository.findByUserIdAndConfirmedFalse(userId)).willReturn(List.of());

    // when & then
    NotificationNotFoundException exception = assertThrows(NotificationNotFoundException.class,
        () -> notificationService.updateAll(userId));

    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOTIFICATION_NOT_FOUND);
    assertThat(exception.getDetails().get("userId")).isEqualTo(userId);

    then(notificationRepository).should().findByUserIdAndConfirmedFalse(userId);
  }

  @Test
  @DisplayName("알림 확인_성공")
  void update_Success_ShouldReturnUpdatedNotification() {
    // given
    given(notificationRepository.findById(notificationId)).willReturn(Optional.of(notification));

    // when
    Notification result = notificationService.update(notificationId, userId);

    // then
    assertThat(result.getId()).isEqualTo(notificationId);
    assertThat(result.getUser().getId()).isEqualTo(userId);
    assertThat(result.isConfirmed()).isTrue();

    then(notificationRepository).should().findById(notificationId);
  }

  @Test
  @DisplayName("알림 확인_실패_알림이 존재하지 않는 경우")
  void update_Failure_WhenNotificationNotFound() {
    // given
    given(notificationRepository.findById(notificationId)).willReturn(Optional.empty());

    // when & then
    NotificationNotFoundException exception = assertThrows(NotificationNotFoundException.class,
        () -> notificationService.update(notificationId, userId));

    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOTIFICATION_NOT_FOUND);
    assertThat(exception.getDetails().get("notificationId")).isEqualTo(notificationId);

    then(notificationRepository).should().findById(notificationId);
  }

  @Test
  @DisplayName("알림 확인_실패_해당 알림의 사용자 Id와 userId가 일치하지 않는 경우")
  void update_Failure_WhenNotificationDoesNotBelongToUser() {
    // given
    UUID requestId = UUID.randomUUID();
    given(notificationRepository.findById(notificationId)).willReturn(Optional.of(notification));

    // when & then
    NotificationNotFoundException exception = assertThrows(NotificationNotFoundException.class,
        () -> notificationService.update(notificationId, requestId));

    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOTIFICATION_NOT_FOUND);
    assertThat(exception.getDetails().get("notificationId")).isEqualTo(notificationId);

    then(notificationRepository).should().findById(notificationId);
  }

  @Test
  @DisplayName("확인된 알림 삭제_성공")
  void deleteConfirmedNotificationsOlderThan7Days_Success() {
    // given
    long deleteCount = 3L;

    given(notificationRepository.deleteByConfirmedTrueAndCreatedAtBefore(any())).willReturn(deleteCount);

    // when
    long result = notificationService.deleteConfirmedNotificationsOlderThan7Days();

    // then
    assertThat(result).isEqualTo(deleteCount);
    then(notificationRepository).should().deleteByConfirmedTrueAndCreatedAtBefore(instantCaptor.capture());

    Instant cutoffDateTime = Instant.now().minus(7, ChronoUnit.DAYS);
    assertThat(instantCaptor.getValue()).isBeforeOrEqualTo(cutoffDateTime);
  }


}
