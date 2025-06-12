package com.team4.monew.repository;

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

    if (!"likeCount".equals(orderBy) && !"createdAt".equals(orderBy)) {
      throw new MonewException(ErrorCode.INVALID_ORDER_BY);
    }

    Order sortOrder = "desc".equalsIgnoreCase(direction) ? Order.DESC : Order.ASC;

    var query = queryFactory
        .selectFrom(comment)
        .where(comment.article.id.eq(articleId), comment.isDeleted.eq(false))
        .limit(limit);

    if ("likeCount".equals(orderBy)) {
      query.orderBy(
          new OrderSpecifier<>(sortOrder, comment.likeCount),
          new OrderSpecifier<>(sortOrder, comment.createdAt),
          new OrderSpecifier<>(sortOrder, comment.id)
      );

      if (cursor != null && after != null) {
        try {
          Long likeCursor = Long.parseLong(cursor);
          Instant afterTime = Instant.parse(after);

          if (sortOrder == Order.ASC) {
            query.where(
                comment.likeCount.gt(likeCursor)
                    .or(comment.likeCount.eq(likeCursor)
                        .and(comment.createdAt.gt(afterTime)))
            );
          } else {
            query.where(
                comment.likeCount.lt(likeCursor)
                    .or(comment.likeCount.eq(likeCursor)
                        .and(comment.createdAt.lt(afterTime)))
            );
          }
        } catch (NumberFormatException | DateTimeParseException e) {
          throw new MonewException(ErrorCode.INVALID_CURSOR_FORMAT, Map.of("error", e.getMessage()));
        }
      }

    } else {
      query.orderBy(
          new OrderSpecifier<>(sortOrder, comment.createdAt),
          new OrderSpecifier<>(sortOrder, comment.id)
      );

      if (after != null) {
        try {
          Instant afterTime = Instant.parse(after);
          if (sortOrder == Order.ASC) {
            query.where(comment.createdAt.gt(afterTime));
          } else {
            query.where(comment.createdAt.lt(afterTime));
          }
        } catch (DateTimeParseException e) {
          throw new MonewException(ErrorCode.INVALID_AFTER_FORMAT, Map.of("error", e.getMessage()));
        }
      }
    }

    return query.fetch();
  }
}
