package com.team4.monew.exception;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

@Getter
public class MonewException extends RuntimeException {

  final Instant timestamp;
  final ErrorCode errorCode;
  final Map<String, Object> details;

  public MonewException(ErrorCode errorCode) {
    super(errorCode.toString());
    this.timestamp = Instant.now();
    this.errorCode = errorCode;
    this.details = new HashMap<>();
  }

  public MonewException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode.toString());
    this.timestamp = Instant.now();
    this.errorCode = errorCode;
    this.details = details;
  }
}
