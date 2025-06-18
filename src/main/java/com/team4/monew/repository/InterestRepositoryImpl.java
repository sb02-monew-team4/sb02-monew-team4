package com.team4.monew.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpression;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team4.monew.entity.Interest;
import com.team4.monew.entity.QInterest;
import com.team4.monew.entity.QInterestKeyword;
import com.team4.monew.exception.ErrorCode;
import com.team4.monew.exception.MonewException;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

public class InterestRepositoryImpl implements InterestRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  public InterestRepositoryImpl(EntityManager em) {
    this.queryFactory = new JPAQueryFactory(em);
  }

  @Override
  public List<Interest> findInterestsWithCursorPaging(
      String keyword,
      String orderBy,
      String direction,
      String cursor,
      LocalDateTime after,
      int limit
  ) {

    if (!"name".equals(orderBy) && !"subscriberCount".equals(orderBy)) {
      throw new MonewException(ErrorCode.INVALID_ORDER_BY);
    }
    if (!"ASC".equalsIgnoreCase(direction) && !"DESC".equalsIgnoreCase(direction)) {
      throw new MonewException(ErrorCode.INVALID_SORT_DIRECTION);
    }
    if (limit < 1 || limit > 100) {
      throw new MonewException(ErrorCode.INVALID_LIMIT);
    }

    QInterest interest = QInterest.interest;
    QInterestKeyword interestKeyword = QInterestKeyword.interestKeyword;

    BooleanExpression keywordFilter = null;
    if (keyword != null && !keyword.isBlank()) {
      keywordFilter = interest.name.containsIgnoreCase(keyword)
          .or(interestKeyword.keyword.containsIgnoreCase(keyword));
    }

    BooleanExpression cursorPredicate = buildCursorPredicate(interest, orderBy, direction, cursor, after);

    JPQLQuery<Interest> query = queryFactory
        .selectFrom(interest)
        .distinct()
        .leftJoin(interest.keywords, interestKeyword)
        .where(keywordFilter, cursorPredicate)
        .orderBy(getOrderSpecifiers(interest, orderBy, direction))
        .limit(limit);

    return query.fetch();
  }

  private BooleanExpression buildCursorPredicate(QInterest interest, String orderBy, String direction, String cursor, LocalDateTime after) {
    if (cursor == null || after == null) return null;

    ComparableExpression<?> cursorField;
    BooleanExpression predicate = null;
    boolean isAsc = direction.equalsIgnoreCase("ASC");

    switch (orderBy) {
      case "name" -> {
        cursorField = interest.name;
        if (isAsc) {
          predicate = interest.name.gt(cursor).or(interest.name.eq(cursor).and(interest.createdAt.gt(after)));
        } else {
          predicate = interest.name.lt(cursor).or(interest.name.eq(cursor).and(interest.createdAt.lt(after)));
        }
      }
      case "subscriberCount" -> {
        NumberPath<Long> subscriberCount = interest.subscriberCount;
        Long cursorValue = Long.valueOf(cursor);
        if (isAsc) {
          predicate = subscriberCount.gt(cursorValue).or(subscriberCount.eq(cursorValue).and(interest.createdAt.gt(after)));
        } else {
          predicate = subscriberCount.lt(cursorValue).or(subscriberCount.eq(cursorValue).and(interest.createdAt.lt(after)));
        }
      }
    }

    return predicate;
  }

  private OrderSpecifier<?>[] getOrderSpecifiers(QInterest interest, String orderBy, String direction) {
    boolean isAsc = direction.equalsIgnoreCase("ASC");

    switch (orderBy) {
      case "name":
        return new OrderSpecifier[]{
            isAsc ? interest.name.asc() : interest.name.desc(),
            isAsc ? interest.createdAt.asc() : interest.createdAt.desc()
        };
      case "subscriberCount":
        return new OrderSpecifier[]{
            isAsc ? interest.subscriberCount.asc() : interest.subscriberCount.desc(),
            isAsc ? interest.createdAt.asc() : interest.createdAt.desc()
        };
      default:
        throw new MonewException(ErrorCode.INVALID_ORDER_BY);
    }
  }

  @Override
  public long countByKeyword(String keyword) {
    QInterest interest = QInterest.interest;
    QInterestKeyword interestKeyword = QInterestKeyword.interestKeyword;

    BooleanExpression predicate = null;
    if (keyword != null && !keyword.isBlank()) {
      predicate = interest.name.containsIgnoreCase(keyword)
          .or(interestKeyword.keyword.containsIgnoreCase(keyword));
    }

    JPQLQuery<Long> query = queryFactory
        .select(interest.countDistinct())
        .from(interest)
        .leftJoin(interest.keywords, interestKeyword);

    if (predicate != null) {
      query.where(predicate);
    }

    Long result = query.fetchOne();
    return result != null ? result : 0L;
  }
}