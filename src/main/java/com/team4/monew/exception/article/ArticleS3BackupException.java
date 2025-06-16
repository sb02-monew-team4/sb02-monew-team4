package com.team4.monew.exception.article;

import com.team4.monew.exception.ErrorCode;

public class ArticleS3BackupException extends ArticleException {

  public ArticleS3BackupException(ErrorCode errorCode) {
    super(errorCode);
  }

  public static ArticleS3BackupException backupFailed() {
    return new ArticleS3BackupException(ErrorCode.ARTICLE_BACKUP_FAIL);
  }

  public static ArticleS3BackupException readFailed() {
    return new ArticleS3BackupException(ErrorCode.ARTICLE_READ_ERROR);
  }

  public static ArticleS3BackupException serializationFailed() {
    return new ArticleS3BackupException(ErrorCode.ARTICLE_SERIALIZATION_FAIL);
  }
}
