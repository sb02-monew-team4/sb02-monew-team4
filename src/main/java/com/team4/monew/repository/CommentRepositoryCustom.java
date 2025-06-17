package com.team4.monew.repository;

import com.team4.monew.entity.Comment;
import java.util.List;
import java.util.UUID;

public interface CommentRepositoryCustom {
  List<Comment> findCommentsByArticleWithCursorPaging(
      UUID articleId,
      String orderBy,
      String direction,
      String cursor,
      String after,
      int limit
  );
}

