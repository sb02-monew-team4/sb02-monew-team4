package com.team4.monew.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

  // USER
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
  DUPLICATE_USER(HttpStatus.CONFLICT, "이미 존재하는 사용자입니다."),
  WRONG_PASSWORD(HttpStatus.CONFLICT, "잘못된 비밀번호입니다."),

  // COMMENT
  COMMENT_NOT_FOUND(HttpStatus.BAD_REQUEST, "댓글이 존재하지 않습니다."),
  COMMENT_FORBIDDEN(HttpStatus.FORBIDDEN, "댓글에 대한 권한이 없습니다."),
  COMMENT_LIKE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "좋아요를 누르지 않은 상태입니다."),
  COMMENT_ALREADY_LIKED(HttpStatus.BAD_REQUEST, "이미 좋아요를 눌렀습니다.");

  private HttpStatus status;
  private String message;

  ErrorCode(HttpStatus status, String message) {
    this.status = status;
    this.message = message;
  }

}
