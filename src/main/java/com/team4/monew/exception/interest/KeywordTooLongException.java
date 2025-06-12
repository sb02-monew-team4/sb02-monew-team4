package com.team4.monew.exception.interest;

import com.team4.monew.exception.ErrorCode;
import com.team4.monew.exception.MonewException;

public class KeywordTooLongException extends MonewException {
  public KeywordTooLongException() {
    super(ErrorCode.KEYWORD_TOO_LONG);
  }
}