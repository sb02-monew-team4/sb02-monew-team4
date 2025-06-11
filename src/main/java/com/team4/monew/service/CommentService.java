package com.team4.monew.service;

import com.team4.monew.dto.comment.CommentDto;
import com.team4.monew.dto.comment.CommentLikeDto;
import com.team4.monew.dto.comment.CommentRegisterRequest;
import com.team4.monew.dto.comment.CommentUpdateRequest;
import com.team4.monew.dto.comment.CursorPageResponseCommentDto;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {
  CommentDto register(CommentRegisterRequest request);
  CursorPageResponseCommentDto getCommentsByArticleWithCursor(
      UUID articleId,
      String orderBy,
      String direction,
      String cursor,
      String after,
      int limit,
      UUID requesterId
  );
  Page<CommentDto> getMyComments(UUID userId, Pageable pageable);
  CommentLikeDto likeComment(UUID commentId, UUID userId);
  void unlikeComment(UUID commentId, UUID userId);
  CommentDto update(UUID commentId, UUID userId, CommentUpdateRequest request);
  void softDelete(UUID commentId, UUID userId);
  void hardDelete(UUID commentId, UUID userId);
}
