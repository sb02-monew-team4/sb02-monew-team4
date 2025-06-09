package com.team4.monew.service.basic;

import com.team4.monew.dto.UserActivity.CommentActivityDto;
import com.team4.monew.dto.UserActivity.CommentLikeActivityDto;
import com.team4.monew.dto.UserActivity.UserActivityDto;
import com.team4.monew.dto.interest.SubscriptionDto;
import com.team4.monew.dto.news.ArticleViewDto;
import com.team4.monew.dto.user.UserDto;
import com.team4.monew.entity.ArticleView;
import com.team4.monew.entity.Comment;
import com.team4.monew.entity.CommentLike;
import com.team4.monew.entity.Subscription;
import com.team4.monew.entity.User;
import com.team4.monew.entity.UserActivity;
import com.team4.monew.entity.UserActivityDocument;
import com.team4.monew.exception.userActivity.UserActivityNotFoundException;
import com.team4.monew.exception.userActivity.UserActivityNotFoundInMongoException;
import com.team4.monew.mapper.CommentActivityMapper;
import com.team4.monew.mapper.CommentLikeActivityMapper;
import com.team4.monew.mapper.SubscriptionMapper;
import com.team4.monew.mapper.UserActivityMapper;
import com.team4.monew.repository.UserActivityMongoRepository;
import com.team4.monew.repository.UserActivityRepository;
import com.team4.monew.service.UserActivityService;
import com.team4.monew.util.UserActivityListUtils;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BasicUserActivityService implements UserActivityService {

  private final UserActivityRepository userActivityRepository;
  private final UserActivityMapper userActivityMapper;
  private final CommentActivityMapper commentActivityMapper;
  private final SubscriptionMapper subscriptionMapper;
  private final UserMapper userMapper;
  private final ArticleViewMapper articleViewMapper;
  private final CommentLikeActivityMapper commentLikeActivityMapper;
  private final UserActivityMongoRepository userActivityMongoRepository;

  @Transactional
  @Override
  public UserActivityDto create(User user) {
    UserDto dto = UserMapper.toDto(user);
    UserActivity userActivity = new UserActivity(dto.id());
    userActivityRepository.save(userActivity);

    UserActivityDocument userActivityDocument = new UserActivityDocument(userActivity.getId(),
        dto);
    userActivityMongoRepository.save(userActivityDocument);
    return userActivityMapper.toDto(userActivityDocument);
  }

  @Override
  public UserActivityDto getByUserId(UUID userId) {
    UserActivityDocument userActivityDocument = getUserActivityDocOrThrow(userId);
    return userActivityMapper.toDto(userActivityDocument);
  }

  @Transactional
  @Override
  public void addRecentComment(UUID userId, Comment comment) {
    CommentActivityDto dto = commentActivityMapper.toDto(comment);

    UserActivity userActivity = getUserActivityOrThrow(userId);
    List<UUID> recentCommentIds = userActivity.getRecentCommentIds();
    UserActivityListUtils.addToLimitedList(recentCommentIds, comment.getId());
    userActivityRepository.save(userActivity);

    UserActivityDocument userActivityDocument = getUserActivityDocOrThrow(userId);
    List<CommentActivityDto> recentCommentDtos = userActivityDocument.getRecentCommentActivityDtos();
    UserActivityListUtils.addToLimitedList(recentCommentDtos, dto);
    userActivityMongoRepository.save(userActivityDocument);
  }

  @Transactional
  @Override
  public void removeRecentComment(UUID userId, Comment comment) {
    CommentActivityDto dto = commentActivityMapper.toDto(comment);

    UserActivity userActivity = getUserActivityOrThrow(userId);
    userActivity.getRecentCommentIds().remove(comment.getId());
    userActivityRepository.save(userActivity);

    UserActivityDocument userActivityDocument = getUserActivityDocOrThrow(userId);
    userActivityDocument.getRecentCommentActivityDtos().remove(dto);
    userActivityMongoRepository.save(userActivityDocument);
  }

  @Transactional
  @Override
  public void addCommentLike(UUID userId, CommentLike commentLike) {
    CommentLikeActivityDto dto = commentLikeActivityMapper.toDto(commentLike);

    UserActivity userActivity = getUserActivityOrThrow(userId);
    List<UUID> recentCommentLikeIds = userActivity.getRecentCommentLikeIds();
    UserActivityListUtils.addToLimitedList(recentCommentLikeIds, commentLike.getId());
    userActivityRepository.save(userActivity);

    UserActivityDocument userActivityDocument = getUserActivityDocOrThrow(userId);
    List<CommentLikeActivityDto> recentCommentLikeActivityDtos = userActivityDocument.getRecentCommentLikeActivityDtos();
    UserActivityListUtils.addToLimitedList(recentCommentLikeActivityDtos, dto);
    userActivityMongoRepository.save(userActivityDocument);
  }

  @Transactional
  @Override
  public void removeCommentLike(UUID userId, CommentLike commentLike) {
    CommentLikeActivityDto dto = commentLikeActivityMapper.toDto(commentLike);

    UserActivity userActivity = getUserActivityOrThrow(userId);
    userActivity.getRecentCommentLikeIds().remove(commentLike.getId());
    userActivityRepository.save(userActivity);

    UserActivityDocument userActivityDocument = getUserActivityDocOrThrow(userId);
    userActivityDocument.getRecentCommentActivityDtos().remove(dto);
    userActivityMongoRepository.save(userActivityDocument);
  }

  @Transactional
  @Override
  public void addRecentArticleView(UUID userId, ArticleView articleView) {
    ArticleViewDto dto = articleViewMapper.toDto(articleView);

    UserActivity userActivity = getUserActivityOrThrow(userId);
    List<UUID> recentArticleViewIds = userActivity.getRecentArticleViewIds();
    UserActivityListUtils.addToLimitedList(recentArticleViewIds, articleView.getId());
    userActivityRepository.save(userActivity);

    UserActivityDocument userActivityDocument = getUserActivityDocOrThrow(userId);
    List<ArticleViewDto> recentArticleViewDtos = userActivityDocument.getRecentArticleViewDtos();
    UserActivityListUtils.addToLimitedList(recentArticleViewDtos, dto);
    userActivityMongoRepository.save(userActivityDocument);
  }

  @Transactional
  @Override
  public void removeRecentArticleView(UUID userId, ArticleView articleView) {
    ArticleViewDto dto = articleViewMapper.toDto(articleView);

    UserActivity userActivity = getUserActivityOrThrow(userId);
    userActivity.getRecentArticleViewIds().remove(articleView.getId());
    userActivityRepository.save(userActivity);

    UserActivityDocument userActivityDocument = getUserActivityDocOrThrow(userId);
    userActivityDocument.getRecentArticleViewDtos().remove(dto);
    userActivityMongoRepository.save(userActivityDocument);
  }

  @Transactional
  @Override
  public void addSubscription(UUID userId, Subscription subscription) {
    SubscriptionDto dto = subscriptionMapper.toDto(subscription);

    UserActivity userActivity = getUserActivityOrThrow(userId);
    userActivity.getSubscriptionIds().add(subscription.getId());
    userActivityRepository.save(userActivity);

    UserActivityDocument userActivityDocument = getUserActivityDocOrThrow(userId);
    userActivityDocument.getSubscriptionDtos().add(dto);
    userActivityMongoRepository.save(userActivityDocument);
  }

  @Transactional
  @Override
  public void removeSubscription(UUID userId, Subscription subscription) {
    SubscriptionDto dto = subscriptionMapper.toDto(subscription);

    UserActivity userActivity = getUserActivityOrThrow(userId);
    userActivity.getSubscriptionIds().remove(subscription.getId());
    userActivityRepository.save(userActivity);

    UserActivityDocument userActivityDocument = getUserActivityDocOrThrow(userId);
    userActivityDocument.getSubscriptionDtos().remove(dto);
    userActivityMongoRepository.save(userActivityDocument);
  }


  private UserActivity getUserActivityOrThrow(UUID userId) {
    return userActivityRepository.findByUserId(userId)
        .orElseThrow(() -> UserActivityNotFoundException.byUserId(userId));
  }

  private UserActivityDocument getUserActivityDocOrThrow(UUID userId) {
    return userActivityMongoRepository.findByUser_UserId(userId)
        .orElseThrow(() -> UserActivityNotFoundInMongoException.byUserId(userId));
  }
}
