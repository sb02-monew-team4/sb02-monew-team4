package com.team4.monew.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

  // USER
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),


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
