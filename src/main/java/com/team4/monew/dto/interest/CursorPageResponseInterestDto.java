package com.team4.monew.dto.interest;

import java.util.List;

public record CursorPageResponseInterestDto(
   List<InterestDto> content,
   String nextCursor,
   String nextAfter,
   int size,
   Long totalElements,
   boolean hasNext
) {
}
