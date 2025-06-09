package com.team4.monew.exception.user;

import com.team4.monew.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class UserNotFoundException extends UserException {

  public UserNotFoundException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }

  public static UserNotFoundException byId(UUID userId) {
    return new UserNotFoundException(ErrorCode.USER_NOT_FOUND, Map.of("userId", userId));
  }

  public static UserNotFoundException byEmail(String email) {
    return new UserNotFoundException(ErrorCode.USER_NOT_FOUND, Map.of("email", email));
  }

}
