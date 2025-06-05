package com.team4.monew.service.basic;

import com.team4.monew.dto.article.ArticleViewDto;
import com.team4.monew.entity.Article;
import com.team4.monew.entity.ArticleView;
import com.team4.monew.entity.User;
import com.team4.monew.exception.article.ArticleNotFoundException;
import com.team4.monew.exception.user.UserNotFoundException;
import com.team4.monew.mapper.ArticleViewMapper;
import com.team4.monew.repository.ArticleRepository;
import com.team4.monew.repository.ArticleViewRepository;
import com.team4.monew.repository.UserRepository;
import com.team4.monew.service.ArticleService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class BasicArticleService implements ArticleService {

  private final ArticleViewRepository articleViewRepository;
  private final ArticleRepository articleRepository;
  private final UserRepository userRepository;
  private final ArticleViewMapper articleViewMapper;

  @Override
  public ArticleViewDto registerNewsView(UUID newsId, UUID userId) {
    Article article = articleRepository.findById(newsId)
        .orElseThrow(() -> ArticleNotFoundException.byId(newsId));

    User user = userRepository.findById(userId)
        .orElseThrow(() -> UserNotFoundException.byId(userId));

    ArticleView articleView = new ArticleView(article, user);
    ArticleView savedArticleView = articleViewRepository.save(articleView);
    log.info("article ID: {}, user ID: {} articleView 저장 완료", article.getId(), user.getId());
    return articleViewMapper.toDto(savedArticleView);
  }
}
