package com.team4.monew.exception.userActivity;

import com.team4.monew.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class UserActivityNotFoundInMongoException extends UserActivityException {

  public UserActivityNotFoundInMongoException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }

  public static UserActivityNotFoundException byUserId(UUID userId) {
    return new UserActivityNotFoundException(ErrorCode.USER_ACTIVITY_NOT_FOUND,
        Map.of("userId", userId));
  }
}
