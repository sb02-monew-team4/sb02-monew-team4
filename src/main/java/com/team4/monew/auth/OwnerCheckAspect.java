package com.team4.monew.auth;

import com.team4.monew.exception.user.UnauthorizedAccessException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OwnerCheckAspect {

  private final HttpServletRequest request;

  @Around("@annotation(com.team4.monew.auth.OwnerCheck)")
  public Object checkOwner(ProceedingJoinPoint joinPoint) throws Throwable {
    Object authenticatedUserIdObj = request.getAttribute("authenticatedUserId");

    if (authenticatedUserIdObj == null) {
      throw new IllegalStateException("Authenticated user ID not found in request. Check interceptor configuration.");
    }
    UUID authenticatedUserId = (UUID) authenticatedUserIdObj;

    // 메서드의 파라미터 중 UUID 타입인 userId 추출
    UUID targetUserId = Arrays.stream(joinPoint.getArgs())
        .filter(arg -> arg instanceof UUID)
        .map(arg -> (UUID) arg)
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("userId 파라미터가 필요합니다"));

    // 사용자 ID 비교
    if (!authenticatedUserId.equals(targetUserId)) {
      throw UnauthorizedAccessException.byUserId();
    }

    return joinPoint.proceed();
  }


}
