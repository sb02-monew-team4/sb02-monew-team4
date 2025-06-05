package com.team4.monew.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

  // USER
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
  DUPLICATE_USER(HttpStatus.CONFLICT, "이미 존재하는 사용자입니다."),
  WRONG_PASSWORD(HttpStatus.CONFLICT, "잘못된 비밀번호입니다.");
  
  // USER_ACTIVITY
  USER_ACTIVITY_NOT_FOUND(HttpStatus.NOT_FOUND, "유저 활동 내역이 없습니다"),
  USER_ACTIVITY_NOT_FOUND_IN_MONGO(HttpStatus.NOT_FOUND, "MongoDB에서 유저 활동 내역을 찾을 수 없습니다.");


  private HttpStatus status;
  private String message;

  ErrorCode(HttpStatus status, String message) {
    this.status = status;
    this.message = message;
  }
}
