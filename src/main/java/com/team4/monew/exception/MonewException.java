package com.team4.monew.exception;

import java.time.Instant;
import lombok.Getter;

@Getter
public class MonewException extends RuntimeException {

  final Instant timestamp;
  final ErrorCode errorCode;

  public MonewException(final Instant timestamp, final ErrorCode errorCode) {
    this.timestamp = timestamp;
    this.errorCode = errorCode;
  }
}
