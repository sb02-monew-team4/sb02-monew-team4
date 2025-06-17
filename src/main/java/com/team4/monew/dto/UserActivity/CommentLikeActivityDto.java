package com.team4.monew.dto.UserActivity;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentLikeActivityDto(
    UUID id,
    LocalDateTime createdAt,
    UUID commentId,
    UUID articleId,
    String articleTitle,
    UUID commentUserId,
    String commentUserNickname,
    String commentContent,
    Long commentLikeCount,
    LocalDateTime commentCreatedAt
) {

}
