package com.team4.monew.exception.userActivity;

import com.team4.monew.exception.ErrorCode;
import com.team4.monew.exception.MonewException;
import java.util.Map;

public abstract class UserActivityException extends MonewException {

  public UserActivityException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
