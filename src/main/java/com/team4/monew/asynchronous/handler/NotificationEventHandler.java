package com.team4.monew.asynchronous.handler;

import com.team4.monew.asynchronous.event.article.ArticleCreatedEventForNotification;
import com.team4.monew.asynchronous.event.commentlike.CommentLikeCreatedEventForNotification;
import com.team4.monew.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationEventHandler {

  private final NotificationService notificationService;

  @Async
  @TransactionalEventListener
  public void handleCommentLikeCreated(CommentLikeCreatedEventForNotification event) {
    notificationService.createForCommentLike(event.commentId(), event.likerId(), event.commentOwnerId());
  }

  @Async
  @TransactionalEventListener
  public void handleArticleCreated(ArticleCreatedEventForNotification event) {
    notificationService.createForNewArticles(event.interestId(), event.interestName(), event.articleCount(), event.subscriberId());
  }

}
