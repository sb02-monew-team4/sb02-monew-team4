package com.team4.monew.eventHandler;

import static org.mockito.Mockito.verify;

import com.team4.monew.asynchronous.event.article.ArticleCreatedEventForNotification;
import com.team4.monew.asynchronous.event.commentlike.CommentLikeCreatedEventForNotification;
import com.team4.monew.asynchronous.handler.NotificationEventHandler;
import com.team4.monew.service.basic.BasicNotificationService;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class NotificationEventHandlerTest {

  @Mock
  private BasicNotificationService notificationService;

  @InjectMocks
  private NotificationEventHandler eventHandler;

  @Test
  @DisplayName("댓글 좋아요 이벤트가 발생하면 알림 생성 서비스 호출")
  void handleCommentLikeCreated_ShouldCallService() {
    // given
    UUID commentId = UUID.randomUUID();
    UUID likerId = UUID.randomUUID();
    UUID ownerId = UUID.randomUUID();
    CommentLikeCreatedEventForNotification event = new CommentLikeCreatedEventForNotification(commentId, likerId, ownerId);

    // when
    eventHandler.handleCommentLikeCreated(event);

    // then
    verify(notificationService).createForCommentLike(commentId, likerId, ownerId);
  }

  @Test
  @DisplayName("기사 생성 이벤트가 발생하면 알림 생성 서비스 호출")
  void handleArticleCreated_ShouldCallService() {
    // given
    UUID interestId = UUID.randomUUID();
    String interestName = "테크";
    int count = 3;
    UUID subscriberId = UUID.randomUUID();
    ArticleCreatedEventForNotification event = new ArticleCreatedEventForNotification(interestId, interestName, count, subscriberId);

    // when
    eventHandler.handleArticleCreated(event);

    // then
    verify(notificationService).createForNewArticles(interestId, interestName, count, subscriberId);
  }

}
