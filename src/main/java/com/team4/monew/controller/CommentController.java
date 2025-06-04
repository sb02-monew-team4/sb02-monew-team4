package com.team4.monew.controller;

import com.team4.monew.dto.comment.*;
import com.team4.monew.service.basic.BasicCommentService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

  private final BasicCommentService commentService;

  public CommentController(BasicCommentService commentService) {
    this.commentService = commentService;
  }

  @GetMapping
  public ResponseEntity<CursorPageResponseCommentDto> findComments(
      @RequestParam UUID articleId,
      @RequestParam String orderBy,
      @RequestParam String direction,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) String after,
      @RequestParam int limit,
      @RequestHeader("Monew-Request-User-ID") UUID requesterId
  ) {

    CursorPageResponseCommentDto response = commentService.findCommentsByArticleWithCursorPaging(
        articleId, orderBy, direction, cursor, after, limit);
    return ResponseEntity.ok(response);
  }

  @PostMapping
  public ResponseEntity<CommentDto> register(
      @Valid @RequestBody CommentRegisterRequest request
  ) {
    CommentDto created = commentService.register(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
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
      @RequestHeader("Monew-Request-User-ID") UUID userId
  ) {
    commentService.softDelete(commentId, userId);
    return ResponseEntity.noContent().build();
  }

  @PatchMapping("/{commentId}")
  public ResponseEntity<CommentDto> update(
      @PathVariable UUID commentId,
      @RequestHeader("Monew-Request-User-ID") UUID requesterId,
      @Valid @RequestBody CommentUpdateRequest request
  ) {
    CommentDto updated = commentService.update(commentId, requesterId, request);
    return ResponseEntity.ok(updated);
  }

  @DeleteMapping("/{commentId}/hard")
  public ResponseEntity<Void> hardDelete(
      @PathVariable UUID commentId,
      @RequestHeader("Monew-Request-User-ID") UUID userId
  ) {
    commentService.hardDelete(commentId, userId);
    return ResponseEntity.noContent().build();
  }
}
