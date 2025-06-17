package com.team4.monew.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team4.monew.dto.article.ArticleDto;
import com.team4.monew.dto.article.CursorPageResponseArticleDto;
import com.team4.monew.entity.QArticle;
import com.team4.monew.entity.QInterest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ArticleRepositoryImpl implements ArticleRepositoryCustom {

  private static final QArticle article = QArticle.article;
  private static final QInterest interest = QInterest.interest;
  private final JPAQueryFactory queryFactory;
  private final ArticleViewRepository articleViewRepository;

  @Override
  public CursorPageResponseArticleDto findArticlesWithCursor(
      String keyword,
      UUID interestId,
      List<String> sourceIn,
      Instant publishDateFrom,
      Instant publishDateTo,
      String orderBy,
      String direction,
      String cursor,
      int limit,
      Instant after,
      UUID userId) {

    // 동적 쿼리 실행
    List<ArticleDto> articles = queryFactory
        .select(Projections.constructor(ArticleDto.class,
            article.id,
            article.source,
            article.originalLink,
            article.title,
            article.publishedDate,
            article.summary,
            article.commentCount,
            article.viewCount,
            Expressions.constant(false)
        ))
        .from(article)
        .leftJoin(article.interest, interest)
        .where(buildConditions(keyword,
            interestId,
            sourceIn,
            publishDateFrom,
            publishDateTo,
            cursor,
            after,
            orderBy,
            direction))
        .orderBy(getOrderSpecifier(orderBy, direction))
        .limit(limit + 1)
        .fetch();

    // hasNext 확인
    boolean hasNext = articles.size() > limit;
    if (hasNext) {
      articles = articles.subList(0, limit);
    }

    // 다음 커서 및 after 생성
    String nextCursor = null;
    Instant nextAfter = null;
    if (hasNext && !articles.isEmpty()) {
      ArticleDto lastArticle = articles.get(articles.size() - 1);
      nextCursor = generateCursor(lastArticle, orderBy);
      nextAfter = lastArticle.publishDate();
    }

    // 전체 개수 조회
    long totalElements = countArticlesWithConditions(
        keyword, interestId, sourceIn, publishDateFrom, publishDateTo);

    return new CursorPageResponseArticleDto(
        articles,
        nextCursor,
        nextAfter,
        articles.size(),
        totalElements,
        hasNext
    );
  }

  private List<ArticleDto> setViewedByMe(List<ArticleDto> articles, UUID userId) {
    if (articles.isEmpty() || userId == null) {
      return articles;
    }

    List<UUID> articleIds = articles.stream()
        .map(ArticleDto::id)
        .toList();

    Set<UUID> viewedArticleIds = articleViewRepository
        .findViewedArticleIdsByUserIdAndArticleIds(userId, articleIds);

    return articles.stream()
        .map(dto -> new ArticleDto(
            dto.id(),
            dto.source(),
            dto.sourceUrl(),
            dto.title(),
            dto.publishDate(),
            dto.summary(),
            dto.commentCount(),
            dto.viewCount(),
            viewedArticleIds.contains(dto.id())
        ))
        .toList();
  }

  private BooleanBuilder buildConditions(
      String keyword,
      UUID interestId,
      List<String> sourceIn,
      Instant publishDateFrom,
      Instant publishDateTo,
      String cursor,
      Instant after,
      String orderBy,
      String direction) {

    BooleanBuilder builder = new BooleanBuilder();

    // 기본 조건
    builder.and(isNotDeleted());

    // 선택적 조건들
    addConditionIfNotNull(builder, keywordCondition(keyword));
    addConditionIfNotNull(builder, interestCondition(interestId));
    addConditionIfNotNull(builder, sourceCondition(sourceIn));
    addConditionIfNotNull(builder, publishDateFromCondition(publishDateFrom));
    addConditionIfNotNull(builder, publishDateToCondition(publishDateTo));

    // 커서 또는 after 조건
    BooleanExpression cursorExp = cursorCondition(cursor, orderBy, direction);
    if (cursorExp != null) {
      builder.and(cursorExp);
    } else {
      addConditionIfNotNull(builder, afterCondition(after));
    }

    return builder;
  }

  private void addConditionIfNotNull(BooleanBuilder builder, BooleanExpression condition) {
    if (condition != null) {
      builder.and(condition);
    }
  }

  // BooleanExpression 조건 메서드들
  private BooleanExpression isNotDeleted() {
    return article.isDeleted.eq(false);
  }

  private BooleanExpression keywordCondition(String keyword) {
    if (!StringUtils.hasText(keyword)) {
      return null;
    }
    return article.title.containsIgnoreCase(keyword)
        .or(article.summary.containsIgnoreCase(keyword));
  }

  private BooleanExpression interestCondition(UUID interestId) {
    return interestId != null ? interest.id.eq(interestId) : null;
  }

  private BooleanExpression sourceCondition(List<String> sourceIn) {
    return (sourceIn != null && !sourceIn.isEmpty()) ?
        article.source.in(sourceIn) : null;
  }

  private BooleanExpression publishDateFromCondition(Instant publishDateFrom) {
    return publishDateFrom != null ?
        article.publishedDate.goe(publishDateFrom) : null;
  }

  private BooleanExpression publishDateToCondition(Instant publishDateTo) {
    return publishDateTo != null ?
        article.publishedDate.loe(publishDateTo) : null;
  }

  private BooleanExpression afterCondition(Instant after) {
    return after != null ? article.publishedDate.gt(after) : null;
  }

  private BooleanExpression cursorCondition(String cursor, String orderBy, String direction) {
    if (!StringUtils.hasText(cursor)) {
      return null;
    }

    try {
      boolean desc = "DESC".equalsIgnoreCase(direction);

      return switch (orderBy) {
        case "commentCount" -> {
          long cursorVal = Long.parseLong(cursor);
          yield desc
              ? article.commentCount.lt(cursorVal)
              : article.commentCount.gt(cursorVal);
        }
        case "viewCount" -> {
          long cursorVal = Long.parseLong(cursor);
          yield desc
              ? article.viewCount.lt(cursorVal)
              : article.viewCount.gt(cursorVal);
        }
        default -> {
          LocalDateTime cursorDateTime = LocalDateTime.parse(cursor);
          yield desc
              ? article.publishedDate.lt(Instant.from(cursorDateTime))
              : article.publishedDate.gt(Instant.from(cursorDateTime));
        }
      };
    } catch (DateTimeParseException | NumberFormatException e) {
      log.warn("커서 파싱 실패: cursor={}, orderBy={}", cursor, orderBy, e);
      return null;
    }
  }

  private OrderSpecifier<?> getOrderSpecifier(String orderBy, String direction) {
    boolean isDesc = "DESC".equalsIgnoreCase(direction);

    return switch (orderBy) {
      case "commentCount" -> isDesc ?
          article.commentCount.desc() : article.commentCount.asc();
      case "viewCount" -> isDesc ?
          article.viewCount.desc() : article.viewCount.asc();
      case "publishDate" -> isDesc ?
          article.publishedDate.desc() : article.publishedDate.asc();
      default -> article.publishedDate.desc(); // 기본값
    };
  }

  private String generateCursor(ArticleDto articleDto, String orderBy) {
    return switch (orderBy) {
      case "commentCount" -> String.valueOf(articleDto.commentCount());
      case "viewCount" -> String.valueOf(articleDto.viewCount());
      case "publishDate" -> articleDto.publishDate().toString();
      default -> articleDto.publishDate().toString();
    };
  }

  @Override
  public long countArticlesWithConditions(
      String keyword,
      UUID interestId,
      List<String> sourceIn,
      Instant publishDateFrom,
      Instant publishDateTo) {

    Long count = queryFactory
        .select(article.count())
        .from(article)
        .leftJoin(article.interest, interest)
        .where(
            isNotDeleted(),
            keywordCondition(keyword),
            interestCondition(interestId),
            sourceCondition(sourceIn),
            publishDateFromCondition(publishDateFrom),
            publishDateToCondition(publishDateTo)
        )
        .fetchOne();

    return count != null ? count : 0L;
  }
}
