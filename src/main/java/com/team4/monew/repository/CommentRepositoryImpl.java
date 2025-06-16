package com.team4.monew.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.team4.monew.entity.Comment;
import com.team4.monew.entity.QComment;
import com.team4.monew.exception.ErrorCode;
import com.team4.monew.exception.MonewException;
import jakarta.persistence.EntityManager;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CommentRepositoryImpl implements CommentRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  public CommentRepositoryImpl(EntityManager em) {
    this.queryFactory = new JPAQueryFactory(em);
  }

  @Override
  public List<Comment> findCommentsByArticleWithCursorPaging(
      UUID articleId,
      String orderBy,
      String direction,
      String cursor,
      String after,
      int limit
  ) {

    if (!"likeCount".equals(orderBy) && !"createdAt".equals(orderBy)) {
      throw new MonewException(ErrorCode.INVALID_ORDER_BY);
    }

    if (!"ASC".equalsIgnoreCase(direction) && !"DESC".equalsIgnoreCase(direction)) {
      throw new MonewException(ErrorCode.INVALID_SORT_DIRECTION);
    }

    if (limit < 1 || limit > 100) {
      throw new MonewException(ErrorCode.INVALID_LIMIT);
    }

    QComment comment = QComment.comment;

    Order sortOrder = "desc".equalsIgnoreCase(direction) ? Order.DESC : Order.ASC;

    BooleanExpression baseCondition = comment.article.id.eq(articleId)
        .and(comment.isDeleted.eq(false));

    BooleanExpression cursorCondition = null;

    try {
      if ("likeCount".equals(orderBy)) {
        if (cursor != null && after != null) {
          Long likeCursor = Long.parseLong(cursor);
          Instant afterTime = Instant.parse(after);
          if (sortOrder == Order.ASC) {
            cursorCondition = comment.likeCount.gt(likeCursor)
                .or(comment.likeCount.eq(likeCursor).and(comment.createdAt.gt(afterTime)));
          } else {
            cursorCondition = comment.likeCount.lt(likeCursor)
                .or(comment.likeCount.eq(likeCursor).and(comment.createdAt.lt(afterTime)));
          }
        }
      } else {
        if (after != null) {
          Instant afterTime = Instant.parse(after);
          if (sortOrder == Order.ASC) {
            cursorCondition = comment.createdAt.gt(afterTime);
          } else {
            cursorCondition = comment.createdAt.lt(afterTime);
          }
        }
      }
    } catch (NumberFormatException | DateTimeParseException e) {
      throw new MonewException(ErrorCode.INVALID_CURSOR_FORMAT, Map.of("error", e.getMessage()));
    }

    if (cursorCondition != null) {
      baseCondition = baseCondition.and(cursorCondition);
    }

    var query = queryFactory.selectFrom(comment)
        .where(baseCondition)
        .orderBy(
            "likeCount".equals(orderBy)
                ? new OrderSpecifier<>(sortOrder, comment.likeCount)
                : new OrderSpecifier<>(sortOrder, comment.createdAt),
            new OrderSpecifier<>(sortOrder, comment.createdAt),
            new OrderSpecifier<>(sortOrder, comment.id)
        )
        .limit(limit);

    return query.fetch();
  }
}