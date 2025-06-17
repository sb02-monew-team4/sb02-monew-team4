package com.team4.monew.dto.comment;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentLikeDto(
    UUID id,
    UUID likedBy,
    LocalDateTime createdAt,
    UUID commentId,
    UUID articleId,
    UUID commentUserId,
    String commentUserNickname,
    String commentContent,
    int commentLikeCount,
    LocalDateTime commentCreatedAt
) {
}
