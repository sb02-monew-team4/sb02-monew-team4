package com.team4.monew.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.team4.monew.entity.Article;
import com.team4.monew.repository.ArticleRepository;
import com.team4.monew.service.collector.NaverApiCollectorService;
import com.team4.monew.service.collector.RssCollectorService;
import com.team4.monew.service.filter.KeywordFilterService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ArticleSchedulerTest {

  @Mock
  private RssCollectorService rssCollector;
  @Mock
  private NaverApiCollectorService naverCollector;
  @Mock
  private KeywordFilterService filter;
  @Mock
  private ArticleRepository repository;

  @InjectMocks
  private ArticleScheduler scheduler;

  @Test
  @DisplayName("필터링된 아티클, 네이버 아티클 저장 검증")
  void hourlyArticleProcessingTest() {
    Article rssArticle =
        createArticle("RSS 기사", "RSS 기사 제목", "http://rss.com/1", "RSS 기사입니다.");
    Article naverArticle =
        createArticle("Naver 기사", "Naver 기사 제목", "http://naver.com/1", "Naver 기사입니다.");

    List<Article> rssArticles = List.of(rssArticle);
    List<Article> naverArticles = List.of(naverArticle);

    when(rssCollector.collectFromAllSources()).thenReturn(List.of(rssArticle));
    when(naverCollector.collectArticles()).thenReturn(List.of(naverArticle));

    when(filter.filterArticles(rssArticles)).thenReturn(rssArticles);
    when(filter.filterArticles(naverArticles)).thenReturn(naverArticles);

    when(repository.existsByOriginalLink(anyString())).thenReturn(false);

    scheduler.hourlyArticleProcessing();

    verify(rssCollector, times(1)).collectFromAllSources();
    verify(naverCollector, times(1)).collectArticles();

    verify(filter, times(1)).filterArticles(rssArticles);
    verify(filter, times(1)).filterArticles(naverArticles);

    verify(repository, times(2)).save(any(Article.class));
  }

  private Article createArticle(String source, String title, String link, String summary) {
    return new Article(
        source,
        link,
        title,
        Instant.now(),
        summary
    );
  }


}
