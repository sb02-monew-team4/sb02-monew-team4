package com.team4.monew.asynchronous.event.comment;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommentDeletedEvent {

  private final UUID userId;
  private final UUID commentId;
}
