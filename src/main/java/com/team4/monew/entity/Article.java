package com.team4.monew.entity;

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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "news")
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
  private Instant date;

  @Column(length = 150, nullable = false)
  private String summary;

  @Column(name = "view_count", nullable = false)
  private Long viewCount = 0L;

  @Column(name = "is_deleted", nullable = false)
  private Boolean isDeleted = false;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @ManyToMany
  @JoinTable(
      name = "news_interests",
      joinColumns = @JoinColumn(name = "news_id"),
      inverseJoinColumns = @JoinColumn(name = "interest_id")
  )
  private Set<Interest> interest = new HashSet<>();
}
