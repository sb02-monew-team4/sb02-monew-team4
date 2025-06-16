package com.team4.monew.service.basic;

import com.team4.monew.dto.article.ArticleRestoreResultDto;
import com.team4.monew.dto.article.ArticleViewDto;
import com.team4.monew.dto.article.CursorPageResponseArticleDto;
import com.team4.monew.entity.Article;
import com.team4.monew.entity.ArticleSource;
import com.team4.monew.entity.ArticleView;
import com.team4.monew.entity.User;
import com.team4.monew.exception.article.ArticleNotFoundException;
import com.team4.monew.exception.user.UserNotFoundException;
import com.team4.monew.mapper.ArticleViewMapper;
import com.team4.monew.repository.ArticleRepository;
import com.team4.monew.repository.ArticleViewRepository;
import com.team4.monew.repository.UserRepository;
import com.team4.monew.service.ArticleService;
import com.team4.monew.service.filter.KeywordFilterService;
import com.team4.monew.service.s3.ArticleS3Service;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class BasicArticleService implements ArticleService {

  private final ArticleViewRepository articleViewRepository;
  private final ArticleRepository articleRepository;
  private final UserRepository userRepository;
  private final ArticleViewMapper articleViewMapper;
  private final ArticleS3Service articleS3Service;
  private final KeywordFilterService keywordFilterService;

  @Override
  public ArticleViewDto registerArticleView(UUID articleId, UUID userId) {
    Article article = articleRepository.findById(articleId)
        .orElseThrow(() -> ArticleNotFoundException.byId(articleId));

    User user = userRepository.findById(userId)
        .orElseThrow(() -> UserNotFoundException.byId(userId));

    ArticleView articleView = new ArticleView(article, user);
    ArticleView savedArticleView = articleViewRepository.save(articleView);
    // viewCount 증가
    article.incrementViewCount();
    log.info("article ID: {}, user ID: {} articleView 저장 완료", article.getId(), user.getId());
    return articleViewMapper.toDto(savedArticleView);
  }

  @Override
  public CursorPageResponseArticleDto getAllArticles(
      String keyword,
      UUID interestId,
      List<String> sourceIn,
      Instant publishDateFrom,
      Instant publishDateTo,
      String orderBy,
      String direction,
      String cursor,
      int limit,
      Instant after,
      UUID userId
  ) {
    return articleRepository.findArticlesWithCursor(
        keyword, interestId, sourceIn,
        publishDateFrom, publishDateTo,
        orderBy, direction,
        cursor, limit,
        after, userId);
  }

  @Override
  public List<String> getAllSources() {
    List<String> sources = Arrays.stream(ArticleSource.values())
        .map(ArticleSource::getSource)
        .collect(Collectors.toList());
    sources.add("NAVER");

    return sources;
  }

  @Override
  public ArticleRestoreResultDto restoreArticle(Instant from, Instant to) {
    // interest 필드를 @JsonIgnore 처리, S3에서 download 한 Article 들은 interest = new HashSet으로 비어있다.
    // S3로부터 download 한 Article들을 현재 등록된 interest의 keyword로 filtering + keyword mapping 한다.
    List<Article> downloadArticles = articleS3Service.getArticlesByDateRange(from, to);
    List<Article> restoredArticles = keywordFilterService.filterArticles(downloadArticles);

    articleRepository.saveAll(restoredArticles);

    List<UUID> restoredArticlesIds = restoredArticles.stream()
        .map(Article::getId)
        .toList();

    long restoredArticleCount = restoredArticles.size();

    return new ArticleRestoreResultDto(
        Instant.now(),
        restoredArticlesIds,
        restoredArticleCount
    );
  }

  @Override
  public void softDelete(UUID articleId) {
    Article article = articleRepository.findById(articleId)
        .orElseThrow(() -> ArticleNotFoundException.byId(articleId));

    article.updateIsDeleted();
  }

  @Override
  public void hardDelete(UUID articleId) {
    articleRepository.deleteById(articleId);
    log.warn("article ID: {} 삭제 완료", articleId);
  }
}
