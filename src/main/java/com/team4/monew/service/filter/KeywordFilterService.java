package com.team4.monew.service.filter;

import com.team4.monew.entity.Article;
import com.team4.monew.entity.Interest;
import com.team4.monew.repository.InterestRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeywordFilterService {

  private final InterestRepository interestRepository;

  public List<Article> filterArticles(List<Article> articles) {
    // 키워드 별 Interest 매핑 생성 (N+1 문제 해결)
    Map<String, Set<Interest>> keywordToInterestsMap = buildKeywordInterestsMap();
    Set<String> allKeywords = keywordToInterestsMap.keySet();

    log.info("필터링 시작: 기사 {}개, 키워드 {}개", articles.size(), allKeywords.size());

    return articles.stream()
        .filter(article ->
            containsKeyword(article, allKeywords))
        .map(article -> attachMatchingInterests(article, keywordToInterestsMap))
        .collect(Collectors.toList());
  }

  // 키워드 별로 해당하는 Interest 들을 매핑하는 Map 생성
  private Map<String, Set<Interest>> buildKeywordInterestsMap() {
    return interestRepository.findAllWithKeywords().stream()
        .flatMap(interest -> interest.getKeywords().stream()
            .map(
                InterestKeyword -> Map.entry(InterestKeyword.getKeyword().toLowerCase(), interest)))
        .collect(Collectors.groupingBy(
            Map.Entry::getKey,
            Collectors.mapping(Map.Entry::getValue, Collectors.toSet())
        ));
  }

  // Article 이 키워드를 포함하는지 확인
  private boolean containsKeyword(Article article, Set<String> keywords) {
    String title = article.getTitle().toLowerCase();
    String summary = article.getSummary().toLowerCase();

    return keywords.stream()
        .anyMatch(keyword -> title.contains(keyword) || summary.contains(keyword));
  }

  // Article 에 매칭되는 interest 들을 연관관계로 설정
  private Article attachMatchingInterests(Article article, Map<String, Set<Interest>> keywordMap) {
    String title = article.getTitle().toLowerCase();
    String summary = article.getSummary().toLowerCase();

    Set<Interest> matchingInterests = keywordMap.entrySet().stream()
        .filter(entry -> title.contains(entry.getKey()) ||
            summary.contains(entry.getKey()))
        .flatMap(entry -> entry.getValue().stream())
        .collect(Collectors.toSet());

    // Article에 Interest 연관관계 설정 (양방향 동기화)
    matchingInterests.forEach(article::addInterest);

    log.debug("기사 {}: {}개 관심사와 연결", article.getTitle(), matchingInterests.size());

    return article;
  }

}
