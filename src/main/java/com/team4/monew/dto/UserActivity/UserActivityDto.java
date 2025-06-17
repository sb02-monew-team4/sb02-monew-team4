package com.team4.monew.dto.UserActivity;

import com.team4.monew.dto.article.ArticleViewDto;
import com.team4.monew.dto.interest.SubscriptionDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UserActivityDto(
    UUID id,
    String email,
    String nickname,
    LocalDateTime createdAt,
    List<SubscriptionDto> subscriptions,
    List<CommentActivityDto> comments,
    List<CommentLikeActivityDto> commentLikes,
    List<ArticleViewDto> articleViews
) {

}
