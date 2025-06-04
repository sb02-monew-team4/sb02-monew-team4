package com.team4.monew.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "news_views")
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ArticleView {

  @EmbeddedId
  private ArticleViewId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "news_id", referencedColumnName = "id",
      insertable = false, updatable = false)
  private Article news;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", referencedColumnName = "id",
      insertable = false, updatable = false)
  private User user;

  @CreatedDate
  @LastModifiedDate
  @Column(name = "viewed_at", nullable = false)
  private Instant viewedAt;

  public ArticleView(Article news, User user) {
    this.id = new ArticleViewId(news.getId(), user.getId());
    this.news = news;
    this.user = user;
  }
}
