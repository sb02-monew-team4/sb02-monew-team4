package com.team4.monew.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Notification {

  @CreatedDate
  @Column(updatable = false, nullable = false)
  private Instant createdAt;
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;
  @Column(nullable = false, length = 255)
  private String content;
  @Column(nullable = false)
  private UUID resourceId;
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ResourceType resourceType;
  @Column(nullable = false)
  private boolean isRead = false;

  public static Notification create(User user, String content, UUID resourceId,
      ResourceType resourceType, boolean isRead) {
    return Notification.builder()
        .user(user)
        .content(content)
        .resourceId(resourceId)
        .resourceType(resourceType)
        .isRead(isRead)
        .build();
  }

  public void update(Boolean isRead) {
    if (isRead != null && !isRead.equals(this.isRead)) {
      this.isRead = isRead;
    }
  }
}
