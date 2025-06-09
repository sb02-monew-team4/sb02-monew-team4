package com.team4.monew.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team4.monew.exception.ErrorResponse;
import com.team4.monew.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

  private final UserRepository userRepository;
  private final ObjectMapper objectMapper;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    String userIdHeader = request.getHeader("MoNew-Request-User-ID");

    if (userIdHeader == null || userIdHeader.isEmpty()) {
      writeErrorResponse(response,
          HttpServletResponse.SC_UNAUTHORIZED,
          "UNAUTHORIZED",
          "MoNew-Request-User-ID header is missing",
          null);
      return false;
    }

    try {
      UUID userId = UUID.fromString(userIdHeader);

      if (!userRepository.existsById(userId)) {
        writeErrorResponse(response,
            HttpServletResponse.SC_UNAUTHORIZED,
            "UNAUTHORIZED",
            "Invalid user ID",
            Map.of("userId", userIdHeader));
        return false;
      }

      request.setAttribute("authenticatedUserId", userId);

    } catch (IllegalArgumentException e) {
      writeErrorResponse(response,
          HttpServletResponse.SC_UNAUTHORIZED,
          "UNAUTHORIZED",
          "Invalid UUID format in MoNew-Request-User-ID header",
          Map.of("userId", userIdHeader));
      return false;
    }

    return true;
  }

  private void writeErrorResponse(HttpServletResponse response,
      int status,
      String code,
      String message,
      Map<String, Object> details) throws IOException {
    response.setStatus(status);
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    ErrorResponse errorResponse = ErrorResponse.builder()
        .timestamp(Instant.now())
        .status(status)
        .code(code)
        .message(message)
        .details(details)
        .exception(null)
        .build();

    String json = objectMapper.writeValueAsString(errorResponse);
    response.getWriter().write(json);
  }
}

