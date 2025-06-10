package com.team4.monew.service;

import com.team4.monew.dto.UserActivity.UserActivityDto;
import com.team4.monew.entity.ArticleView;
import com.team4.monew.entity.Comment;
import com.team4.monew.entity.CommentLike;
import com.team4.monew.entity.Subscription;
import com.team4.monew.entity.User;
import java.util.UUID;

public interface UserActivityService {

  UserActivityDto create(User user);

  UserActivityDto getByUserId(UUID userId);

  void addRecentComment(UUID userId, Comment comment);

  void removeRecentComment(UUID userId, Comment comment);

  void updateRecentComment(UUID userId, Comment comment);

  void addCommentLike(UUID userId, CommentLike commentLike);

  void removeCommentLike(UUID userId, CommentLike commentLike);

  void addRecentArticleView(UUID userId, ArticleView articleView);

  void removeRecentArticleView(UUID userId, ArticleView articleView);

  void addSubscription(UUID userId, Subscription subscription);

  void removeSubscription(UUID userId, Subscription subscription);

  void updateSubscription(UUID userId, Subscription subscription);


}
