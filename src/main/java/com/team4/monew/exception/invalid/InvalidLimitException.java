package com.team4.monew.exception.invalid;

import com.team4.monew.exception.ErrorCode;
import com.team4.monew.exception.MonewException;

public class InvalidLimitException extends MonewException {
  public InvalidLimitException() {
    super(ErrorCode.INVALID_LIMIT);
  }
}
