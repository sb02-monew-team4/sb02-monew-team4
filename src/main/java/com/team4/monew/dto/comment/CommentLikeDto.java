package com.team4.monew.dto.comment;

import com.team4.monew.entity.CommentLike;
import java.time.Instant;
import java.util.UUID;

public record CommentLikeDto(
    UUID id,
    UUID likedBy,
    Instant createdAt,
    UUID commentId,
    UUID articleId,
    UUID commentUserId,
    String commentUserNickname,
    String commentContent,
    int commentLikeCount,
    Instant commentCreatedAt
) {
    public static CommentLikeDto from(CommentLike like) {
    var comment = like.getComment();
    return new CommentLikeDto(
        like.getId(),
        like.getUser().getId(),
        like.getCreatedAt(),
        comment.getId(),
        comment.getNews().getId(),
        comment.getUser().getId(),
        comment.getUser().getNickname(),
        comment.getContent(),
        comment.getLikeCount().intValue(),
        comment.getCreatedAt()
    );
  }
}
