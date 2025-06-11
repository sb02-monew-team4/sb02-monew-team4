package com.team4.monew.exception.comment;

import com.team4.monew.exception.ErrorCode;
import com.team4.monew.exception.MonewException;

public class InvalidCursorFormatException extends MonewException {
  public InvalidCursorFormatException() {
    super(ErrorCode.INVALID_CURSOR_FORMAT);
  }
}
