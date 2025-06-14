package com.team4.monew.service.s3;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.team4.monew.entity.Article;
import com.team4.monew.exception.article.ArticleS3BackupException;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleS3Service {

  private static final ZoneId KST_ZONE = ZoneId.of("Asia/Seoul");
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
  private final S3Client s3Client;
  private final ObjectMapper objectMapper;
  @Value("${backup.s3.bucket}")
  private String bucketName;

  public void uploadToS3(List<Article> articles) {
    if (articles.isEmpty()) {
      log.info("백업할 기사가 없습니다.");
      return;
    }

    try {
      // JSON 직렬화를 위한 ObejctMapper 설정
      objectMapper.registerModule(new JavaTimeModule());

      String jsonContent = objectMapper.writeValueAsString(articles);

      String s3Key = generateS3Key();

      PutObjectRequest putRequest = PutObjectRequest.builder()
          .bucket(bucketName)
          .key(s3Key)
          .contentType("application/json")
          .build();

      s3Client.putObject(putRequest, RequestBody.fromString(jsonContent));

      log.info("S3 백업 완료: {} ({} 개 기사)", s3Key, articles.size());

    } catch (Exception e) {
      log.error("S3 백업 실패", e);
      throw ArticleS3BackupException.backupFailed();
    }
  }

  public List<Article> getArticlesByDateRange(Instant from, Instant to) {
    try {
      log.info("Article 조회 시작: {} ~ {}", from, to);

      // 날짜 범위에 해당하는 모든 폴더 경로 생성
      List<String> folderPaths = generateFolderPathsKST(from, to);
      log.debug("검색할 폴더 경로: {}", folderPaths);

      // 각 폴더에서 JSON 파일들 찾기
      List<String> allObjectKeys = new ArrayList<>();
      for (String folderPath : folderPaths) {
        List<String> objectKeys = listObjectsInFolder(folderPath);
        allObjectKeys.addAll(objectKeys);
      }
      log.info("발견된 JSON 파일 수: {}", allObjectKeys.size());

      // 각 JSON 파일 다운로드 및 return List<Article>
      List<Article> allArticles = new ArrayList<>();
      for (String objectKey : allObjectKeys) {
        try {
          List<Article> articles = downloadAndParseArticleList(objectKey);
          allArticles.addAll(articles);
          log.debug("파일 {} 에서 {}개 Article 추가", objectKey, allArticles.size());
        } catch (Exception e) {
          log.warn("파일 처리 실패: {}, 오류: {}", objectKey, e.getMessage());
        }
      }

      log.info("총 {}개의 Article을 조회했습니다.", allArticles.size());
      return allArticles;
    } catch (Exception e) {
      log.error("Article 조회 중 오류 발생: {}", e.getMessage());
      throw ArticleS3BackupException.readFailed();
    }
  }

  private List<Article> downloadAndParseArticleList(String objectKey) throws IOException {
    try {
      log.debug("파일 다운로드 시작: {}", objectKey);

      GetObjectRequest getObjectRequest = GetObjectRequest.builder()
          .bucket(bucketName)
          .key(objectKey)
          .build();

      ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);

      // JSON 배열을 List<Article>로 변환
      TypeReference<List<Article>> typeRef = new TypeReference<List<Article>>() {
      };
      List<Article> articles = objectMapper.readValue(s3Object, typeRef);

      s3Object.close();
      log.debug("파일 {} 에서 {} 개 Article 파싱 완료", objectKey, articles.size());
      return articles;
    } catch (S3Exception e) {
      log.error("S3 객체 다운로드 실패: {}, 오루: {}", objectKey, e.getMessage());
      return new ArrayList<>();
    }
  }

  // articles/20250101/ ... articles/20250105/ <- 폴더 아래에 있는 모든 Article JSON 파일 조회
  // 파일명 패턴: articles/20250101/articles-yyyy-MM-dd-HH-mm-ss.json
  private List<String> listObjectsInFolder(String folderPrefix) {
    try {
      ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
          .bucket(bucketName)
          .prefix(folderPrefix)
          .build();

      ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);

      List<String> objectKeys = listResponse.contents().stream()
          .map(S3Object::key)
          .filter(
              key -> {
                return key.startsWith(folderPrefix) &&
                    key.endsWith(".json") &&
                    key.matches(".*articles-\\d{4}-\\d{2}-\\d{2}-\\d{2}-\\d{2}-\\d{2}\\.json$");
              })
          .sorted()
          .collect(Collectors.toList());

      log.debug("폴더 {} 에서 {}개 파일 발견", folderPrefix, objectKeys.size());
      return objectKeys;
    } catch (S3Exception e) {
      log.warn("폴더 조회 실패: {}, 오류: {}", folderPrefix, e.getMessage());
      return new ArrayList<>();
    }
  }

  // from, to 기간을 key 로 변환해서 return List<String> -> articles/20250101/ ... articles/20250105/
  private List<String> generateFolderPathsKST(Instant from, Instant to) {
    List<String> folderPaths = new ArrayList<>();

    // Instant를 KST 기준 LocalDate로 변환
    LocalDate fromDate = from.atZone(KST_ZONE).toLocalDate();
    LocalDate toDate = to.atZone(KST_ZONE).toLocalDate();

    log.debug("KST 기준 날짜 범위: {} ~ {}", fromDate, toDate);

    LocalDate currentDate = fromDate;
    while (!currentDate.isAfter(toDate)) {
      String dateString = currentDate.format(DATE_FORMATTER);
      String folderPath = "articles/" + dateString + "/";

      folderPaths.add(folderPath);
      currentDate = currentDate.plusDays(1);
    }

    return folderPaths;
  }


  private String generateS3Key() {
    LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    return String.format("articles/%s/articles-%s.json",
        now.format(formatter),
        now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")));
  }
}
