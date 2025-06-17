package com.team4.monew.asynchronous.event.articleview;

import com.team4.monew.entity.ArticleView;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ArticleViewCreatedEvent {

  private final UUID userId;
  private final ArticleView articleView;

}
