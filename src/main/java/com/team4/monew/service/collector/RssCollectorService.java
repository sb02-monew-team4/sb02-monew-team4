package com.team4.monew.service.collector;

import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.team4.monew.entity.Article;
import com.team4.monew.entity.ArticleSource;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RssCollectorService {

  private final SyndFeedInput syndFeedInput = new SyndFeedInput();

  public List<Article> collectFromAllSources() {
    return Arrays.stream(ArticleSource.values())
        .parallel()
        .map(this::fetchArticlesFromSource)
        .flatMap(List::stream)
        .collect(Collectors.toList());
  }

  private List<Article> fetchArticlesFromSource(ArticleSource source) {
    try {
      String rssUrl = source.getRssUrl();
      log.info("RSS 수집 시작 - source: {}, url: {}", source.getSource(), rssUrl);

      URL feedUrl = new URL(rssUrl);
      SyndFeed feed = syndFeedInput.build(new XmlReader(feedUrl));

      List<Article> articles = feed.getEntries().stream()
          .map(entry -> {
            try {
              return convertToArticle(entry, source);
            } catch (Exception e) {
              log.warn("entry 변환 실패 - title: {}, error: {}",
                  entry.getTitle(), e.getMessage(), e);
              return null;
            }
          })
          .filter(Objects::nonNull)
          .collect(Collectors.toList());

      log.info("RSS 수집 완료 - source: {}, 수집된 기사 수: {}", source.getSource(), articles.size());

      return articles;

    } catch (Exception e) {
      log.error("{} 수집 실패: {}", source.getSource(), e.getMessage());
      return Collections.emptyList();
    }
  }

  private Article convertToArticle(SyndEntry entry, ArticleSource source) {
    String summary = null;
    SyndContent syndDescription = entry.getDescription();
    if (syndDescription != null) {
      summary = syndDescription.getValue();
    } else {
      log.debug("[{}] 기사 '{}'에 description (summary) 필드가 없습니다.", source.getSource(), entry.getTitle());
    }

    // summary 길이 150 넘으면 substring 하도록 수정
    if (summary != null && summary.length() > 150) {
      summary = summary.substring(0, 150);
    }
    return new Article(
        source.getSource(),
        entry.getLink(),
        entry.getTitle(),
        entry.getPublishedDate().toInstant(),
        summary
    );
  }

}
