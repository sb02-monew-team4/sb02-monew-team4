package com.team4.monew.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.team4.monew.asynchronous.event.article.ArticleCreatedEventForNotification;
import com.team4.monew.entity.Article;
import com.team4.monew.entity.Interest;
import com.team4.monew.entity.Subscription;
import com.team4.monew.entity.User;
import com.team4.monew.repository.ArticleRepository;
import com.team4.monew.repository.SubscriptionRepository;
import com.team4.monew.service.collector.NaverApiCollectorService;
import com.team4.monew.service.collector.RssCollectorService;
import com.team4.monew.service.filter.KeywordFilterService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ArticleSchedulerTest {

  @Mock
  private RssCollectorService rssCollectorService;

  @Mock
  private NaverApiCollectorService naverApiCollectorService;

  @Mock
  private KeywordFilterService keywordFilterService;

  @Mock
  private ArticleRepository articleRepository;

  @Mock
  private SubscriptionRepository subscriptionRepository;

  @Mock
  private ApplicationEventPublisher eventPublisher;

  @Captor
  private ArgumentCaptor<ArticleCreatedEventForNotification> eventCaptor;

  @InjectMocks
  private ArticleScheduler articleScheduler;

  private List<Article> testRssArticles;
  private List<Article> testNaverArticles;
  private List<Article> testFilteredArticles;

  private User user1;
  private User user2;
  private Interest interest;

  @BeforeEach
  void setUp() {
    // 테스트용 RSS 기사 데이터 생성
    testRssArticles = Arrays.asList(
        createTestArticle("RSS_SOURCE_1", "https://example.com/rss1", "RSS 기사 제목 1"),
        createTestArticle("RSS_SOURCE_2", "https://example.com/rss2", "RSS 기사 제목 2")
    );

    // 테스트용 네이버 기사 데이터 생성
    testNaverArticles = Arrays.asList(
        createTestArticle("NAVER", "https://example.com/naver1", "네이버 기사 제목 1"),
        createTestArticle("NAVER", "https://example.com/naver2", "네이버 기사 제목 2")
    );

    // 테스트용 필터링된 기사 데이터 생성
    testFilteredArticles = Arrays.asList(
        createTestArticle("RSS_SOURCE_1", "https://example.com/filtered1", "필터링된 기사 제목")
    );
  }

  private Article createTestArticle(String source, String link, String title) {
    return new Article(source, link, title, Instant.now(), "테스트 기사 요약");
  }

  private void setUpForPublishEvent(List<Article> articles){
    user1 = User.create("test1@email.com", "user1", "pw");
    ReflectionTestUtils.setField(user1, "id", UUID.randomUUID());
    user2 = User.create("test2@email.com", "user2", "pw");
    ReflectionTestUtils.setField(user2, "id", UUID.randomUUID());

    Subscription subscription1 = mock(Subscription.class);
    Subscription subscription2 = mock(Subscription.class);

    interest = new Interest(UUID.randomUUID(), "IT", 2L, Instant.now(), Instant.now(), List.of(), List.of(subscription1, subscription2), new HashSet<>(articles));
    for (Article article : articles) {
      if (!article.getInterest().contains(interest)) {
        article.addInterest(interest);
      }
    }

    when(subscription1.getUser()).thenReturn(user1);
    when(subscription2.getUser()).thenReturn(user2);

    when(subscriptionRepository.findByInterest(interest)).thenReturn(Arrays.asList(subscription1,
        subscription2));
    when(articleRepository.save(any(Article.class))).thenAnswer(invocation -> invocation.getArgument(0));
  }

  private void verifyPublishEvent(List<Article> articles){
    verify(eventPublisher, times(2)).publishEvent(eventCaptor.capture());

    List<ArticleCreatedEventForNotification> capturedEvents = eventCaptor.getAllValues();
    assertEquals(2, capturedEvents.size());
    assertTrue(capturedEvents.stream().anyMatch(e -> e.subscriberId().equals(user1.getId())));
    assertTrue(capturedEvents.stream().anyMatch(e -> e.subscriberId().equals(user2.getId())));

    ArticleCreatedEventForNotification firstEvent = capturedEvents.get(0);
    assertEquals(interest.getId(), firstEvent.interestId());
    assertEquals(interest.getName(), firstEvent.interestName());
    assertEquals(articles.size(), firstEvent.articleCount());
  }

  @Test
  @DisplayName("정상적인 기사 수집 및 처리 플로우 검증")
  void testHourlyArticleProcessing_NormalFlow() {
    // Given
    when(rssCollectorService.collectFromAllSources()).thenReturn(testRssArticles);
    when(naverApiCollectorService.collectArticles()).thenReturn(testNaverArticles);
    when(keywordFilterService.filterArticles(testRssArticles)).thenReturn(testFilteredArticles);
    when(articleRepository.existsByOriginalLink(anyString())).thenReturn(false);

    // 이벤트 발행 시 필요한 데이터 세팅 - 관심사, 구독자
    List<Article> totalArticles = new ArrayList<>();
    totalArticles.addAll(testFilteredArticles);
    totalArticles.addAll(testNaverArticles);
    setUpForPublishEvent(totalArticles);

    // When
    articleScheduler.hourlyArticleProcessing();

    // Then - 서비스 호출 순서 및 횟수 검증
    verify(rssCollectorService, times(1)).collectFromAllSources();
    verify(naverApiCollectorService, times(1)).collectArticles();
    verify(keywordFilterService, times(1)).filterArticles(testRssArticles);

    // 저장 로직 검증
    verify(articleRepository, times(3)).existsByOriginalLink(anyString());
    verify(articleRepository, times(3)).save(any(Article.class));

    // 이벤트 발행 검증
    verifyPublishEvent(totalArticles);
  }

  @Test
  @DisplayName("RSS 수집 서비스 상호작용 검증")
  void testRssCollectionInteraction() {
    // Given
    when(rssCollectorService.collectFromAllSources()).thenReturn(testRssArticles);
    when(naverApiCollectorService.collectArticles()).thenReturn(Collections.emptyList());
    when(keywordFilterService.filterArticles(testRssArticles)).thenReturn(testFilteredArticles);
    when(articleRepository.existsByOriginalLink(anyString())).thenReturn(false);

    // 이벤트 발행 시 필요한 데이터 세팅 - 관심사, 구독자
    setUpForPublishEvent(testFilteredArticles);

    // When
    articleScheduler.hourlyArticleProcessing();

    // Then
    verify(rssCollectorService, times(1)).collectFromAllSources();
    verify(keywordFilterService, times(1)).filterArticles(testRssArticles);

    // 필터링된 기사들이 저장되는지 확인
    for (Article article : testFilteredArticles) {
      verify(articleRepository, times(1)).existsByOriginalLink(article.getOriginalLink());
      verify(articleRepository, times(1)).save(article);
    }

    // 이벤트 발행 검증
    verifyPublishEvent(testFilteredArticles);
  }

  @Test
  @DisplayName("네이버 API 수집 서비스 상호작용 검증")
  void testNaverApiCollectionInteraction() {
    // Given
    when(rssCollectorService.collectFromAllSources()).thenReturn(Collections.emptyList());
    when(naverApiCollectorService.collectArticles()).thenReturn(testNaverArticles);
    when(keywordFilterService.filterArticles(Collections.emptyList())).thenReturn(
        Collections.emptyList());
    when(articleRepository.existsByOriginalLink(anyString())).thenReturn(false);

    // 이벤트 발행 시 필요한 데이터 세팅 - 관심사, 구독자
    setUpForPublishEvent(testNaverArticles);

    // When
    articleScheduler.hourlyArticleProcessing();

    // Then
    verify(naverApiCollectorService, times(1)).collectArticles();

    // 네이버 기사들이 직접 저장되는지 확인 (필터링 없이)
    for (Article article : testNaverArticles) {
      verify(articleRepository, times(1)).existsByOriginalLink(article.getOriginalLink());
      verify(articleRepository, times(1)).save(article);
    }

    // 이벤트 발행 검증
    verifyPublishEvent(testNaverArticles);
  }

  @Test
  @DisplayName("키워드 필터링 서비스 상호작용 검증")
  void testKeywordFilteringInteraction() {
    // Given
    List<Article> largeRssCollection = Arrays.asList(
        createTestArticle("RSS1", "https://test1.com", "제목1"),
        createTestArticle("RSS2", "https://test2.com", "제목2"),
        createTestArticle("RSS3", "https://test3.com", "제목3")
    );

    List<Article> filteredResult = Arrays.asList(
        createTestArticle("RSS1", "https://test1.com", "제목1") // 하나만 필터링 통과
    );

    when(rssCollectorService.collectFromAllSources()).thenReturn(largeRssCollection);
    when(naverApiCollectorService.collectArticles()).thenReturn(Collections.emptyList());
    when(keywordFilterService.filterArticles(largeRssCollection)).thenReturn(filteredResult);
    when(articleRepository.existsByOriginalLink(anyString())).thenReturn(false);

    // 이벤트 발행 시 필요한 데이터 세팅 - 관심사, 구독자
    setUpForPublishEvent(filteredResult);

    // When
    articleScheduler.hourlyArticleProcessing();

    // Then
    verify(keywordFilterService, times(1)).filterArticles(largeRssCollection);

    // 필터링된 결과만 저장되는지 확인
    verify(articleRepository, times(1)).existsByOriginalLink(
        filteredResult.get(0).getOriginalLink());
    verify(articleRepository, times(1)).save(filteredResult.get(0));
    verify(articleRepository, times(1)).save(any(Article.class)); // 총 1개만 저장

    // 이벤트 발행 검증
    verifyPublishEvent(filteredResult);
  }

  @Test
  @DisplayName("중복 기사 처리 로직 검증")
  void testDuplicateArticleHandling() {
    // Given
    when(rssCollectorService.collectFromAllSources()).thenReturn(Collections.emptyList());
    when(naverApiCollectorService.collectArticles()).thenReturn(testNaverArticles);
    when(keywordFilterService.filterArticles(Collections.emptyList())).thenReturn(
        Collections.emptyList());

    // 첫 번째 기사는 중복, 두 번째는 새로운 기사
    when(articleRepository.existsByOriginalLink(
        testNaverArticles.get(0).getOriginalLink())).thenReturn(true);
    when(articleRepository.existsByOriginalLink(
        testNaverArticles.get(1).getOriginalLink())).thenReturn(false);

    // 이벤트 발행 시 필요한 데이터 세팅 - 관심사, 구독자
    setUpForPublishEvent(List.of(testNaverArticles.get(1)));

    // When
    articleScheduler.hourlyArticleProcessing();

    // Then
    verify(articleRepository, times(2)).existsByOriginalLink(anyString());
    verify(articleRepository, never()).save(testNaverArticles.get(0)); // 중복 기사는 저장되지 않음
    verify(articleRepository, times(1)).save(testNaverArticles.get(1)); // 새로운 기사만 저장됨

    // 이벤트 발행 검증
    verifyPublishEvent(List.of(testNaverArticles.get(1)));
  }

  @Test
  @DisplayName("빈 컬렉션 처리 검증")
  void testEmptyCollectionHandling() {
    // Given
    when(rssCollectorService.collectFromAllSources()).thenReturn(Collections.emptyList());
    when(naverApiCollectorService.collectArticles()).thenReturn(Collections.emptyList());
    when(keywordFilterService.filterArticles(Collections.emptyList())).thenReturn(
        Collections.emptyList());

    // When
    articleScheduler.hourlyArticleProcessing();

    // Then
    verify(rssCollectorService, times(1)).collectFromAllSources();
    verify(naverApiCollectorService, times(1)).collectArticles();
    verify(keywordFilterService, times(1)).filterArticles(Collections.emptyList());

    // 저장 관련 메서드는 호출되지 않아야 함
    verify(articleRepository, never()).existsByOriginalLink(anyString());
    verify(articleRepository, never()).save(any(Article.class));
    // 이벤트 발행되지 않아야 함
    verify(eventPublisher, never()).publishEvent(eventCaptor.capture());
  }

  @Test
  @DisplayName("여러 기사 저장 과정에서 중복 체크 순서 검증")
  void testMultipleArticleSaveOrder() {
    // Given
    List<Article> mixedArticles = Arrays.asList(
        createTestArticle("SOURCE1", "https://new1.com", "새 기사 1"),
        createTestArticle("SOURCE2", "https://duplicate.com", "중복 기사"),
        createTestArticle("SOURCE3", "https://new2.com", "새 기사 2")
    );

    when(rssCollectorService.collectFromAllSources()).thenReturn(Collections.emptyList());
    when(naverApiCollectorService.collectArticles()).thenReturn(mixedArticles);
    when(keywordFilterService.filterArticles(Collections.emptyList())).thenReturn(
        Collections.emptyList());

    when(articleRepository.existsByOriginalLink("https://new1.com")).thenReturn(false);
    when(articleRepository.existsByOriginalLink("https://duplicate.com")).thenReturn(true);
    when(articleRepository.existsByOriginalLink("https://new2.com")).thenReturn(false);

    // 이벤트 발행 시 필요한 데이터 세팅 - 관심사, 구독자
    setUpForPublishEvent(List.of(mixedArticles.get(0), mixedArticles.get(2)));

    // When
    articleScheduler.hourlyArticleProcessing();

    // Then - 호출 순서 검증
    verify(articleRepository, times(1)).existsByOriginalLink("https://new1.com");
    verify(articleRepository, times(1)).existsByOriginalLink("https://duplicate.com");
    verify(articleRepository, times(1)).existsByOriginalLink("https://new2.com");

    // 저장 결과 검증
    verify(articleRepository, times(1)).save(mixedArticles.get(0)); // 첫 번째 새 기사
    verify(articleRepository, never()).save(mixedArticles.get(1));  // 중복 기사는 저장 안 됨
    verify(articleRepository, times(1)).save(mixedArticles.get(2)); // 세 번째 새 기사

    // 이벤트 발행 검증
    verifyPublishEvent(List.of(mixedArticles.get(0), mixedArticles.get(2)));
  }

  @Test
  @DisplayName("모든 의존성 서비스 호출 검증 - 실행 순서 확인")
  void testAllDependencyServicesCalled() {
    // Given
    when(rssCollectorService.collectFromAllSources()).thenReturn(testRssArticles);
    when(naverApiCollectorService.collectArticles()).thenReturn(testNaverArticles);
    when(keywordFilterService.filterArticles(any())).thenReturn(testFilteredArticles);
    when(articleRepository.existsByOriginalLink(anyString())).thenReturn(false);

    // 이벤트 발행 시 필요한 데이터 세팅 - 관심사, 구독자
    List<Article> totalArticles = new ArrayList<>();
    totalArticles.addAll(testFilteredArticles);
    totalArticles.addAll(testNaverArticles);
    setUpForPublishEvent(totalArticles);

    // When
    articleScheduler.hourlyArticleProcessing();

    // Then - 모든 의존성 서비스가 호출되었는지 확인
    verify(rssCollectorService, times(1)).collectFromAllSources();
    verify(naverApiCollectorService, times(1)).collectArticles();
    verify(keywordFilterService, times(1)).filterArticles(testRssArticles);
    verify(articleRepository, atLeastOnce()).existsByOriginalLink(anyString());
    verify(articleRepository, atLeastOnce()).save(any(Article.class));

    // 추가 상호작용이 없는지 확인
    verifyNoMoreInteractions(rssCollectorService);
    verifyNoMoreInteractions(naverApiCollectorService);
    verifyNoMoreInteractions(keywordFilterService);

    // 이벤트 발행 검증
    verifyPublishEvent(totalArticles);
  }
}
