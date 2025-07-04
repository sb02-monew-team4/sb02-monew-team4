package com.team4.monew.asynchronous.event.commentlike;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommentLikeDeletedEvent {

  private final UUID userId;
  private final UUID commentLikeId;
}
