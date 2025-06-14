package com.team4.monew.exception.article;

import com.team4.monew.exception.ErrorCode;
import com.team4.monew.exception.MonewException;
import java.util.Map;

public class ArticleException extends MonewException {

  public ArticleException(ErrorCode errorCode) {
    super(errorCode);
  }

  public ArticleException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
