package com.team4.monew.config;

import com.team4.monew.entity.Article;
import com.team4.monew.repository.ArticleRepository;
import com.team4.monew.service.collector.NaverApiCollectorService;
import com.team4.monew.service.collector.RssCollectorService;
import com.team4.monew.service.filter.KeywordFilterService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@EnableScheduling
@Slf4j
public class ArticleScheduler {

  @Autowired
  private RssCollectorService RssCollector;
  @Autowired
  private NaverApiCollectorService naverCollector;
  @Autowired
  private KeywordFilterService filterService;
  @Autowired
  private ArticleRepository repository;

  @Scheduled(cron = "0 0 * * * *")
  @Transactional
  public void hourlyArticleProcessing() {
    log.info("정시 기사 수집 시작");

    // natural Articles
    List<Article> rssArticles = RssCollector.collectFromAllSources();
    List<Article> naverArticles = naverCollector.collectArticles();

    log.info("수집 완료: RSS {}개, 네이버 {}개", rssArticles.size(), naverArticles.size());

    // natural Articles 에 keyword 포함 되는지 filter
    List<Article> filteredRss = filterService.filterArticles(rssArticles);

    // filter 한 List<Article> 저장
    saveUniqueArticles(filteredRss);
    saveUniqueArticles(naverArticles);

    log.info("정시 기사 수집 완료");
  }

  private void saveUniqueArticles(List<Article> articles) {
    int savedCount = 0;
    for (Article article : articles) {
      if (!repository.existsByOriginalLink(article.getOriginalLink())) {
        repository.save(article);
        savedCount++;
      }
    }
    log.info("저장 완료: {}개", savedCount);
  }

}
