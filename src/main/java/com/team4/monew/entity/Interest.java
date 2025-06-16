package com.team4.monew.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "interests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Interest {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, unique = true, length = 100)
  private String name;

  @Column(name = "subscriber_count", nullable = false)
  private Long subscriberCount = 0L;

  @CreatedDate
  @Column(name = "created_at", updatable = false, nullable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @OneToMany(mappedBy = "interest", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<InterestKeyword> keywords = new ArrayList<>();

  @OneToMany(mappedBy = "interest", cascade = CascadeType.REMOVE, orphanRemoval = true)
  private List<Subscription> subscriptions = new ArrayList<>();

  @ManyToMany(mappedBy = "interest")
  private Set<Article> article = new HashSet<>();

  public Interest(String name, List<InterestKeyword> keywords) {
    this.name = name;
    this.keywords = keywords;
    this.subscriberCount = 0L;
  }

  public Interest(UUID id, String name, Long subscriberCount,
      Instant createdAt, Instant updatedAt,
      List<InterestKeyword> keywords, Set<Article> article) {
    this.id = id;
    this.name = name;
    this.subscriberCount = subscriberCount;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.keywords = keywords;
    this.subscriptions = new ArrayList<>();
    this.article = article;
  }


  public void updateKeywords(List<String> newKeywords) {
    this.keywords.clear();

    for (String keyword : newKeywords) {
      this.keywords.add(new InterestKeyword(this, keyword));
    }
  }

  public void increaseSubscriberCount() {
    this.subscriberCount += 1;
  }

  public void decreaseSubscriberCount() {
    if (subscriberCount > 0) {
      subscriberCount--;
    }
  }

  public void addKeyword(InterestKeyword keyword) {
    this.keywords.add(keyword);
    keyword.setInterest(this);
  }
}
