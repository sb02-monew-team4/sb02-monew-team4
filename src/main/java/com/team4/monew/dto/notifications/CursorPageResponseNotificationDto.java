package com.team4.monew.dto.notifications;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public record CursorPageResponseNotificationDto(
    List<NotificationDto> content,
    String nextCursor,
    LocalDateTime nextAfter,
    int size,
    long totalElements,
    boolean hasNext
) {
}
