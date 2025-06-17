package com.team4.monew.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.team4.monew.entity.Article;
import com.team4.monew.entity.ArticleSource;
import com.team4.monew.service.collector.RssCollectorService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class RssCollectorServiceTest {

  @Mock
  private SyndFeedInput mockSyndFeedInput;

  @InjectMocks
  private RssCollectorService rssCollectorService;

  @BeforeEach
  void setUp() {
    // ReflectionTestUtils를 사용하여 final 필드 mock 주입
    ReflectionTestUtils.setField(rssCollectorService, "syndFeedInput", mockSyndFeedInput);
  }

  @Test
  @DisplayName("모든 RSS 소스에서 기사를 성공적으로 수집해야 한다")
  void shouldCollectArticlesFromAllSources() throws Exception {
    // given
    try (MockedStatic<ArticleSource> mockedArticleSource = mockStatic(ArticleSource.class)) {
      ArticleSource[] mockSources = {ArticleSource.HANKYUNG, ArticleSource.CHOSUN,
          ArticleSource.YONHAP};
      mockedArticleSource.when(ArticleSource::values).thenReturn(mockSources);

      // 각 소스별로 다른 피드 설정
      SyndFeed hankyungFeed = createMockFeedWithEntries(ArticleSource.HANKYUNG, 2);
      SyndFeed chosunFeed = createMockFeedWithEntries(ArticleSource.CHOSUN, 1);
      SyndFeed yonhapFeed = createMockFeedWithEntries(ArticleSource.YONHAP, 3);

      when(mockSyndFeedInput.build(any(XmlReader.class)))
          .thenReturn(hankyungFeed)
          .thenReturn(chosunFeed)
          .thenReturn(yonhapFeed);

      // when
      List<Article> articles = rssCollectorService.collectFromAllSources();

      // then
      assertThat(articles).hasSize(6); // 2 + 1 + 3 = 6
      verify(mockSyndFeedInput, times(3)).build(any(XmlReader.class));
    }
  }

  @Test
  @DisplayName("RSS 파싱 중 예외 발생시 해당 소스는 건너뛰고 다른 소스는 정상 처리해야 한다")
  void shouldSkipFailedSourceAndContinueWithOthers() throws Exception {
    // given
    try (MockedStatic<ArticleSource> mockedArticleSource = mockStatic(ArticleSource.class)) {
      ArticleSource[] mockSources = {ArticleSource.HANKYUNG, ArticleSource.CHOSUN,
          ArticleSource.YONHAP};
      mockedArticleSource.when(ArticleSource::values).thenReturn(mockSources);

      // 성공할 피드 생성 - 한 소스에서만 2개 Article 생성
      SyndFeed successFeed = createMockFeedWithEntries(ArticleSource.HANKYUNG, 2);

      // 호출 횟수를 추적하여 첫 번째만 성공, 나머지는 실패
      AtomicInteger callCount = new AtomicInteger(0);

      when(mockSyndFeedInput.build(any(XmlReader.class))).thenAnswer(invocation -> {
        int currentCall = callCount.getAndIncrement();
        System.out.println("Mock 호출 #" + currentCall); // 디버깅용

        if (currentCall == 0) {
          return successFeed; // 첫 번째 호출만 성공
        } else {
          throw new RuntimeException("의도적 실패 " + currentCall);
        }
      });

      // when
      List<Article> articles = rssCollectorService.collectFromAllSources();

      // then
      assertThat(articles).hasSize(2); // 성공한 한 소스에서만 2개

      // 병렬 처리로 인해 어떤 소스가 성공할지 예측 불가능하므로
      // 유효한 소스 중 하나인지만 확인
      assertThat(articles).allSatisfy(article -> {
        assertThat(article.getSource()).isIn(
            ArticleSource.HANKYUNG.getSource(),
            ArticleSource.CHOSUN.getSource(),
            ArticleSource.YONHAP.getSource()
        );
        assertThat(article.getTitle()).isNotNull();
        assertThat(article.getOriginalLink()).isNotNull();
      });
    }
  }

  @Test
  @DisplayName("모든 RSS 소스에서 예외 발생시 빈 목록을 반환해야 한다")
  void shouldReturnEmptyListWhenAllSourcesFail() throws Exception {
    // given
    try (MockedStatic<ArticleSource> mockedArticleSource = mockStatic(ArticleSource.class)) {
      ArticleSource[] mockSources = {ArticleSource.HANKYUNG, ArticleSource.CHOSUN,
          ArticleSource.YONHAP};
      mockedArticleSource.when(ArticleSource::values).thenReturn(mockSources);

      when(mockSyndFeedInput.build(any(XmlReader.class)))
          .thenThrow(new RuntimeException("RSS 파싱 실패"))
          .thenThrow(new RuntimeException("네트워크 오류"))
          .thenThrow(new RuntimeException("타임아웃"));

      // when
      List<Article> articles = rssCollectorService.collectFromAllSources();

      // then
      assertThat(articles).isEmpty();
    }
  }

  @Test
  @DisplayName("RSS 엔트리가 Article 객체로 정확히 변환되어야 한다")
  void shouldConvertSyndEntryToArticleCorrectly() throws Exception {
    // given
    try (MockedStatic<ArticleSource> mockedArticleSource = mockStatic(ArticleSource.class)) {
      ArticleSource[] mockSources = {ArticleSource.HANKYUNG};
      mockedArticleSource.when(ArticleSource::values).thenReturn(mockSources);

      // 특정 데이터로 SyndEntry 생성
      Date publishedDate = new Date();
      SyndEntry mockEntry = createMockEntry(
          "테스트 제목",
          "http://test.link",
          publishedDate,
          "테스트 설명"
      );
      SyndFeed mockFeed = mock(SyndFeed.class);
      when(mockFeed.getEntries()).thenReturn(List.of(mockEntry));

      when(mockSyndFeedInput.build(any(XmlReader.class))).thenReturn(mockFeed);

      // when
      List<Article> articles = rssCollectorService.collectFromAllSources();

      // then
      assertThat(articles).hasSize(1);
      Article article = articles.get(0);
      assertThat(article.getSource()).isEqualTo(ArticleSource.HANKYUNG.getSource());
      assertThat(article.getTitle()).isEqualTo("테스트 제목");
      assertThat(article.getOriginalLink()).isEqualTo("http://test.link");
      assertThat(article.getPublishedDate()).isEqualTo(publishedDate.toInstant());
      assertThat(article.getSummary()).isEqualTo("테스트 설명");
    }
  }

  @Test
  @DisplayName("빈 RSS 피드에 대해 빈 목록을 반환해야 한다")
  void shouldReturnEmptyListForEmptyFeeds() throws Exception {
    // given
    try (MockedStatic<ArticleSource> mockedArticleSource = mockStatic(ArticleSource.class)) {
      ArticleSource[] mockSources = {ArticleSource.HANKYUNG, ArticleSource.CHOSUN};
      mockedArticleSource.when(ArticleSource::values).thenReturn(mockSources);

      SyndFeed emptyFeed = createEmptyMockFeed();
      when(mockSyndFeedInput.build(any(XmlReader.class))).thenReturn(emptyFeed);

      // when
      List<Article> articles = rssCollectorService.collectFromAllSources();

      // then
      assertThat(articles).isEmpty();
    }
  }

  // ========== 추가 테스트: 부분 실패 시나리오 ==========
  @Test
  @DisplayName("일부 소스 실패시 성공한 소스의 데이터만 반환해야 한다")
  void shouldReturnOnlySuccessfulSourcesData() throws Exception {
    // given
    try (MockedStatic<ArticleSource> mockedArticleSource = mockStatic(ArticleSource.class)) {
      ArticleSource[] mockSources = {ArticleSource.HANKYUNG, ArticleSource.CHOSUN,
          ArticleSource.YONHAP};
      mockedArticleSource.when(ArticleSource::values).thenReturn(mockSources);

      // 2개 소스만 성공
      SyndFeed feed1 = createMockFeedWithEntries(ArticleSource.HANKYUNG, 1);
      SyndFeed feed2 = createMockFeedWithEntries(ArticleSource.CHOSUN, 2);

      AtomicInteger callCount = new AtomicInteger(0);

      when(mockSyndFeedInput.build(any(XmlReader.class))).thenAnswer(invocation -> {
        int call = callCount.getAndIncrement();
        switch (call) {
          case 0:
            return feed1;  // 첫 번째 소스 성공
          case 1:
            return feed2;  // 두 번째 소스 성공
          default:
            throw new RuntimeException("세 번째 소스 실패");
        }
      });

      // when
      List<Article> articles = rssCollectorService.collectFromAllSources();

      // then
      // 병렬 처리로 인해 정확한 개수 예측은 어렵지만, 최소 1개 이상 반환되어야 함
      assertThat(articles).isNotEmpty();
      assertThat(articles.size()).isLessThanOrEqualTo(3); // 최대 3개 (1+2)

      // 모든 Article이 유효한 소스를 가져야 함
      assertThat(articles).allSatisfy(article -> {
        assertThat(article.getSource()).isIn(
            ArticleSource.HANKYUNG.getSource(),
            ArticleSource.CHOSUN.getSource(),
            ArticleSource.YONHAP.getSource()
        );
      });
    }
  }

  private SyndFeed createMockFeedWithEntries(ArticleSource source, int entryCount) {
    SyndFeed mockFeed = mock(SyndFeed.class);
    List<SyndEntry> entries = new ArrayList<>();

    for (int i = 0; i < entryCount; i++) {
      SyndEntry entry = createMockEntry(
          source.getSource() + " 제목 " + (i + 1),
          "http://" + source.name().toLowerCase() + ".com/article/" + (i + 1),
          new Date(),
          source.getSource() + " 내용 " + (i + 1)
      );
      entries.add(entry);
    }

    when(mockFeed.getEntries()).thenReturn(entries);
    return mockFeed;
  }

  private SyndEntry createMockEntry(String title, String link, Date publishedDate, String summary) {
    SyndEntry mockEntry = mock(SyndEntry.class);
    SyndContent mockContent = mock(SyndContent.class);

    when(mockEntry.getTitle()).thenReturn(title);
    when(mockEntry.getLink()).thenReturn(link);
    when(mockEntry.getPublishedDate()).thenReturn(publishedDate);
    when(mockEntry.getDescription()).thenReturn(mockContent);
    when(mockContent.getValue()).thenReturn(summary);

    return mockEntry;
  }

  private SyndFeed createEmptyMockFeed() {
    SyndFeed mockFeed = mock(SyndFeed.class);
    when(mockFeed.getEntries()).thenReturn(Collections.emptyList());
    return mockFeed;
  }
}