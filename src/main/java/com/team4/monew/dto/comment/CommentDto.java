package com.team4.monew.dto.comment;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentDto(
    UUID id,
    UUID articleId,
    UUID userId,
    String userNickname,
    String content,
    int likeCount,
    boolean likeByMe,
    LocalDateTime createdAt
) {
}
