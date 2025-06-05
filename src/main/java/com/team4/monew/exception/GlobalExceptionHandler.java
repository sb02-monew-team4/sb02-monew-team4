package com.team4.monew.exception;

import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(
      MethodArgumentNotValidException e) {
    log.error("Error message: {}", e.getMessage());

    Map<String, Object> details = e.getBindingResult().getFieldErrors().stream()
        .collect(Collectors.toMap(
            FieldError::getField,
            FieldError::getDefaultMessage,
            (msg1, msg2) -> msg1 // 필드 하나에 에러가 여러 개인 경우, 첫 번째 에러 메시지만 남겨둠
        ));

    HttpStatus status = HttpStatus.BAD_REQUEST;
    ErrorResponse errorResponse = ErrorResponse.of(
        status.name(),
        e.getMessage(),
        details,
        e.getClass().getSimpleName(),
        status.value()
    );

    return ResponseEntity.status(status).body(errorResponse);
  }

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
