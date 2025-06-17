package com.team4.monew.service;

import com.team4.monew.dto.UserActivity.UserActivityDto;
import com.team4.monew.entity.ArticleView;
import com.team4.monew.entity.Comment;
import com.team4.monew.entity.CommentLike;
import com.team4.monew.entity.Subscription;
import com.team4.monew.entity.User;
import java.util.List;
import java.util.UUID;

public interface UserActivityService {

  UserActivityDto create(User user);

  void updateUser(User user);

  void delete(UUID userId);

  UserActivityDto getByUserId(UUID userId);

  void addRecentComment(UUID userId, Comment comment);

  void removeRecentComment(UUID userId, UUID commentId);

  void updateRecentComment(UUID userId, Comment comment);

  void addCommentLike(UUID userId, CommentLike commentLike);

  void removeCommentLike(UUID userId, UUID commentLikeId);

  void addRecentArticleView(UUID userId, ArticleView articleView);

  void removeRecentArticleView(UUID userId, UUID articleId);

  void addSubscription(UUID userId, Subscription subscription);

  void removeSubscription(UUID userId, UUID subscriptionId);

  void removeSubscriptionByInterestId(UUID userId, UUID interestId);

  void updateSubscriptionKeywords(UUID interestId, List<String> newKeywords);

  void syncUserActivity(UUID userId);


}
