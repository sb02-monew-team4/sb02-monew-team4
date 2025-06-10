package com.team4.monew.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Comment {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "article_id", nullable = false)
  private Article article;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "contents", columnDefinition = "TEXT", nullable = false)
  private String content;

  @CreatedDate
  @Column(name = "created_at", updatable = false, nullable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "is_deleted")
  private Boolean isDeleted = false;

  @Column(name = "like_count")
  private Long likeCount = 0L;

  public Comment(User user, Article article, String content) {
    this.user = user;
    this.article = article;
    this.content = content;
  }

  private Comment(UUID id, User user, Article article, String content, Long likeCount, Instant createdAt) {
    this.id = id;
    this.user = user;
    this.article = article;
    this.content = content;
    this.likeCount = likeCount != null ? likeCount : 0L;
    this.createdAt = createdAt;
  }

  public static Comment createWithLikeCount(UUID id, User user, Article article, String content, Long likeCount, Instant createdAt) {
    return new Comment(id, user, article, content, likeCount, createdAt);
  }

  public static Comment createWithCreatedAt(UUID id, User user, Article article, String content, Instant createdAt) {
    return new Comment(id, user, article, content, 0L, createdAt);
  }

  public void updateContent(String content) {
    if (this.isDeleted) {
      throw new IllegalStateException("삭제된 댓글은 수정할 수 없습니다.");
    }
    this.content = content;
  }

  public void markDeleted() {
    this.isDeleted = true;
  }

  public void increaseLikeCount() {
    this.likeCount++;
  }

  public void decreaseLikeCount() {
    if (this.likeCount > 0) {
      this.likeCount--;
    }
  }
}
