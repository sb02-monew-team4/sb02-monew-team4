package com.team4.monew.controller;

import com.team4.monew.dto.UserActivity.UserActivityDto;
import com.team4.monew.service.UserActivityService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class UserActivityController {

  private final UserActivityService userActivityService;

  @GetMapping("/api/user-activities/{userId}")
  public ResponseEntity<UserActivityDto> getUserActivity(@PathVariable UUID userId) {
    log.info("GET /api/user-activities/{} 요청 - 사용자 활동 조회 시작", userId);
    UserActivityDto userActivity = userActivityService.getByUserId(userId);
    log.info("사용자 활동 조회 완료 - 사용자 ID: {}", userId);
    return ResponseEntity.ok(userActivity);
  }
}
