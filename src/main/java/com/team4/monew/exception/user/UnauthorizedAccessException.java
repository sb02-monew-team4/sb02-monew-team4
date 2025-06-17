package com.team4.monew.exception.user;

import com.team4.monew.exception.ErrorCode;

public class UnauthorizedAccessException extends UserException {

  public UnauthorizedAccessException(ErrorCode errorCode) {
    super(errorCode);
  }

  public static UnauthorizedAccessException byUserId() {
    return new UnauthorizedAccessException(ErrorCode.UNAUTHORIZED_ACCESS);
  }
}
