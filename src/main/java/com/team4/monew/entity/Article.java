package com.team4.monew.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "articles")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Article {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(length = 20, nullable = false)
  private String source;

  @Column(unique = true, columnDefinition = "TEXT", nullable = false)
  private String originalLink;

  @Column(length = 100, nullable = false)
  private String title;

  @Column(nullable = false)
  private Instant publishedDate;

  @Column(length = 150, nullable = false)
  private String summary;

  @Column(name = "view_count", nullable = false)
  private Long viewCount = 0L;

  @Column(nullable = false)
  private Long commentCount = 0L;

  @Column(name = "is_deleted", nullable = false)
  private Boolean isDeleted = false;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @ManyToMany
  @JoinTable(
      name = "article_interests",
      joinColumns = @JoinColumn(name = "article_id"),
      inverseJoinColumns = @JoinColumn(name = "interest_id")
  )
  @JsonIgnore
  private Set<Interest> interest = new HashSet<>();

  public Article(String source, String originalLink, String title, Instant publishedDate,
      String summary) {
    this.source = source;
    this.originalLink = originalLink;
    this.title = title;
    this.publishedDate = publishedDate;

    String tempSummary = (summary != null) ? summary : "";
    tempSummary = tempSummary.trim();
    if (tempSummary.length() > 140) {
      this.summary = tempSummary.substring(0, 140) + "...";
    } else {
      this.summary = tempSummary;
    }
  }

  public void addInterest(Interest interest) {
    this.interest.add(interest);
    interest.getArticle().add(this);
  }

  public void removeInterest(Interest interest) {
    this.interest.remove(interest);
    interest.getArticle().remove(this);
  }

  public Set<Interest> getInterest() {
    return Collections.unmodifiableSet(interest);
  }

  public void incrementCommentCount() {
    this.commentCount++;
  }

  public void decrementCommentCount() {
    this.commentCount--;
  }

  public void incrementViewCount() {
    this.viewCount++;
  }

  public void decrementViewCount() {
    this.viewCount--;
  }

  public void updateIsDeleted() {
    if (!isDeleted) {
      this.isDeleted = true;
    }
  }
}
