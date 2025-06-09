package com.team4.monew.entity;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

@Entity
@Table(name = "user_activities")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserActivity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_id", nullable = false, unique = true)
  private UUID userId;

  @ElementCollection
  @Column(name = "recent_comment_ids", columnDefinition = "uuid[]")
  private List<UUID> recentCommentIds;

  @ElementCollection
  @Column(name = "recent_comment_like_ids", columnDefinition = "uuid[]")
  private List<UUID> recentCommentLikeIds;

  @ElementCollection
  @Column(name = "recent_article_views_ids", columnDefinition = "uuid[]")
  private List<UUID> recentArticleViewIds;

  @ElementCollection
  @Column(name = "subscription_ids", columnDefinition = "uuid[]")
  private List<UUID> subscriptionIds;

  @CreatedDate
  @LastModifiedDate
  @Column(name = "last_updated")
  private Instant lastUpdated;

  public UserActivity(UUID userId) {
    this.userId = userId;
    recentCommentIds = new ArrayList<>();
    recentCommentLikeIds = new ArrayList<>();
    recentArticleViewIds = new ArrayList<>();
    subscriptionIds = new ArrayList<>();
  }
}