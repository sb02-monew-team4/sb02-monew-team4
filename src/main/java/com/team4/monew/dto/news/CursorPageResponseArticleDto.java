package com.team4.monew.dto.news;

import java.time.Instant;
import java.util.List;

public record CursorPageResponseArticleDto(
    List<ArticleDto> content,
    String nextCursor,
    Instant nextAfter,
    int size,
    long totalElements,
    boolean hasNext
) {

}
