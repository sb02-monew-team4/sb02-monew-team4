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
import com.team4.monew.exception.user.UserNotFoundException;
import com.team4.monew.mapper.NotificationMapper;
import com.team4.monew.repository.NotificationRepository;
import com.team4.monew.repository.UserRepository;
import com.team4.monew.service.basic.BasicNotificationService;
import java.time.Instant;
import java.time.LocalDateTime;
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
  private UserRepository userRepository;

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
    ReflectionTestUtils.setField(user, "createdAt", LocalDateTime.now());

    notification = Notification.create(user, content, resourceId, resourceType);
    ReflectionTestUtils.setField(notification, "id", notificationId);
    ReflectionTestUtils.setField(notification, "createdAt", LocalDateTime.now());
    ReflectionTestUtils.setField(notification, "updatedAt", LocalDateTime.now());
  }

  @Test
  @DisplayName("댓글에 좋아요가 눌리면 알림 생성_성공")
  void createForCommentLike_Success_ShouldReturnCreatedNotification() {
    // given
    UUID commentId = UUID.randomUUID();
    UUID ownerId = UUID.randomUUID();
    UUID likerId = UUID.randomUUID();

    User owner = User.create("email@email.com", "owner", "password1!");
    User liker = User.create("email@email.com", "liker", "password1!");
    ReflectionTestUtils.setField(owner, "id", ownerId);
    ReflectionTestUtils.setField(liker, "id", likerId);

    given(userRepository.findById(ownerId)).willReturn(Optional.of(owner));
    given(userRepository.findById(likerId)).willReturn(Optional.of(liker));

    Notification notification = Notification.create(
        owner,
        "liker님이 나의 댓글을 좋아합니다.",
        commentId,
        ResourceType.COMMENT
    );
    given(notificationRepository.save(any(Notification.class))).willReturn(notification);

    // when
    Notification result = notificationService.createForCommentLike(commentId, ownerId, likerId);

    // then
    assertThat(result.getUser()).isEqualTo(owner);
    assertThat(result.getContent()).isEqualTo("liker님이 나의 댓글을 좋아합니다.");
    assertThat(result.getResourceId()).isEqualTo(commentId);

    then(notificationRepository).should().save(any(Notification.class));
    then(userRepository).should().findById(ownerId);
    then(userRepository).should().findById(likerId);
  }

  @Test
  @DisplayName("댓글에 좋아요가 눌리면 알림 생성_실패_좋아요를 누른 사람이 존재하지 않는 경우")
  void createForCommentLike_Failure_LikerNotFound() {
    // given
    UUID commentId = UUID.randomUUID();
    UUID ownerId = UUID.randomUUID();
    UUID likerId = UUID.randomUUID();

    User owner = User.create("email@email.com", "owner", "password1!");
    ReflectionTestUtils.setField(owner, "id", ownerId);

    given(userRepository.findById(ownerId)).willReturn(Optional.of(owner));
    given(userRepository.findById(likerId)).willReturn(Optional.empty());

    // when & then
    UserNotFoundException exception = assertThrows(UserNotFoundException.class,
        () -> notificationService.createForCommentLike(commentId, likerId, ownerId));

    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    assertThat(exception.getDetails().get("userId")).isEqualTo(likerId);

    then(userRepository).should().findById(ownerId);
    then(userRepository).should().findById(likerId);
    then(notificationRepository).shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("구독 중인 관심사와 관련된 기사가 새로 등록되면 알림 생성_성공")
  void createForNewArticles_Success_ShouldReturnCreatedNotification() {
    // given
    UUID interestId = UUID.randomUUID();
    String interestName = "IT";
    int articleCount = 10;

    given(userRepository.findById(userId)).willReturn(Optional.of(user));

    Notification notification = Notification.create(
        user,
        "IT와(과) 관련된 새로운 기사가 10건 등록되었습니다.",
        interestId,
        ResourceType.INTEREST
    );
    given(notificationRepository.save(any(Notification.class))).willReturn(notification);

    // when
    Notification result = notificationService.createForNewArticles(interestId, interestName, articleCount, userId);

    // then
    assertThat(result.getUser()).isEqualTo(user);
    assertThat(result.getContent()).isEqualTo("IT와(과) 관련된 새로운 기사가 10건 등록되었습니다.");
    assertThat(result.getResourceId()).isEqualTo(interestId);

    then(notificationRepository).should().save(any(Notification.class));
    then(userRepository).should().findById(userId);
  }

  @Test
  @DisplayName("구독 중인 관심사와 관련된 기사가 새로 등록되면 알림 생성_실패_구독자가 존재하지 않는 경우")
  void createForNewArticles_Failure_SubscriberNotFound() {
    // given
    UUID interestId = UUID.randomUUID();
    String interestName = "과학";
    int articleCount = 3;
    UUID subscriberId = UUID.randomUUID();

    given(userRepository.findById(subscriberId)).willReturn(Optional.empty());

    // when & then
    UserNotFoundException exception = assertThrows(UserNotFoundException.class,
        () -> notificationService.createForNewArticles(interestId, interestName, articleCount, subscriberId));

    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    assertThat(exception.getDetails().get("userId")).isEqualTo(subscriberId);

    then(userRepository).should().findById(subscriberId);
    then(notificationRepository).shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("알림 목록 조회_성공")
  void findUnconfirmedByCursor_Success_ShouldReturnCursorPage(){
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime after = now.minusSeconds(300);
    int limit = 2;
    String cursorString = null;
    LocalDateTime cursorLocalDateTime = null;

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

    given(notificationRepository.findUnconfirmedByCursor(cursorLocalDateTime, after, limit + 1, userId))
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

    then(notificationRepository).should().findUnconfirmedByCursor(cursorLocalDateTime, after, limit + 1, userId);
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
