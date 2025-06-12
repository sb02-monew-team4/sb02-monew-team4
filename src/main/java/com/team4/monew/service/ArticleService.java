package com.team4.monew.service;

import com.team4.monew.dto.article.ArticleSearchRequest;
import com.team4.monew.dto.article.ArticleViewDto;
import com.team4.monew.dto.article.CursorPageResponseArticleDto;
import java.util.List;
import java.util.UUID;

public interface ArticleService {

  ArticleViewDto registerArticleView(UUID articleId, UUID userId);

  void hardDelete(UUID articleId);

  void softDelete(UUID articleId);

  List<String> getAllSources();

  CursorPageResponseArticleDto getAllArticles(ArticleSearchRequest request, UUID userId);
}
