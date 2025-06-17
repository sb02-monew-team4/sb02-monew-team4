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
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "article_views",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_article_user",
        columnNames = {"article_id", "user_id"}))
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ArticleView {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "article_id", referencedColumnName = "id", nullable = false)
  private Article article;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
  private User user;

  @CreatedDate
  @LastModifiedDate
  @Column(name = "viewed_at", nullable = false)
  private LocalDateTime viewedAt;

  public ArticleView(Article article, User user) {
    this.article = article;
    this.user = user;
  }
}
