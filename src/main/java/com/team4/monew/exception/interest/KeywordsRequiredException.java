package com.team4.monew.exception.interest;

import com.team4.monew.exception.ErrorCode;
import com.team4.monew.exception.MonewException;

public class KeywordsRequiredException extends MonewException {
  public KeywordsRequiredException() {
    super(ErrorCode.KEYWORDS_REQUIRED);
  }
}