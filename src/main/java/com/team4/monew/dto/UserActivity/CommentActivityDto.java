package com.team4.monew.dto.UserActivity;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentActivityDto(
    UUID id,
    UUID articleId,
    String articleTitle,
    UUID userId,
    String userNickname,
    String content,
    Long likeCount,
    LocalDateTime createdAt
) {

}
