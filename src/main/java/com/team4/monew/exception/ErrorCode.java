package com.team4.monew.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

  // USER
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found");


  private HttpStatus status;
  private String message;

  ErrorCode(HttpStatus status, String message) {
    this.status = status;
    this.message = message;
  }

}
