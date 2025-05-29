package com.team4.monew.dto.UserActivity;

import java.time.Instant;
import java.util.UUID;

public record CommentLikeActivityDto(
    UUID id,
    Instant createdAt,
    UUID commentId,
    UUID articleId,
    String articleTitle,
    UUID commentUserId,
    String commentUserNickname,
    String commentContent,
    Long commentLikeCount,
    Instant commentCreatedAt
) {

}
