package com.team4.monew.exception.interest;

import com.team4.monew.exception.ErrorCode;
import com.team4.monew.exception.MonewException;

public class InterestForbiddenException extends MonewException {
  public InterestForbiddenException() {
    super(ErrorCode.INTEREST_FORBIDDEN);
  }
}