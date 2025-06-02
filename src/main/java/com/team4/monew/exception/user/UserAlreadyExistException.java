package com.team4.monew.exception.user;

import com.team4.monew.exception.ErrorCode;
import java.util.Map;

public class UserAlreadyExistException extends UserException {

  public UserAlreadyExistException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }

  public static UserAlreadyExistException byEmail(String email) {
    return new UserAlreadyExistException(ErrorCode.DUPLICATE_USER, Map.of("email", email));
  }

}
