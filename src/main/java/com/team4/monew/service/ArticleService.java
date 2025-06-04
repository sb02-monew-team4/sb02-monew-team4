package com.team4.monew.service;

import com.team4.monew.dto.article.ArticleViewDto;
import java.util.UUID;

public interface ArticleService {

  ArticleViewDto registerNewsView(UUID newsId, UUID userId);
}
