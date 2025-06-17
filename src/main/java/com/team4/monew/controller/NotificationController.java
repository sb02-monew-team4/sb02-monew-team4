package com.team4.monew.controller;

import com.team4.monew.dto.notifications.CursorPageResponseNotificationDto;
import com.team4.monew.entity.Notification;
import com.team4.monew.service.NotificationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

  private final NotificationService notificationService;

  @GetMapping
  public ResponseEntity<CursorPageResponseNotificationDto> findUnconfirmed(
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) String after,
      @RequestParam int limit,
      @RequestHeader("Monew-Request-User-ID") UUID userId
  ){
    CursorPageResponseNotificationDto unconfirmedNotifications = notificationService.findUnconfirmedByCursor(
        cursor, after, limit, userId
    );
    return ResponseEntity.ok(unconfirmedNotifications);
  }

  @PatchMapping
  public ResponseEntity<Void> updateAll(
      @RequestHeader("Monew-Request-User-ID") UUID userId
  ) {
    List<Notification> updatedNotifications = notificationService.updateAll(userId);
    log.debug("Updated notifications: {}", updatedNotifications);
    return ResponseEntity.ok().build();
  }

  @PatchMapping("/{notificationId}")
  public ResponseEntity<Void> update(
      @PathVariable("notificationId") UUID notificationId,
      @RequestHeader("Monew-Request-User-ID") UUID userId
  ) {
    Notification updatedNotification = notificationService.update(notificationId, userId);
    log.debug("Updated notification: {}", updatedNotification);
    return ResponseEntity.ok().build();
  }

}
