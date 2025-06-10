package com.team4.monew.asynchronous.event.commentlike;

import com.team4.monew.entity.CommentLike;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommentLikeCreatedEvent {

  private final UUID userId;
  private final CommentLike commentLike;
}
