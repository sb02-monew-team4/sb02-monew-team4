package com.team4.monew.service.collector;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.team4.monew.entity.Article;
import com.team4.monew.entity.InterestKeyword;
import com.team4.monew.repository.InterestRepository;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jdom2.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NaverApiCollectorService {

  private final InterestRepository interestRepository;
  private final SyndFeedInput syndFeedInput = new SyndFeedInput();

  @Value("${naver.client.id}")
  private String clientId;

  @Value("${naver.client.secret}")
  private String clientSecret;

  @Value("${naver.api.url}")
  private String apiURL;

  public List<Article> collectArticles() {
    Set<String> uniqueKeywords = interestRepository.findAll().stream()
        .flatMap(interest -> interest.getKeywords().stream())
        .map(InterestKeyword::getKeyword)
        .collect(Collectors.toSet());

    log.info("네이버 API 수집 시작: {}개 키워드", uniqueKeywords.size());

    return uniqueKeywords.stream()
        .map(this::fetchArticlesByKeyword)
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }

  public Set<String> extractUniqueKeywords() {
    return interestRepository.findAll().stream()
        .flatMap(interest -> interest.getKeywords().stream())
        .map(InterestKeyword::getKeyword)
        .collect(Collectors.toSet());
  }

  private List<Article> fetchArticlesByKeyword(String keyword) {
    try {
      String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
      String requestUrl = String.format("%s?query=%s&display=100&sort=date", apiURL,
          encodedKeyword);

      HttpURLConnection conn = (HttpURLConnection) new URL(requestUrl).openConnection();
      conn.setRequestMethod("GET");
      conn.setRequestProperty("X-Naver-Client-Id", clientId);
      conn.setRequestProperty("X-Naver-Client-Secret", clientSecret);

      if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) { // 여기부터 시작
        try (InputStream inputStream = conn.getInputStream()) {
          SyndFeed feed = syndFeedInput.build(new XmlReader(inputStream));

          List<Article> articles = feed.getEntries().stream()
              .map(this::convertToArticle)
              .collect(Collectors.toList());
          log.debug("키워드 '{}': {}개 기사 수집", keyword, articles.size());

          return articles;
        }
      } else {
        log.error("Naver API 호출 실패 - 키워드: {}, 상태 코드: {}", keyword, conn.getResponseCode());
        return Collections.emptyList();
      }
    } catch (Exception e) {
      log.error("Naver 뉴스 수집 오류 - 키워드: {}: {}", keyword, e.getMessage());
      return Collections.emptyList(); // 전체 프로세스 보장을 위해 빈 리스트 반
    }
  }

  // summary 길이 150 넘으면 substring 하도록 수정
  private Article convertToArticle(SyndEntry entry) {
    String summary = entry.getDescription().getValue();
    if (summary.length() > 150) {
      summary = summary.substring(0, 150);
    }
    return new Article(
        "NAVER",
        extractOriginalLink(entry),
        entry.getTitle(),
        entry.getPublishedDate().toInstant(),
        summary
    );
  }

  private String extractOriginalLink(SyndEntry entry) {
    return entry.getForeignMarkup().stream()
        .filter(node -> "originallink".equals(node.getName()))
        .map(Element::getText)
        .findFirst()
        .orElse(entry.getLink());
  }


}
