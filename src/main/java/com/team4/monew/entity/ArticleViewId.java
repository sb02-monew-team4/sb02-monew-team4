package com.team4.monew.entity;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Embeddable
@Getter
public class ArticleViewId implements Serializable {

  private UUID newsId;
  private UUID userId;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ArticleViewId that = (ArticleViewId) o;
    return newsId.equals(that.newsId) && userId.equals(that.userId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(newsId, userId);
  }
}
