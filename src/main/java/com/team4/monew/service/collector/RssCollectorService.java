package com.team4.monew.service.collector;

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
      URL feedUrl = new URL(source.getRssUrl());
      SyndFeed feed = syndFeedInput.build(new XmlReader(feedUrl));
      return feed.getEntries().stream()
          .map(entry -> convertToArticle(entry, source))
          .collect(Collectors.toList());
    } catch (Exception e) {
      log.error("{} 수집 실패: {}", source.getSource(), e.getMessage());
      return Collections.emptyList();
    }
  }

  private Article convertToArticle(SyndEntry entry, ArticleSource source) {
    return new Article(
        source.getSource(),
        entry.getLink(),
        entry.getTitle(),
        entry.getPublishedDate().toInstant(),
        entry.getDescription().getValue()
    );
  }

}
