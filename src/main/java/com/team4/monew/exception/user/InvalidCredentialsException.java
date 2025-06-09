package com.team4.monew.exception.user;

import com.team4.monew.exception.ErrorCode;

public class InvalidCredentialsException extends UserException {

  public InvalidCredentialsException(ErrorCode errorCode) {
    super(errorCode);
  }

  public static InvalidCredentialsException wrongPassword() {
    return new InvalidCredentialsException(ErrorCode.WRONG_PASSWORD);
  }
}
