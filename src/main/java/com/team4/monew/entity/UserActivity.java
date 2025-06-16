package com.team4.monew.entity;

import com.team4.monew.dto.UserActivity.CommentActivityDto;
import com.team4.monew.dto.UserActivity.CommentLikeActivityDto;
import com.team4.monew.dto.article.ArticleViewDto;
import com.team4.monew.dto.interest.SubscriptionDto;
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
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "user_activities")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserActivity {

  @Id
  private UUID id;

  private UserDto user;

  private List<CommentActivityDto> recentCommentActivityDtos;
  private List<CommentLikeActivityDto> recentCommentLikeActivityDtos;
  private List<ArticleViewDto> recentArticleViewDtos;
  private List<SubscriptionDto> subscriptionDtos;

  @CreatedDate
  @LastModifiedDate
  private Instant lastUpdated;

  public UserActivity(UserDto user) {
    this.id = UUID.randomUUID();
    this.user = user;
    recentCommentActivityDtos = new ArrayList<>();
    recentCommentLikeActivityDtos = new ArrayList<>();
    recentArticleViewDtos = new ArrayList<>();
    subscriptionDtos = new ArrayList<>();
  }

  public void updateUser(UserDto userdto) {
    this.user = userdto;
  }

  public void updateSubscriptionDtos(List<SubscriptionDto> subscriptiondto) {
    this.subscriptionDtos = subscriptiondto;
  }
}