package com.team4.monew.dto.article;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record ArticleViewDto(
    UUID id,
    UUID viewedBy,
    LocalDateTime createdAt,
    UUID articleId,
    String source,
    String sourceUrl,
    Instant articlePublishedDate,
    String articleSummary,
    long articleCommentCount,
    long articleViewCount
) {

}
