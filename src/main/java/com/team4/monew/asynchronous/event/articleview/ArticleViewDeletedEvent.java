package com.team4.monew.asynchronous.event.articleview;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ArticleViewDeletedEvent {

  private final UUID userId;
  private final UUID articleId;
}
