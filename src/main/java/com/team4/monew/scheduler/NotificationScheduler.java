package com.team4.monew.scheduler;

import com.team4.monew.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

  private final NotificationService notificationService;

  @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
  public void deleteOldConfirmedNotifications() {
    try {
      long deletedCnt = notificationService.deleteConfirmedNotificationsOlderThan7Days();
      log.info("Deleted {} confirmed notifications older than 7 days", deletedCnt);
    } catch (Exception e) {
      log.error("Error while deleting old confirmed notifications", e);
    }
  }

}
