package com.team4.monew.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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
import com.team4.monew.service.basic.BasicArticleService;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ArticleServiceTest {

  @Mock
  private ArticleRepository articleRepository;
  @Mock
  private ArticleViewRepository articleViewRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private ArticleViewMapper articleViewMapper;

  @InjectMocks
  private BasicArticleService basicArticleService;

  @Test
  @DisplayName("기사 뷰 등록 성공")
  void registerArticleView_Success() {
    // Given
    UUID newsId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    Article article = new Article();
    User user = User.create(null, null, null);
    ArticleView articleView = new ArticleView(article, user);
    LocalDateTime nowLocalDateTime = LocalDateTime.now();
    Instant nowInstant = Instant.now();

    ArticleViewDto expectedDto = new ArticleViewDto(
        UUID.randomUUID(),
        userId,
        nowLocalDateTime,
        newsId,
        "test source",
        "https://test.com",
        nowInstant,
        "test summary",
        10L,
        100L
    );

    when(articleRepository.findById(newsId)).thenReturn(Optional.of(article));
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(articleViewRepository.save(any(ArticleView.class))).thenReturn(articleView);
    when(articleViewMapper.toDto(articleView)).thenReturn(expectedDto);

    // When
    ArticleViewDto result = basicArticleService.registerArticleView(newsId, userId);

    // Then
    assertNotNull(result);
    assertEquals(expectedDto, result);
    verify(articleRepository).findById(newsId);
    verify(userRepository).findById(userId);
    verify(articleViewRepository).save(any(ArticleView.class));
    verify(articleViewMapper).toDto(articleView);
  }

  @Test
  void registerNewsView_ArticleNotFoundException() {
    UUID newsId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    when(articleRepository.findById(newsId)).thenReturn(Optional.empty());

    assertThrows(ArticleNotFoundException.class, () ->
        basicArticleService.registerArticleView(newsId, userId)
    );
    verify(articleRepository).findById(newsId);
    verifyNoInteractions(userRepository, articleViewRepository, articleViewMapper);
  }

  @Test
  void registerArticleView_UserNotFoundException() {
    UUID newsId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    Article article = new Article();

    when(articleRepository.findById(newsId)).thenReturn(Optional.of(article));
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> // UserNotFoundException.class 로 변경해야함
        basicArticleService.registerArticleView(newsId, userId)
    );
    verify(articleRepository).findById(newsId);
    verify(userRepository).findById(userId);
    verifyNoInteractions(articleViewRepository, articleViewMapper);
  }

}
