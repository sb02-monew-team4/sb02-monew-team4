package com.team4.monew.controller;

import com.team4.monew.dto.article.ArticleRestoreResultDto;
import com.team4.monew.dto.article.ArticleViewDto;
import com.team4.monew.dto.article.CursorPageResponseArticleDto;
import com.team4.monew.service.ArticleService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping
@Slf4j
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
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) UUID interestId,
      @RequestParam(required = false) List<String> sourceIn,
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate publishDateFrom,
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate publishDateTo,
      @RequestParam(defaultValue = "publishDate") String orderBy,
      @RequestParam(defaultValue = "DESC") String direction,
      @RequestParam(required = false) String cursor,
      @RequestParam(defaultValue = "20") @Min(1) @Max(50) int limit,
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant after,
      @RequestHeader(value = "Monew-Request-User-ID", required = false) UUID userId
  ) {
    Instant fromInstant = (publishDateFrom != null)
        ? publishDateFrom.atStartOfDay(ZoneOffset.UTC).toInstant()
        : null;
    Instant toInstant = (publishDateTo != null)
        ? publishDateTo.atTime(LocalTime.MAX).atOffset(ZoneOffset.UTC).toInstant()
        : null;

    CursorPageResponseArticleDto response = articleService.getAllArticles(
        keyword, interestId, sourceIn,
        fromInstant, toInstant,
        orderBy, direction,
        cursor, limit,
        after, userId
    );

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
