package com.team4.monew.dto.comment;

import com.team4.monew.entity.Comment;
import java.time.Instant;
import java.util.UUID;

public record CommentDto(
    UUID id,
    UUID articleId,
    UUID userId,
    String userNickname,
    String content,
    int likeCount,
    boolean likeByMe,
    Instant createdAt
) {
  public static CommentDto from(Comment comment) {
    return new CommentDto(
        comment.getId(),
        comment.getNews().getId(),
        comment.getUser().getId(),
        comment.getUser().getNickname(),
        comment.getContent(),
        comment.getLikeCount().intValue(),
        false,
        comment.getCreatedAt()
    );
  }
}
