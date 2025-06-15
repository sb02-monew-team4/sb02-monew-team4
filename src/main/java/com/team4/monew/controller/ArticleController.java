package com.team4.monew.controller;

import com.team4.monew.dto.article.ArticleRestoreResultDto;
import com.team4.monew.dto.article.ArticleSearchRequest;
import com.team4.monew.dto.article.ArticleViewDto;
import com.team4.monew.dto.article.CursorPageResponseArticleDto;
import com.team4.monew.service.ArticleService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping
public class ArticleController {

  private final ArticleService articleService;

  @PostMapping("/api/articles/{articleId}/article-views")
  public ResponseEntity<ArticleViewDto> register(
      @PathVariable UUID articleId,
      @RequestHeader("Monew-Request-User-ID") UUID userId
  ) {
    ArticleViewDto response = articleService.registerArticleView(articleId, userId);

    return ResponseEntity.ok(response);
  }

  @Transactional
  @GetMapping("/api/articles")
  public ResponseEntity<CursorPageResponseArticleDto> getArticles(
      @Valid @ModelAttribute ArticleSearchRequest request,
      @RequestHeader("Monew-Request-User-ID") UUID userId
  ) {
    CursorPageResponseArticleDto response = articleService.getAllArticles(request, userId);

    return ResponseEntity.ok(response);
  }

  @Transactional
  @GetMapping("/api/articles/sources")
  public ResponseEntity<List<String>> getSources() {
    List<String> sources = articleService.getAllSources();

    return ResponseEntity.ok(sources);
  }

  @Transactional
  @GetMapping("/api/articles/restore")
  public ResponseEntity<ArticleRestoreResultDto> restore(
      @RequestParam Instant from,
      @RequestParam Instant to
  ) {
    ArticleRestoreResultDto response = articleService.restoreArticle(from, to);

    return ResponseEntity.ok(response);
  }

  @Transactional
  @DeleteMapping("/api/articles/{articleId}")
  public ResponseEntity<Void> softDelete(@PathVariable UUID articleId) {
    articleService.softDelete(articleId);

    return ResponseEntity.noContent().build();
  }

  @Transactional
  @DeleteMapping("/api/articles/{articleId}/hard")
  public ResponseEntity<Void> hardDelete(@PathVariable UUID articleId) {
    articleService.hardDelete(articleId);
    return ResponseEntity.noContent().build();
  }


}
