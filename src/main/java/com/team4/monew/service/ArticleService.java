package com.team4.monew.service;

import com.team4.monew.dto.article.ArticleRestoreResultDto;
import com.team4.monew.dto.article.ArticleSearchRequest;
import com.team4.monew.dto.article.ArticleViewDto;
import com.team4.monew.dto.article.CursorPageResponseArticleDto;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ArticleService {

  ArticleViewDto registerArticleView(UUID articleId, UUID userId);

  CursorPageResponseArticleDto getAllArticles(ArticleSearchRequest request, UUID userId);

  List<String> getAllSources();

  ArticleRestoreResultDto restoreArticle(Instant from, Instant to);

  void hardDelete(UUID articleId);

  void softDelete(UUID articleId);
}
