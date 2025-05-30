package com.team4.monew.exception;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {

  private final Instant timestamp;
  private final String code;
  private final String message;
  private final String exception;
  private final int status;

  public static ErrorResponse of(String code, String message, String exception, int status) {
    return ErrorResponse.builder()
        .timestamp(Instant.now())
        .code(code)
        .message(message)
        .exception(exception)
        .status(status)
        .build();
  }
}
