package com.team4.monew.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.team4.monew.entity.Article;
import com.team4.monew.entity.Interest;
import com.team4.monew.entity.InterestKeyword;
import com.team4.monew.repository.InterestRepository;
import com.team4.monew.service.filter.KeywordFilterService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
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

  private Interest techInterest;
  private Interest sportsInterest;
  private Article techArticle;
  private Article sportsArticle;
  private Article unrelatedArticle;

  @BeforeEach
  void setUp() {

    techInterest = createInterest("기술", List.of("스프링", "자바", "개발"));
    sportsInterest = createInterest("스포츠", List.of("축구", "야구"));

    techArticle = createArticle("스프링 부트 최신 기능 소개",
        "스프링 부트의 새로운 기능들을 살펴봅니다.");
    sportsArticle = createArticle("월드컵 축구 경기 결과",
        "월드컵 축구 경기 결과는 ... ");
    unrelatedArticle = createArticle("일기 예보",
        "오늘 하늘은 맑을 예정입니다.");
  }

  @Test
  @DisplayName("키워드가 포함된 기사만 필터링되어야 한다.")
  void shouldFilterArticleContainingKeywords() {
    // given
    when(interestRepository.findAllWithKeywords())
        .thenReturn(List.of(techInterest, sportsInterest));

    List<Article> inputArticles = List.of(techArticle, sportsArticle, unrelatedArticle);

    // when
    List<Article> filteredArticles = keywordFilterService.filterArticles(inputArticles);

    // then
    assertThat(filteredArticles).hasSize(2);
    assertThat(filteredArticles).contains(techArticle, sportsArticle);
    assertThat(filteredArticles).doesNotContain(unrelatedArticle);
  }

  @Test
  @DisplayName("여러 관심사의 키워드에 매칭되는 기사는 모든 관심사와 연결되어야 한다.")
  void shouldAttachMultipleInterestsToArticle() {
    //  given
    Article mixedArticle = createArticle("스프링으로 축구 게임 개발",
        "스프링 프레임워크로 축구 게임을 만드는 방법은 ...");

    when(interestRepository.findAllWithKeywords())
        .thenReturn(List.of(techInterest, sportsInterest));

    // when
    List<Article> filteredArticles = keywordFilterService.filterArticles(List.of(mixedArticle));

    // then
    Article filteredArticle = filteredArticles.get(0);
    assertThat(filteredArticle.getInterest()).hasSize(2);
    assertThat(filteredArticle.getInterest()).contains(techInterest, sportsInterest);
  }

  private Interest createInterest(String name, List<String> keywords) {
    Interest interest = new Interest(
        UUID.randomUUID(), name, 0L, Instant.now(), Instant.now(),
        new ArrayList<>(), new ArrayList<>(), new HashSet<>()
    );

    List<InterestKeyword> keywordEntities = keywords.stream()
        .map(keyword -> new InterestKeyword(UUID.randomUUID(), interest, keyword))
        .collect(Collectors.toList());

    interest.getKeywords().addAll(keywordEntities);

    return interest;
  }

  private Article createArticle(String title, String summary) {
    return new Article(
        "TEST_SOURCE",
        "http://test.com/" + UUID.randomUUID(),
        title,
        Instant.now(),
        summary
    );
  }
}
