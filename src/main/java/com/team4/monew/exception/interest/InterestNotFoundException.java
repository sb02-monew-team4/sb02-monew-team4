package com.team4.monew.exception.interest;

import com.team4.monew.exception.ErrorCode;
import com.team4.monew.exception.MonewException;

public class InterestNotFoundException extends MonewException {
  public InterestNotFoundException() {
    super(ErrorCode.INTEREST_NOT_FOUND);
  }
}