package com.team4.monew.controller;

import com.team4.monew.dto.comment.*;
import com.team4.monew.exception.ErrorCode;
import com.team4.monew.exception.MonewException;
import com.team4.monew.service.basic.BasicCommentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
      HttpServletRequest servletRequest,
      @Valid @RequestBody CommentUpdateRequest request
  ) {
    UUID authenticatedUserId = (UUID) servletRequest.getAttribute("authenticatedUserId");
    CommentDto response = commentService.update(commentId, authenticatedUserId, request);
    return ResponseEntity.ok(response);
  }

  @GetMapping
  public ResponseEntity<CursorPageResponseCommentDto> getCommentsByArticleWithCursor(
      @RequestParam UUID articleId,
      @RequestParam String orderBy,
      @RequestParam String direction,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false) String after,
      @RequestParam int limit,
      HttpServletRequest servletRequest
  ) {
    if (!orderBy.equals("createdAt") && !orderBy.equals("likeCount")) {
      throw new MonewException(ErrorCode.INVALID_ORDER_BY);
    }

    if (!direction.equalsIgnoreCase("ASC") && !direction.equalsIgnoreCase("DESC")) {
      throw new MonewException(ErrorCode.INVALID_SORT_DIRECTION);
    }

    if (limit < 1 || limit > 100) {
      throw new MonewException(ErrorCode.INVALID_LIMIT);
    }

    UUID authenticatedUserId = (UUID) servletRequest.getAttribute("authenticatedUserId");

    CursorPageResponseCommentDto response = commentService.getCommentsByArticleWithCursor(
        articleId, orderBy, direction, cursor, after, limit, authenticatedUserId
    );
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{commentId}/comment-likes")
  public ResponseEntity<CommentLikeDto> likeComment(
      @PathVariable UUID commentId,
      HttpServletRequest servletRequest
  ) {
    UUID authenticatedUserId = (UUID) servletRequest.getAttribute("authenticatedUserId");
    CommentLikeDto likeDto = commentService.likeComment(commentId, authenticatedUserId);
    return ResponseEntity.ok(likeDto);
  }

  @DeleteMapping("/{commentId}/comment-likes")
  public ResponseEntity<Void> unlikeComment(
      @PathVariable UUID commentId,
      HttpServletRequest servletRequest
  ) {
    UUID authenticatedUserId = (UUID) servletRequest.getAttribute("authenticatedUserId");
    commentService.unlikeComment(commentId, authenticatedUserId);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{commentId}")
  public ResponseEntity<Void> softDelete(
      @PathVariable UUID commentId,
      HttpServletRequest request
  ) {
    UUID authenticatedUserId = (UUID) request.getAttribute("authenticatedUserId");
    commentService.softDelete(commentId, authenticatedUserId);
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{commentId}/hard")
  public ResponseEntity<Void> hardDelete(
      @PathVariable UUID commentId,
      HttpServletRequest request
  ) {
    UUID authenticatedUserId = (UUID) request.getAttribute("authenticatedUserId");
    commentService.hardDelete(commentId, authenticatedUserId);
    return ResponseEntity.noContent().build();
  }
}
