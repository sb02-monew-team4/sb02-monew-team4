package com.team4.monew.dto.article;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;

public record ArticleSearchRequest(
    @Size(max = 100, message = "키워드는 100자를 초과할 수 없습니다")
    String keyword,
    UUID interestId,
    List<String> SourceIn,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    Instant publishDateFrom,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    Instant publishDateTo,

    @Pattern(regexp = "publishDate|commentCount|viewCount",
        message = "정렬 기준은 publishDate, commentCount, viewCount 중 하나여야 합니다")
    String orderBy,

    @Pattern(regexp = "ASC|DESC", message = "정렬 방향은 ASC 또는 DESC여야 합니다")
    String direction,
    String cursor,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    Instant after, // 보조 커서 createdAt

    @Min(value = 1, message = "페이지 크기는 최소 1 이상")
    @Max(value = 100, message = "페이지 크기는 최대 100이하")
    Integer limit
) {

  public ArticleSearchRequest {
    if (orderBy == null || orderBy.isEmpty()) {
      orderBy = "publishDate";
    }
    if (direction == null || direction.isEmpty()) {
      direction = "DESC";
    }
    if (limit == null || limit <= 0) {
      limit = 10;
    }

    if (limit() < 1 || limit() > 100) {
      throw new IllegalArgumentException("limit은 1과 100 사이의 값이어야 합니다");
    }

    if (publishDateFrom != null && publishDateTo != null && publishDateFrom.isAfter(
        publishDateTo)) {
      throw new IllegalArgumentException("시작 날짜는 종료 날짜보다 이전이어야 합니다");
    }
  }

  public static ArticleSearchRequest withDefaults() {
    return new ArticleSearchRequest(
        null, null, null, null, null,
        "publishDate", "DESC", null, null, 10
    );
  }

  public static ArticleSearchRequest withKeyword(String keyword) {
    return new ArticleSearchRequest(
        keyword, null, null, null, null,
        "publishDate", "DESC", null, null, 10
    );
  }
}
