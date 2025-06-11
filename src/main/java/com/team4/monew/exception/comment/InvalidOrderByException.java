package com.team4.monew.exception.comment;

import com.team4.monew.exception.ErrorCode;
import com.team4.monew.exception.MonewException;

public class InvalidOrderByException extends MonewException {
  public InvalidOrderByException() {
    super(ErrorCode.INVALID_ORDER_BY);
  }
}