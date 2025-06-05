package com.team4.monew.service.filter;

import com.team4.monew.entity.Article;
import com.team4.monew.entity.InterestKeyword;
import com.team4.monew.repository.InterestRepository;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KeywordFilterService {

  private final InterestRepository interestRepository;

  private final Set<String> KEYWORDS = ConcurrentHashMap.newKeySet();

  public List<Article> filterArticles(List<Article> articles) {
    refreshKeywords();

    return articles.stream()
        .filter(article ->
            containsKeyword(article.getTitle()) ||
                containsKeyword(article.getSummary()))
        .collect(Collectors.toList());
  }

  private boolean containsKeyword(String text) {
    return KEYWORDS.stream()
        .anyMatch(keyword -> text.toLowerCase().contains(keyword));
  }

  private void refreshKeywords() {
    KEYWORDS.clear();
    interestRepository.findAll().forEach(interest ->
        interest.getKeywords().stream()
            .map(InterestKeyword::getKeyword)
            .map(String::toLowerCase)
            .forEach(KEYWORDS::add)
    );
  }
}
