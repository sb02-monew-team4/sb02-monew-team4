package com.team4.monew.logging;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogUploaderService {

  private final S3Client s3Client;
  private final String LOG_DIR = "logs";
  @Value("${logging.s3.bucket}")
  private String BUCKET_NAME;

  @Scheduled(cron = "0 0 0 * * *")
  public void uploadYesterdayLog() {
    LocalDate yesterday = LocalDate.now().minusDays(1);
    String dateStr = yesterday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    String fileName = String.format("app-%s.log", dateStr);
    File logFile = new File(LOG_DIR, fileName);

    if (!logFile.exists()) {
      log.warn("Log file not found: {}", logFile.getAbsolutePath());
      return;
    }

    String s3Key = String.format("logs/%s/%s", dateStr, fileName);

    try {
      s3Client.putObject(
          PutObjectRequest.builder()
              .bucket(BUCKET_NAME)
              .key(s3Key)
              .build(),
          RequestBody.fromFile(logFile)
      );
      log.info("Uploaded log to S3: {}", s3Key);
    } catch (Exception e) {
      log.error("Failed to upload log to S3", e);
    }
  }
}