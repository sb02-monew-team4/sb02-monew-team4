package com.team4.monew.asynchronous.event.article;

import java.util.UUID;

public record ArticleCreatedEventForNotification(
    UUID interestId,
    String interestName,
    int articleCount,
    UUID subscriberId
) {
}
