package com.team4.monew.entity;

import com.team4.monew.dto.UserActivity.CommentActivityDto;
import com.team4.monew.dto.UserActivity.CommentLikeActivityDto;
import com.team4.monew.dto.interest.SubscriptionDto;
import com.team4.monew.dto.news.ArticleViewDto;
import com.team4.monew.dto.user.UserDto;
import jakarta.persistence.Id;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "user_activities")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserActivityDocument {

  @Id
  private String id;

  private UserDto user;

  private List<CommentActivityDto> recentCommentActivityDtos;
  private List<CommentLikeActivityDto> recentCommentLikeActivityDtos;
  private List<ArticleViewDto> recentArticleViewDtos;
  private List<SubscriptionDto> subscriptionDtos;

  private Instant lastUpdated;

  public UserActivityDocument(UUID id, UserDto user) {
    this.id = id.toString();
    this.user = user;
    recentCommentActivityDtos = new ArrayList<>();
    recentCommentLikeActivityDtos = new ArrayList<>();
    recentArticleViewDtos = new ArrayList<>();
    subscriptionDtos = new ArrayList<>();
  }
}
