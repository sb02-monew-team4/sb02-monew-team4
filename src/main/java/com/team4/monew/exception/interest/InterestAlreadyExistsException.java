package com.team4.monew.exception.interest;

import com.team4.monew.exception.ErrorCode;
import com.team4.monew.exception.MonewException;

public class InterestAlreadyExistsException extends MonewException {
  public InterestAlreadyExistsException() {
    super(ErrorCode.INTEREST_ALREADY_EXISTS);
  }
}
