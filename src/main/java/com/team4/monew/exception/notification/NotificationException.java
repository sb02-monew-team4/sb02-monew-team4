package com.team4.monew.exception.notification;

import com.team4.monew.exception.ErrorCode;
import com.team4.monew.exception.MonewException;
import java.util.Map;

public abstract class NotificationException extends MonewException {

  protected NotificationException(ErrorCode errorCode) {
    super(errorCode);
  }

  protected NotificationException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }
}
