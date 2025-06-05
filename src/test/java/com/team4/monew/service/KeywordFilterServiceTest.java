package com.team4.monew.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team4.monew.entity.Article;
import com.team4.monew.entity.Interest;
import com.team4.monew.entity.InterestKeyword;
import com.team4.monew.repository.InterestRepository;
import com.team4.monew.service.filter.KeywordFilterService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class KeywordFilterServiceTest {

  @Mock
  private InterestRepository interestRepository;

  @InjectMocks
  private KeywordFilterService keywordFilterService;

  private List<Interest> mockInterests;
  private List<Article> testArticles;

  @BeforeEach
  void setUp() {
    setupTestArticles();
    setupMockInterests();
  }

  private void setupMockInterests() {
    Interest techInterest = new Interest(
        null,
        "기술",
        0L,
        null,
        null,
        new ArrayList<>(),
        new HashSet<>()
    );

    InterestKeyword keyword1 = new InterestKeyword(
        UUID.randomUUID(),
        techInterest,
        "AI"
    );
    InterestKeyword keyword2 = new InterestKeyword(
        UUID.randomUUID(),
        techInterest,
        "블록체인"
    );
    InterestKeyword keyword3 = new InterestKeyword(
        UUID.randomUUID(),
        techInterest,
        "머신 러닝"
    );

    techInterest.getKeywords().addAll(Arrays.asList(keyword1, keyword2, keyword3));

    Interest financeInterest = new Interest(
        null,
        "금융",
        0L,
        null,
        null,
        new ArrayList<>(),
        new HashSet<>()
    );

    InterestKeyword keyword4 = new InterestKeyword(
        UUID.randomUUID(),
        financeInterest,
        "주식"
    );
    InterestKeyword keyword5 = new InterestKeyword(
        UUID.randomUUID(),
        financeInterest,
        "투자"
    );

    financeInterest.getKeywords().addAll(Arrays.asList(keyword4, keyword5));

    mockInterests = Arrays.asList(techInterest, financeInterest);

    when(interestRepository.findAll()).thenReturn(mockInterests);
  }

  private void setupTestArticles() {
    testArticles = Arrays.asList(
        new Article(
            "한국경제",
            "http://example.com/1",
            "AI 관련 기사",
            Instant.now(),
            "인공지능 관련 기사 summary"),
        new Article(
            "조선일보",
            "http://example.com/2",
            "스포츠 관련 기사",
            Instant.now(),
            "AI 관련 기사 summary"),
        new Article(
            "연합뉴스",
            "http://example.com/3",
            "투자 관련 기사",
            Instant.now(),
            "부동산 관련 기사 summary"
        ),
        new Article(
            "한국경제",
            "http://example.com/4",
            "날씨 관련 기사",
            Instant.now(),
            "일기 예보 관련 기사 summary"
        ));
  }

  @Test
  @DisplayName("제목, 요약에 키워드가 포함된 기사 필터링")
  void shouldFilterArticlesWithKeywordsInTitle() {
    // Given
    when(interestRepository.findAll()).thenReturn(mockInterests);

    // When
    List<Article> filteredArticles = keywordFilterService.filterArticles(testArticles);

    // Then
    assertThat(filteredArticles)
        .hasSize(3) // 필터링 된 기사 3개
        .extracting(Article::getTitle)
        .containsExactlyInAnyOrder(
            "AI 관련 기사", // title에 포함
            "스포츠 관련 기사", // summary에 포함
            "투자 관련 기사" // title에 포함
        );

    verify(interestRepository, times(1)).findAll();
  }
}
