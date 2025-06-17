package com.team4.monew.asynchronous.event.commentlike;

import java.util.UUID;

public record CommentLikeCreatedEventForNotification(
    UUID commentId,
    UUID likerId,
    UUID commentOwnerId
) {

}
