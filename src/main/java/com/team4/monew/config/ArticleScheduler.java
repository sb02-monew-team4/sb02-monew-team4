package com.team4.monew.config;

import com.team4.monew.entity.Article;
import com.team4.monew.repository.ArticleRepository;
import com.team4.monew.service.collector.RssCollectorService;
import com.team4.monew.service.filter.KeywordFilterService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class ArticleScheduler {

  @Autowired
  private RssCollectorService collector;
  @Autowired
  private KeywordFilterService filterService;
  @Autowired
  private ArticleRepository repository;

  @Scheduled(cron = "0 0 * * * *")
  public void hourlyArticleProcessing() {
    List<Article> rawArticles = collector.collectFromAllSources();
    List<Article> filtered = filterService.filterArticles(rawArticles);
    saveUniqueArticles(filtered);
  }

  private void saveUniqueArticles(List<Article> articles) {
    articles.forEach(article -> {
      if (!repository.existsByOriginalLink(article.getOriginalLink())) {
        repository.save(article);
      }
    });
  }
  
}
