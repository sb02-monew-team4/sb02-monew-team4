package com.team4.monew.exception.comment;

import com.team4.monew.exception.ErrorCode;
import com.team4.monew.exception.MonewException;

public class InvalidSortDirectionException extends MonewException {
  public InvalidSortDirectionException() {
    super(ErrorCode.INVALID_SORT_DIRECTION);
  }
}
