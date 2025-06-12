package com.team4.monew.repository;

import com.team4.monew.dto.article.ArticleSearchRequest;
import com.team4.monew.dto.article.CursorPageResponseArticleDto;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ArticleRepositoryCustom {

  CursorPageResponseArticleDto findArticlesWithCursor(
      ArticleSearchRequest request,
      UUID userId
  );

  long countArticlesWithConditions(
      String keyword,
      UUID interestId,
      List<String> sourceIn,
      Instant publishDateFrom,
      Instant publishDateTo
  );
}
