package com.team4.monew.asynchronous.event.comment;

import com.team4.monew.entity.Comment;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommentCreatedEvent {

  private final UUID userId;
  private final Comment comment;
}
