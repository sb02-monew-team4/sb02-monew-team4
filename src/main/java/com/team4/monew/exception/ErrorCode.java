package com.team4.monew.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

  // USER
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
  DUPLICATE_USER(HttpStatus.CONFLICT, "이미 존재하는 사용자입니다."),
  WRONG_PASSWORD(HttpStatus.UNAUTHORIZED, "잘못된 비밀번호입니다."),
  UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "본인만 접근할 수 있습니다."),

  // NOTIFICATION
  NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다."),

  // USER_ACTIVITY
  USER_ACTIVITY_NOT_FOUND(HttpStatus.NOT_FOUND, "유저 활동 내역이 없습니다"),
  USER_ACTIVITY_NOT_FOUND_IN_MONGO(HttpStatus.NOT_FOUND, "MongoDB에서 유저 활동 내역을 찾을 수 없습니다."),

  // Article
  ARTICLE_NOT_FOUND(HttpStatus.NOT_FOUND, "기사를 찾을 수 없습니다."),
  ARTICLE_BACKUP_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "기사 백업에 실패했습니다."),
  ARTICLE_READ_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "백업 기사 다운로드에 실패했습니다."),
  ARTICLE_SERIALIZATION_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "기사 JSON 직렬화에 실패했습니다."),

  // COMMENT
  COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글이 존재하지 않습니다."),
  COMMENT_FORBIDDEN(HttpStatus.FORBIDDEN, "댓글에 대한 권한이 없습니다."),
  COMMENT_LIKE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "좋아요를 누르지 않은 상태입니다."),
  COMMENT_ALREADY_LIKED(HttpStatus.BAD_REQUEST, "이미 좋아요를 눌렀습니다."),
  COMMENT_ALREADY_DELETED(HttpStatus.CONFLICT, "삭제된 댓글은 수정할 수 없습니다."),

  // INTEREST
  INTEREST_ALREADY_EXISTS(HttpStatus.CONFLICT, "유사한 관심사가 이미 존재합니다."),
  INTEREST_NOT_FOUND(HttpStatus.NOT_FOUND, "관심사를 찾을 수 없습니다."),
  INTEREST_FORBIDDEN(HttpStatus.FORBIDDEN, "해당 관심사에 대한 권한이 없습니다."),
  KEYWORDS_REQUIRED(HttpStatus.BAD_REQUEST, "관심 키워드는 최소 1개 이상 입력해야 합니다."),
  KEYWORDS_TOO_MANY(HttpStatus.BAD_REQUEST, "키워드는 최대 10개까지 입력 가능합니다"),
  KEYWORD_TOO_LONG(HttpStatus.BAD_REQUEST, "키워드는 20자 이내로 입력해주세요."),

  // SUBSCRIPTION
  SUBSCRIPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "구독 정보가 존재하지 않습니다."),

  // QUERYDSL
  INVALID_ORDER_BY(HttpStatus.BAD_REQUEST, "orderBy 값이 잘못되었습니다."),
  INVALID_CURSOR_FORMAT(HttpStatus.BAD_REQUEST, "cursor 값이 잘못되었습니다."),
  INVALID_AFTER_FORMAT(HttpStatus.BAD_REQUEST, "after 값이 잘못되었습니다."),
  INVALID_SORT_DIRECTION(HttpStatus.BAD_REQUEST, "direction 값은 'ASC' 또는 'DESC'만 가능합니다."),
  INVALID_LIMIT(HttpStatus.BAD_REQUEST, "limit 값은 1 이상 100 이하이어야 합니다.");

  private HttpStatus status;
  private String message;

  ErrorCode(HttpStatus status, String message) {
    this.status = status;
    this.message = message;
  }

}