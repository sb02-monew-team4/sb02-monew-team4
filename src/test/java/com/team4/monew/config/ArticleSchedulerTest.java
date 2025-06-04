package com.team4.monew.config;

import com.team4.monew.entity.Article;
import com.team4.monew.entity.Interest;
import com.team4.monew.entity.InterestKeyword;
import com.team4.monew.repository.ArticleRepository;
import com.team4.monew.service.collector.RssCollectorService;
import com.team4.monew.service.filter.KeywordFilterService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ArticleSchedulerTest {

  @Mock
  private RssCollectorService collector;
  @Mock
  private KeywordFilterService filter;
  @Mock
  private ArticleRepository repository;

  @InjectMocks
  private ArticleScheduler scheduler;

  @Test
  @DisplayName("필터링된 아티클 저장 검증")
  void validateFilteredArticleSaving() {
    List<InterestKeyword> keywords = new ArrayList<>();
    InterestKeyword keyword = new InterestKeyword(
        UUID.randomUUID(),
        null,
        "개발자"
    );

    keywords.add(keyword);

    List<Article> articles = collector.collectFromAllSources();

    Interest interest = new Interest(
        UUID.randomUUID(),
        "testName",
        0L,
        Instant.now(),
        Instant.now(),
        keywords,
        null
    );
  }

}
