package com.team4.monew.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.team4.monew.entity.Article;
import com.team4.monew.exception.article.ArticleS3BackupException;
import com.team4.monew.service.s3.ArticleS3Service;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

@ExtendWith(MockitoExtension.class)
@DisplayName("ArticleS3Service 통합 단위 테스트")
class ArticleS3ServiceTest {

  private static final String TEST_BUCKET = "test-bucket";
  @Mock
  private S3Client s3Client;
  @Mock
  private ObjectMapper objectMapper;
  @InjectMocks
  private ArticleS3Service articleS3Service;
  private List<Article> testArticles;
  private Instant testFromDate;
  private Instant testToDate;

  @BeforeAll
  static void setUpAll() {
    System.setProperty("spring.profiles.active", "test");
  }

  @BeforeEach
  void setUp() {
    // bucketName 필드에 테스트 값 설정
    ReflectionTestUtils.setField(articleS3Service, "bucketName", TEST_BUCKET);

    // 테스트용 Article 객체 생성
    testArticles = Arrays.asList(
        new Article("source1", "http://test1.com", "title1",
            Instant.now(), "summary1"),
        new Article("source2", "http://test2.com", "title2",
            Instant.now(), "summary2")
    );

    // 테스트용 날짜 범위 설정
    testFromDate = Instant.parse("2025-01-01T00:00:00Z");
    testToDate = Instant.parse("2025-01-02T00:00:00Z");
  }

  @AfterEach
  void tearDown() {
    reset(s3Client, objectMapper);
  }

  @Nested
  @DisplayName("uploadToS3 메서드 테스트")
  class UploadToS3Tests {

    @Test
    @DisplayName("정상적인 파일 업로드 성공")
    void uploadToS3_Success() throws Exception {
      // Given
      String expectedJson = "[{\"title\":\"test\"}]";
      when(objectMapper.writeValueAsString(testArticles)).thenReturn(expectedJson);
      when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
          .thenReturn(PutObjectResponse.builder().build());

      // When
      articleS3Service.uploadToS3(testArticles);

      // Then
      ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(
          PutObjectRequest.class);
      ArgumentCaptor<RequestBody> bodyCaptor = ArgumentCaptor.forClass(RequestBody.class);

      verify(s3Client, times(1)).putObject(requestCaptor.capture(), bodyCaptor.capture());
      verify(objectMapper, times(1)).registerModule(any(JavaTimeModule.class));
      verify(objectMapper, times(1)).writeValueAsString(testArticles);

      PutObjectRequest capturedRequest = requestCaptor.getValue();
      assertEquals(TEST_BUCKET, capturedRequest.bucket());
      assertTrue(capturedRequest.key().startsWith("articles/"));
      assertTrue(capturedRequest.key().endsWith(".json"));
      assertEquals("application/json", capturedRequest.contentType());
    }

    @Test
    @DisplayName("빈 리스트 업로드 시 로그만 출력하고 종료")
    void uploadToS3_EmptyList() throws JsonProcessingException {
      // Given
      List<Article> emptyList = Collections.emptyList();

      // When
      articleS3Service.uploadToS3(emptyList);

      // Then
      verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
      verify(objectMapper, never()).writeValueAsString(any());
    }

    // 수정: IOException 대신 JsonProcessingException 사용
    @Test
    @DisplayName("ObjectMapper JSON 직렬화 실패 시 예외 발생")
    void uploadToS3_JsonSerializationFailed() throws Exception {
      // Given
      when(objectMapper.writeValueAsString(testArticles))
          .thenThrow(new JsonProcessingException("JSON 직렬화 실패") {
          });

      // When & Then
      ArticleS3BackupException exception = assertThrows(ArticleS3BackupException.class,
          () -> articleS3Service.uploadToS3(testArticles));

      verify(objectMapper, times(1)).registerModule(any(JavaTimeModule.class));
      verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    @DisplayName("S3 업로드 실패 시 예외 발생")
    void uploadToS3_S3UploadFailed() throws Exception {
      // Given
      when(objectMapper.writeValueAsString(testArticles)).thenReturn("test");
      when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
          .thenThrow(S3Exception.builder().message("S3 Upload Error").build());

      // When & Then
      ArticleS3BackupException exception = assertThrows(ArticleS3BackupException.class,
          () -> articleS3Service.uploadToS3(testArticles));

      verify(objectMapper, times(1)).writeValueAsString(testArticles);
      verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }
  }

  @Nested
  @DisplayName("getArticlesByDateRange 메서드 테스트")
  class GetArticlesByDateRangeTests {

    @Test
    @DisplayName("날짜 범위로 Article 조회 성공")
    void getArticlesByDateRange_Success() throws Exception {
      // Given
      List<S3Object> mockS3Objects = Arrays.asList(
          S3Object.builder().key("articles/20250101/articles-2025-01-01-10-00-00.json").build(),
          S3Object.builder().key("articles/20250102/articles-2025-01-02-10-00-00.json").build()
      );

      ListObjectsV2Response mockResponse = ListObjectsV2Response.builder()
          .contents(mockS3Objects)
          .build();

      when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
          .thenReturn(mockResponse);

      // Mock S3 객체 다운로드
      ResponseInputStream<GetObjectResponse> mockInputStream =
          new ResponseInputStream<>(
              GetObjectResponse.builder().build(),
              new ByteArrayInputStream("[]".getBytes())
          );

      when(s3Client.getObject(any(GetObjectRequest.class)))
          .thenReturn(mockInputStream);

      when(objectMapper.readValue(any(ResponseInputStream.class),
          any(TypeReference.class)))
          .thenReturn(testArticles);

      // When
      List<Article> result = articleS3Service.getArticlesByDateRange(testFromDate, testToDate);

      // Then
      assertNotNull(result);
      assertEquals(4, result.size()); // 2개 파일 × 2개 Article
      verify(s3Client, times(2)).listObjectsV2(any(ListObjectsV2Request.class));
      verify(s3Client, times(2)).getObject(any(GetObjectRequest.class));
    }

    // 수정: 실제 구현에서는 예외를 던지지 않고 빈 리스트를 반환하므로 테스트 수정
    @Test
    @DisplayName("S3 객체 목록 조회 실패 시 빈 리스트 반환")
    void getArticlesByDateRange_ListObjectsFailed() {
      // Given
      when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
          .thenThrow(S3Exception.builder().message("List Objects Error").build());

      // When
      List<Article> result = articleS3Service.getArticlesByDateRange(testFromDate, testToDate);

      // Then
      assertNotNull(result);
      assertTrue(result.isEmpty()); // 예외 대신 빈 리스트 반환
    }

    @Test
    @DisplayName("파일 다운로드 실패 시 해당 파일 스킵하고 계속 진행")
    void getArticlesByDateRange_PartialDownloadFailure() throws Exception {
      // Given
      List<S3Object> mockS3Objects = Arrays.asList(
          S3Object.builder().key("articles/20250101/articles-2025-01-01-10-00-00.json").build(),
          S3Object.builder().key("articles/20250102/articles-2025-01-02-10-00-00.json").build()
      );

      ListObjectsV2Response mockResponse = ListObjectsV2Response.builder()
          .contents(mockS3Objects)
          .build();

      when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
          .thenReturn(mockResponse);

      // 첫 번째 파일은 성공, 두 번째 파일은 실패
      ResponseInputStream<GetObjectResponse> successInputStream =
          new ResponseInputStream<>(
              GetObjectResponse.builder().build(),
              new ByteArrayInputStream("[]".getBytes())
          );

      when(s3Client.getObject(any(GetObjectRequest.class)))
          .thenReturn(successInputStream)
          .thenThrow(S3Exception.builder().message("Download Error").build());

      when(objectMapper.readValue(any(ResponseInputStream.class),
          any(TypeReference.class)))
          .thenReturn(testArticles);

      // When
      List<Article> result = articleS3Service.getArticlesByDateRange(testFromDate, testToDate);

      // Then
      assertNotNull(result);
      assertEquals(2, result.size()); // 성공한 파일에서만 Article 조회
      verify(s3Client, times(2)).getObject(any(GetObjectRequest.class));
    }

    @Test
    @DisplayName("빈 결과 조회 시 빈 리스트 반환")
    void getArticlesByDateRange_EmptyResult() {
      // Given
      ListObjectsV2Response emptyResponse = ListObjectsV2Response.builder()
          .contents(Collections.emptyList())
          .build();

      when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
          .thenReturn(emptyResponse);

      // When
      List<Article> result = articleS3Service.getArticlesByDateRange(testFromDate, testToDate);

      // Then
      assertNotNull(result);
      assertTrue(result.isEmpty());
      verify(s3Client, times(2)).listObjectsV2(any(ListObjectsV2Request.class));
      verify(s3Client, never()).getObject(any(GetObjectRequest.class));
    }

    @Test
    @DisplayName("잘못된 파일명 패턴 필터링 테스트")
    void getArticlesByDateRange_FilterInvalidFileNames() throws Exception {
      // Given
      List<S3Object> mockS3Objects = Arrays.asList(
          S3Object.builder().key("articles/20250101/articles-2025-01-01-10-00-00.json").build(),
          // 유효
          S3Object.builder().key("articles/20250101/invalid-file.txt").build(), // 무효
          S3Object.builder().key("articles/20250101/articles-invalid-format.json").build(), // 무효
          S3Object.builder().key("articles/20250102/articles-2025-01-02-15-30-45.json").build()
          // 유효
      );

      ListObjectsV2Response mockResponse = ListObjectsV2Response.builder()
          .contents(mockS3Objects)
          .build();

      when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
          .thenReturn(mockResponse);

      ResponseInputStream<GetObjectResponse> mockInputStream =
          new ResponseInputStream<>(
              GetObjectResponse.builder().build(),
              new ByteArrayInputStream("[]".getBytes())
          );

      when(s3Client.getObject(any(GetObjectRequest.class)))
          .thenReturn(mockInputStream);

      when(objectMapper.readValue(any(ResponseInputStream.class),
          any(TypeReference.class)))
          .thenReturn(testArticles);

      // When
      List<Article> result = articleS3Service.getArticlesByDateRange(testFromDate, testToDate);

      // Then
      assertNotNull(result);
      assertEquals(4, result.size()); // 유효한 2개 파일 × 2개 Article
      verify(s3Client, times(2)).getObject(any(GetObjectRequest.class)); // 유효한 파일만 다운로드
    }
  }

  @Nested
  @DisplayName("Private 메서드 간접 테스트")
  class PrivateMethodTests {

    @Test
    @DisplayName("S3 키 생성 패턴 검증")
    void generateS3Key_PatternValidation() throws Exception {
      // Given
      String expectedJson = "test";
      when(objectMapper.writeValueAsString(testArticles)).thenReturn(expectedJson);
      when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
          .thenReturn(PutObjectResponse.builder().build());

      // When
      articleS3Service.uploadToS3(testArticles);

      // Then
      ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(
          PutObjectRequest.class);
      verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));

      String capturedKey = requestCaptor.getValue().key();

      // S3 키 패턴 검증: articles/yyyyMMdd/articles-yyyy-MM-dd-HH-mm-ss.json
      assertTrue(capturedKey.matches(
          "articles/\\d{8}/articles-\\d{4}-\\d{2}-\\d{2}-\\d{2}-\\d{2}-\\d{2}\\.json"));
    }

    // 수정: 실제 KST 변환 로직에 맞게 호출 횟수 조정
    @Test
    @DisplayName("KST 시간대 기반 폴더 경로 생성 검증")
    void generateFolderPathsKST_Validation() {
      // Given - KST 기준으로 2일간의 범위 설정
      Instant from = Instant.parse("2025-01-01T00:00:00Z"); // UTC 기준
      Instant to = Instant.parse("2025-01-02T00:00:00Z");   // UTC 기준

      ListObjectsV2Response mockResponse = ListObjectsV2Response.builder()
          .contents(Collections.emptyList())
          .build();

      when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
          .thenReturn(mockResponse);

      // When
      articleS3Service.getArticlesByDateRange(from, to);

      // Then
      ArgumentCaptor<ListObjectsV2Request> requestCaptor =
          ArgumentCaptor.forClass(ListObjectsV2Request.class);
      verify(s3Client, times(2)).listObjectsV2(requestCaptor.capture()); // 실제로는 2일간

      List<ListObjectsV2Request> capturedRequests = requestCaptor.getAllValues();
      assertEquals("articles/20250101/", capturedRequests.get(0).prefix());
      assertEquals("articles/20250102/", capturedRequests.get(1).prefix());
    }
  }

  @Nested
  @DisplayName("경계값 및 예외 상황 테스트")
  class EdgeCaseTests {

    @Test
    @DisplayName("동일한 날짜 범위 조회")
    void getArticlesByDateRange_SameDate() {
      // Given
      Instant sameDate = Instant.parse("2025-01-01T10:00:00Z");

      ListObjectsV2Response mockResponse = ListObjectsV2Response.builder()
          .contents(Collections.emptyList())
          .build();

      when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
          .thenReturn(mockResponse);

      // When
      List<Article> result = articleS3Service.getArticlesByDateRange(sameDate, sameDate);

      // Then
      assertNotNull(result);
      assertTrue(result.isEmpty());
      verify(s3Client, times(1)).listObjectsV2(any(ListObjectsV2Request.class));
    }

    @Test
    @DisplayName("null Article 리스트 업로드 시 NullPointerException")
    void uploadToS3_NullList() {
      // When & Then
      assertThrows(NullPointerException.class,
          () -> articleS3Service.uploadToS3(null));
    }

    @Test
    @DisplayName("JSON 파싱 실패 시 빈 리스트 반환")
    void downloadAndParseArticleList_JsonParsingFailed() throws Exception {
      // Given
      List<S3Object> mockS3Objects = Arrays.asList(
          S3Object.builder().key("articles/20250101/articles-2025-01-01-10-00-00.json").build()
      );

      ListObjectsV2Response mockResponse = ListObjectsV2Response.builder()
          .contents(mockS3Objects)
          .build();

      when(s3Client.listObjectsV2(any(ListObjectsV2Request.class)))
          .thenReturn(mockResponse);

      ResponseInputStream<GetObjectResponse> mockInputStream =
          new ResponseInputStream<>(
              GetObjectResponse.builder().build(),
              new ByteArrayInputStream("invalid json".getBytes())
          );

      when(s3Client.getObject(any(GetObjectRequest.class)))
          .thenReturn(mockInputStream);

      when(objectMapper.readValue(any(ResponseInputStream.class),
          any(TypeReference.class)))
          .thenThrow(new IOException("JSON 파싱 실패"));

      // When
      List<Article> result = articleS3Service.getArticlesByDateRange(testFromDate, testToDate);

      // Then
      assertNotNull(result);
      assertTrue(result.isEmpty()); // 파싱 실패 시 빈 리스트 반환
    }
  }
}
