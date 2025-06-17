package com.team4.monew.dto.notifications;

import com.team4.monew.entity.ResourceType;
import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationDto(
    UUID id,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    boolean confirmed,
    UUID userId,
    String content,
    ResourceType resourceType,
    UUID resourceId
) {
}
