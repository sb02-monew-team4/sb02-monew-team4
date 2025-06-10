package com.team4.monew.exception.notification;

import com.team4.monew.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class NotificationNotFoundException extends NotificationException {

  public NotificationNotFoundException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }

  public static NotificationNotFoundException byId(UUID notificationId) {
    return new NotificationNotFoundException(ErrorCode.NOTIFICATION_NOT_FOUND, Map.of("notificationId", notificationId));
  }

  public static NotificationNotFoundException byUserId(UUID userId) {
    return new NotificationNotFoundException(ErrorCode.NOTIFICATION_NOT_FOUND, Map.of("userId", userId));
  }
}
