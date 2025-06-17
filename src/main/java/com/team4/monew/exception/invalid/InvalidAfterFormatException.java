package com.team4.monew.exception.invalid;

import com.team4.monew.exception.ErrorCode;
import com.team4.monew.exception.MonewException;

public class InvalidAfterFormatException extends MonewException {
  public InvalidAfterFormatException() {
    super(ErrorCode.INVALID_AFTER_FORMAT);
  }
}
