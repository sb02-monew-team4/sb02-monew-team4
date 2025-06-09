package com.team4.monew.controller;

import com.team4.monew.dto.article.ArticleViewDto;
import com.team4.monew.service.ArticleService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping
public class ArticleController {

  private final ArticleService articleService;

  @PostMapping("/api/articles/{newsId}/article-views")
  public ResponseEntity<ArticleViewDto> register(
      @PathVariable UUID newsId,
      @RequestHeader("Monew-Request-User-ID") UUID userId
  ) {
    ArticleViewDto response = articleService.registerNewsView(newsId, userId);

    return ResponseEntity.ok(response);
  }

}
