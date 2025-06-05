package com.team4.monew.exception.user;

import com.team4.monew.exception.ErrorCode;
import com.team4.monew.exception.MonewException;
import java.util.Map;

public abstract class UserException extends MonewException {

  protected UserException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }

}
