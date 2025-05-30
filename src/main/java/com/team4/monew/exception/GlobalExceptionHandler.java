package com.team4.monew.exception;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


  @ExceptionHandler(MonewException.class)
  public ResponseEntity<ErrorResponse> handleException(MonewException e) {
    log.error("Error message: {}", e.getMessage());

    ErrorResponse errorResponse = ErrorResponse.of(
        e.getErrorCode().getStatus().name(),
        e.getMessage(),
        e.getDetails(),
        e.getClass().getSimpleName(),
        e.getErrorCode().getStatus().value()
    );

    return ResponseEntity.status(e.getErrorCode().getStatus()).body(errorResponse);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception e) {
    log.error("Error message: {}", e.getMessage());

    ErrorResponse errorResponse = ErrorResponse.of(
        "INTERNAL_SERVER_ERROR",
        "An unexpected error occurred",
        Map.of("error: ", e.getMessage()),
        e.getClass().getSimpleName(),
        HttpStatus.INTERNAL_SERVER_ERROR.value()
    );

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }
}
