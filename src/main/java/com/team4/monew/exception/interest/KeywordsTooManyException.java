package com.team4.monew.exception.interest;

import com.team4.monew.exception.ErrorCode;
import com.team4.monew.exception.MonewException;

public class KeywordsTooManyException extends MonewException {
  public KeywordsTooManyException() {
    super(ErrorCode.KEYWORDS_TOO_MANY);
  }
}