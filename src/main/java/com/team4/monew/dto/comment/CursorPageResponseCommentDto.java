package com.team4.monew.dto.comment;

import java.util.List;

public record CursorPageResponseCommentDto(
    List<CommentDto> content,
    String nextCursor,
    String nextAfter,
    int size,
    Long totalElement,
    boolean hasNext
) {
}
