package com.team4.monew.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String requestId = UUID.randomUUID().toString();
    String clientIp = getClientIp(request);

    MDC.put("requestId", requestId);
    MDC.put("clientIp", clientIp);

    long startTime = System.currentTimeMillis();

    try {
      filterChain.doFilter(request, response);
    } finally {
      long duration = System.currentTimeMillis() - startTime;

      response.addHeader("X-Request-ID", requestId);
      response.addHeader("X-Client-IP", clientIp);

      log.info("[{}] [{}] {} {} - {}ms",
          request.getMethod(),
          request.getRequestURI(),
          clientIp,
          request.getHeader("User-Agent"),
          duration);

      MDC.clear();// 메모리 누수 방지
    }
  }

  private String getClientIp(HttpServletRequest request) {
    String xf = request.getHeader("X-Forwarded-For");
    return (xf != null) ? xf.split(",")[0].trim() : request.getRemoteAddr();
  }
}
