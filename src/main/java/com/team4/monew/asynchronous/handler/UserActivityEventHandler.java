package com.team4.monew.asynchronous.handler;

import com.team4.monew.asynchronous.event.articleview.ArticleViewCreatedEvent;
import com.team4.monew.asynchronous.event.articleview.ArticleViewDeletedEvent;
import com.team4.monew.asynchronous.event.comment.CommentCreatedEvent;
import com.team4.monew.asynchronous.event.comment.CommentDeletedEvent;
import com.team4.monew.asynchronous.event.comment.CommentUpdatedEvent;
import com.team4.monew.asynchronous.event.commentlike.CommentLikeCreatedEvent;
import com.team4.monew.asynchronous.event.commentlike.CommentLikeDeletedEvent;
import com.team4.monew.asynchronous.event.subscription.SubscriptionCreatedEvent;
import com.team4.monew.asynchronous.event.subscription.SubscriptionDeletedEvent;
import com.team4.monew.asynchronous.event.subscription.SubscriptionUpdatedEvent;
import com.team4.monew.service.basic.BasicUserActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserActivityEventHandler {

  private final BasicUserActivityService userActivityService;

  @Async
  @EventListener
  public void handleCommentCreated(CommentCreatedEvent event) {
    userActivityService.addRecentComment(event.getUserId(), event.getComment());
  }

  @Async
  @EventListener
  public void handleCommentDeleted(CommentDeletedEvent event) {
    userActivityService.removeRecentComment(event.getUserId(), event.getCommentId());
  }

  @Async
  @EventListener
  public void handleCommentUpdated(CommentUpdatedEvent event) {
    userActivityService.updateRecentComment(event.getUserId(), event.getComment());
  }


  @Async
  @EventListener
  public void handleCommentLikeCreated(CommentLikeCreatedEvent event) {
    userActivityService.addCommentLike(event.getUserId(), event.getCommentLike());
  }

  @Async
  @EventListener
  public void handleCommentLikeDeleted(CommentLikeDeletedEvent event) {
    userActivityService.removeCommentLike(event.getUserId(), event.getCommentLikeId());
  }

  @Async
  @EventListener
  public void handleSubscriptionCreated(SubscriptionCreatedEvent event) {
    userActivityService.addSubscription(event.getUserId(), event.getSubscription());
  }

  @Async
  @EventListener
  public void handleSubscriptionDeleted(SubscriptionDeletedEvent event) {
    userActivityService.removeSubscription(event.getUserId(), event.getSubscriptionId());
  }

  @Async
  @EventListener
  public void handleSubscriptionUpdated(SubscriptionUpdatedEvent event) {
    userActivityService.updateSubscription(event.getUserId(), event.getSubscription());
  }

  @Async
  @EventListener
  public void handleArticleViewCreated(ArticleViewCreatedEvent event) {
    userActivityService.addRecentArticleView(event.getUserId(), event.getArticleView());
  }

  @Async
  @EventListener
  public void handleArticleViewDeleted(ArticleViewDeletedEvent event) {
    userActivityService.removeRecentArticleView(event.getUserId(), event.getArticleId());
  }
}


