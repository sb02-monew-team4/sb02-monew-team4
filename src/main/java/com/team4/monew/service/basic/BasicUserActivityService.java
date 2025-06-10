package com.team4.monew.service.basic;

import com.team4.monew.dto.UserActivity.CommentActivityDto;
import com.team4.monew.dto.UserActivity.CommentLikeActivityDto;
import com.team4.monew.dto.UserActivity.UserActivityDto;
import com.team4.monew.dto.article.ArticleViewDto;
import com.team4.monew.dto.interest.SubscriptionDto;
import com.team4.monew.dto.user.UserDto;
import com.team4.monew.entity.ArticleView;
import com.team4.monew.entity.Comment;
import com.team4.monew.entity.CommentLike;
import com.team4.monew.entity.Subscription;
import com.team4.monew.entity.User;
import com.team4.monew.entity.UserActivity;
import com.team4.monew.exception.userActivity.UserActivityNotFoundException;
import com.team4.monew.mapper.ArticleViewMapper;
import com.team4.monew.mapper.CommentActivityMapper;
import com.team4.monew.mapper.CommentLikeActivityMapper;
import com.team4.monew.mapper.SubscriptionMapper;
import com.team4.monew.mapper.UserActivityMapper;
import com.team4.monew.mapper.UserMapper;
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

  @Transactional
  @Override
  public UserActivityDto create(User user) {
    UserDto dto = userMapper.toDto(user);
    UserActivity userActivity = new UserActivity(dto);
    userActivityRepository.save(userActivity);

    return userActivityMapper.toDto(userActivity);
  }

  @Override
  public UserActivityDto getByUserId(UUID userId) {
    UserActivity userActivity = getUserActivityOrThrow(userId);
    return userActivityMapper.toDto(userActivity);
  }

  @Transactional
  @Override
  public void addRecentComment(UUID userId, Comment comment) {
    CommentActivityDto dto = commentActivityMapper.toDto(comment);

    UserActivity userActivity = getUserActivityOrThrow(userId);
    List<CommentActivityDto> recentCommentDtos = userActivity.getRecentCommentActivityDtos();
    UserActivityListUtils.addToLimitedList(recentCommentDtos, dto);
    userActivityRepository.save(userActivity);
  }

  @Transactional
  @Override
  public void removeRecentComment(UUID userId, Comment comment) {
    CommentActivityDto dto = commentActivityMapper.toDto(comment);

    UserActivity userActivity = getUserActivityOrThrow(userId);
    userActivity.getRecentCommentActivityDtos().remove(dto);
    userActivityRepository.save(userActivity);

    UserActivity userActivity = getUserActivityOrThrow(userId);
    List<CommentActivityDto> recentCommentDtos = userActivity.getRecentCommentActivityDtos();

    int idx = recentCommentDtos.indexOf(dto);
    if (idx != -1) {
      recentCommentDtos.set(idx, dto);
      userActivityRepository.save(userActivity);
    }
  }

  @Transactional
  @Override
  public void addCommentLike(UUID userId, CommentLike commentLike) {
    CommentLikeActivityDto dto = commentLikeActivityMapper.toDto(commentLike);

    UserActivity userActivity = getUserActivityOrThrow(userId);
    List<CommentLikeActivityDto> recentCommentLikeActivityDtos = userActivity.getRecentCommentLikeActivityDtos();
    UserActivityListUtils.addToLimitedList(recentCommentLikeActivityDtos, dto);
    userActivityRepository.save(userActivity);
  }

  @Transactional
  @Override
  public void removeCommentLike(UUID userId, CommentLike commentLike) {
    CommentLikeActivityDto dto = commentLikeActivityMapper.toDto(commentLike);

    UserActivity userActivity = getUserActivityOrThrow(userId);
    userActivity.getRecentCommentActivityDtos().remove(dto);
    userActivityRepository.save(userActivity);
  }

  @Transactional
  @Override
  public void addRecentArticleView(UUID userId, ArticleView articleView) {
    ArticleViewDto dto = articleViewMapper.toDto(articleView);

    UserActivity userActivity = getUserActivityOrThrow(userId);
    List<ArticleViewDto> recentArticleViewDtos = userActivity.getRecentArticleViewDtos();
    UserActivityListUtils.addToLimitedList(recentArticleViewDtos, dto);
    userActivityRepository.save(userActivity);
  }

  @Transactional
  @Override
  public void removeRecentArticleView(UUID userId, ArticleView articleView) {
    ArticleViewDto dto = articleViewMapper.toDto(articleView);

    UserActivity userActivity = getUserActivityOrThrow(userId);
    userActivity.getRecentArticleViewDtos().remove(dto);
    userActivityRepository.save(userActivity);
  }

  @Transactional
  @Override
  public void addSubscription(UUID userId, Subscription subscription) {
    SubscriptionDto dto = subscriptionMapper.toDto(subscription);

    UserActivity userActivity = getUserActivityOrThrow(userId);
    userActivity.getSubscriptionDtos().add(dto);
    userActivityRepository.save(userActivity);
  }

  @Transactional
  @Override
  public void removeSubscription(UUID userId, Subscription subscription) {
    SubscriptionDto dto = subscriptionMapper.toDto(subscription);

    UserActivity userActivity = getUserActivityOrThrow(userId);
    userActivity.getSubscriptionDtos().remove(dto);
    userActivityRepository.save(userActivity);
  }


  private UserActivity getUserActivityOrThrow(UUID userId) {
    return userActivityRepository.findByUser_UserId(userId)
        .orElseThrow(() -> UserActivityNotFoundException.byUserId(userId));
  }
}
