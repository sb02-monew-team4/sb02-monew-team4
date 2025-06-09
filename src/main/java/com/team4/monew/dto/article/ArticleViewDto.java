package com.team4.monew.dto.article;

import java.time.Instant;
import java.util.UUID;

public record ArticleViewDto(
    UUID id,
    UUID viewedBy,
    Instant createdAt,
    UUID articleId,
    String source,
    String sourceUrl,
    Instant articlePublishedDate,
    String articleSummary,
    long articleCommentCount,
    long articleViewCount
) {

}
