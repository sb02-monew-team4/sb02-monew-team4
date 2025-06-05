package com.team4.monew.controller;

import com.team4.monew.dto.UserActivity.UserActivityDto;
import com.team4.monew.service.UserActivityService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class UserActivityController {

  private final UserActivityService userActivityService;

  @GetMapping("/api/user-activities/{userId}")
  public ResponseEntity<UserActivityDto> getUserActivity(@PathVariable UUID userId) {
    return ResponseEntity.ok(userActivityService.getByUserId(userId));
  }
}
