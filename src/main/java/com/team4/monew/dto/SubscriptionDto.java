package com.team4.monew.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SubscriptionDto(
    UUID id,
    UUID interestId,
    String interestName,
    List<String> interestKeywords,
    int interestSubscriberCount,
    Instant createdAt
) {
}
