package com.team4.monew.scheduler;

import com.team4.monew.asynchronous.event.article.ArticleCreatedEventForNotification;
import com.team4.monew.entity.Article;
import com.team4.monew.entity.Interest;
import com.team4.monew.entity.Subscription;
import com.team4.monew.repository.ArticleRepository;
import com.team4.monew.repository.SubscriptionRepository;
import com.team4.monew.service.collector.NaverApiCollectorService;
import com.team4.monew.service.collector.RssCollectorService;
import com.team4.monew.service.filter.KeywordFilterService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleScheduler {

  private final RssCollectorService rssCollector;
  private final NaverApiCollectorService naverCollector;
  private final KeywordFilterService filterService;
  private final ArticleRepository repository;
  private final ApplicationEventPublisher eventPublisher;
  private final SubscriptionRepository subscriptionRepository;

  @Scheduled(cron = "0 0 * * * *", zone = "Asia/Seoul")
  @Transactional
  public void hourlyArticleProcessing() {
    log.info("정시 기사 수집 시작");

    // natural Articles
    List<Article> rssArticles = rssCollector.collectFromAllSources();
    List<Article> naverArticles = naverCollector.collectArticles();

    log.info("수집 완료: RSS {}개, 네이버 {}개", rssArticles.size(), naverArticles.size());

    // natural Articles 에 keyword 포함 되는지 filter
    List<Article> filteredRss = filterService.filterArticles(rssArticles);

    // 새로 저장된 기사만 수집
    List<Article> savedFilteredRss = saveUniqueArticles(filteredRss);
    List<Article> savedNaverArticles = saveUniqueArticles(naverArticles);

    // 저장된 전체 기사 통합
    List<Article> allNewlySavedArticles = new ArrayList<>();
    allNewlySavedArticles.addAll(savedFilteredRss);
    allNewlySavedArticles.addAll(savedNaverArticles);

    // 이벤트 발행
    publishNotificationEvents(allNewlySavedArticles);

    log.info("정시 기사 수집 완료");
  }

  private List<Article> saveUniqueArticles(List<Article> articles) {
    List<Article> newlyCreatedArticles = new ArrayList<>();
    for (Article article : articles) {
      if (!repository.existsByOriginalLink(article.getOriginalLink())) {
        Article createdArticle = repository.save(article);
        newlyCreatedArticles.add(createdArticle);
      }
    }
    log.info("저장 완료: {}개", newlyCreatedArticles.size());
    return newlyCreatedArticles;
  }

  private void publishNotificationEvents(List<Article> newlyCreatedArticles) {
    log.info("알림 생성 이벤트 발행 시작");

    if (newlyCreatedArticles.isEmpty()) {
      return;
    }

    log.debug("새로 저장된 기사 수: {}", newlyCreatedArticles.size());

    // 1. 관심사별로 새로 생성된 기사 수 집계
    Map<Interest, Long> countByInterest = newlyCreatedArticles.stream()
        .flatMap(article -> {
          if (article.getInterest() == null) {
            log.warn("기사 {}에 연관된 관심사가 null입니다.", article.getId());
            return Stream.empty();
          }
          return article.getInterest().stream();
        })
        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    log.debug("관심사별 집계 결과: {}", countByInterest);

    // 2. 각 관심사별로 구독자를 찾아 이벤트를 발행
    countByInterest.forEach((interest, count) -> {
      List<Subscription> subscriptions = subscriptionRepository.findByInterest(interest);
      subscriptions.forEach(subscription -> {
        eventPublisher.publishEvent(new ArticleCreatedEventForNotification(
            interest.getId(),
            interest.getName(),
            count.intValue(),
            subscription.getUser().getId()
        ));
      });
    });

    log.info("알림 생성 이벤트 발행 종료");
  }

}
