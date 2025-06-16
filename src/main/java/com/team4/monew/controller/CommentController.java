package com.team4.monew.controller;

import com.team4.monew.dto.comment.*;
import com.team4.monew.exception.ErrorCode;
import com.team4.monew.exception.MonewException;
import com.team4.monew.service.basic.BasicCommentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
public class CommentController {

  private final BasicCommentService commentService;

  @PostMapping
  public ResponseEntity<CommentDto> register(
      @Valid @RequestBody CommentRegisterRequest request,
    HttpServletRequest servletRequest
  ) {
    UUID authenticatedUserId = (UUID) servletRequest.getAttribute("authenticatedUserId");

    CommentDto response = commentService.register(authenticatedUserId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PatchMapping("/{commentId}")
  public ResponseEntity<CommentDto> update(
      @PathVariable UUID commentId,
      @RequestHeader("Monew-Request-User-ID") UUID requesterId,
      @Valid @RequestBody CommentUpdateRequest request
  ) {
    CommentDto response = commentService.update(commentId, requesterId, request);
    return ResponseEntity.ok(response);
  }

  @GetMapping
  public ResponseEntity<CursorPageResponseCommentDto> getCommentsByArticleWithCursor(
      @RequestParam UUID articleId,
      @RequestParam(defaultValue = "createdAt") String orderBy,
      @RequestParam(defaultValue = "ASC") String direction,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) String after,
      @RequestParam(defaultValue = "10") int limit,
      @RequestHeader("Monew-Request-User-ID") UUID requesterId
  ) {

    log.info("getComments 호출됨 - limit: {}, orderBy: {}, direction: {}, articleId: {}",
        limit, orderBy, direction, articleId);

    if (!orderBy.equals("createdAt") && !orderBy.equals("likeCount")) {
      throw new MonewException(ErrorCode.INVALID_ORDER_BY);
    }

    if (!direction.equalsIgnoreCase("ASC") && !direction.equalsIgnoreCase("DESC")) {
      throw new MonewException(ErrorCode.INVALID_SORT_DIRECTION);
    }

    if (limit < 1 || limit > 100) {
      log.warn("잘못된 limit 값 {} -> 기본값 10으로 대체", limit);
      limit = 10;
    }

    CursorPageResponseCommentDto response = commentService.getCommentsByArticleWithCursor(
        articleId, orderBy, direction, cursor, after, limit, requesterId
    );
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{commentId}/comment-likes")
  public ResponseEntity<CommentLikeDto> likeComment(
      @PathVariable UUID commentId,
      @RequestHeader("Monew-Request-User-ID") UUID requesterId
  ) {
    CommentLikeDto likeDto = commentService.likeComment(commentId, requesterId);
    return ResponseEntity.ok(likeDto);
  }

  @DeleteMapping("/{commentId}/comment-likes")
  public ResponseEntity<Void> unlikeComment(
      @PathVariable UUID commentId,
      @RequestHeader("Monew-Request-User-ID") UUID requesterId
  ) {
    commentService.unlikeComment(commentId, requesterId);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{commentId}")
  public ResponseEntity<Void> softDelete(
      @PathVariable UUID commentId,
      @RequestHeader("Monew-Request-User-ID") UUID requesterId
  ) {
    commentService.softDelete(commentId, requesterId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{commentId}/hard")
  public ResponseEntity<Void> hardDelete(
      @PathVariable UUID commentId,
      @RequestHeader("Monew-Request-User-ID") UUID requesterId
  ) {
    commentService.hardDelete(commentId, requesterId);
    return ResponseEntity.noContent().build();
  }
}
