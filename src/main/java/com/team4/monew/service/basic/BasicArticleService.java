package com.team4.monew.service.basic;

import com.team4.monew.dto.article.ArticleSearchRequest;
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

  @Override
  public CursorPageResponseArticleDto getAllArticles(ArticleSearchRequest request, UUID userId) {

    return articleRepository.findArticlesWithCursor(request, userId);
  }

  @Override
  public ArticleViewDto registerArticleView(UUID newsId, UUID userId) {
    Article article = articleRepository.findById(newsId)
        .orElseThrow(() -> ArticleNotFoundException.byId(newsId));

    User user = userRepository.findById(userId)
        .orElseThrow(() -> UserNotFoundException.byId(userId));

    ArticleView articleView = new ArticleView(article, user);
    ArticleView savedArticleView = articleViewRepository.save(articleView);
    log.info("article ID: {}, user ID: {} articleView 저장 완료", article.getId(), user.getId());
    return articleViewMapper.toDto(savedArticleView);
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

  @Override
  public List<String> getAllSources() {
    List<String> sources = Arrays.stream(ArticleSource.values())
        .map(ArticleSource::getSource)
        .collect(Collectors.toList());
    sources.add("NAVER");

    return sources;
  }
}
