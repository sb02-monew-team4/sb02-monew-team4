package com.team4.monew.exception.article;

import com.team4.monew.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class ArticleNotFoundException extends ArticleException {

  public ArticleNotFoundException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }

  public static ArticleNotFoundException byId(UUID ArticleId) {
    return new ArticleNotFoundException(ErrorCode.ARTICLE_NOT_FOUND,
        Map.of("ArticleId", ArticleId));
  }
}
