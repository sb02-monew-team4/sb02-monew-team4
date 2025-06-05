package com.team4.monew.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

  // USER
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
  DUPLICATE_USER(HttpStatus.CONFLICT, "이미 존재하는 사용자입니다."),
  WRONG_PASSWORD(HttpStatus.CONFLICT, "잘못된 비밀번호입니다."),

  // NEWS
  NEWS_NOT_FOUND(HttpStatus.NOT_FOUND, "뉴스를 찾을 수 없습니다.");


  private HttpStatus status;
  private String message;

  ErrorCode(HttpStatus status, String message) {
    this.status = status;
    this.message = message;
  }

}
