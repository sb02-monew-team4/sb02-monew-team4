package com.team4.monew.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team4.monew.dto.article.ArticleDto;
import com.team4.monew.dto.article.ArticleSearchRequest;
import com.team4.monew.dto.article.CursorPageResponseArticleDto;
import com.team4.monew.entity.QArticle;
import com.team4.monew.entity.QInterest;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@RequiredArgsConstructor
public class ArticleRepositoryImpl implements ArticleRepositoryCustom {

  private static final QArticle article = QArticle.article;
  private static final QInterest interest = QInterest.interest;
  private final JPAQueryFactory queryFactory;
  private final ArticleViewRepository articleViewRepository;

  @Override
  public CursorPageResponseArticleDto findArticlesWithCursor(
      ArticleSearchRequest request,
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
        .where(
            buildConditions(
                request.keyword(),
                request.interestId(),
                request.sourceIn(),
                request.publishDateFrom(),
                request.publishDateTo(),
                request.cursor(),
                request.after(),
                request.orderBy(),
                request.direction())
        )
        .orderBy(getOrderSpecifier(request.orderBy(), request.direction()))
        .limit(request.limit() + 1)
        .fetch();

    // hasNext 확인 및 viewedByMe 설정
    boolean hasNext = articles.size() > request.limit();
    if (hasNext) {
      articles = articles.subList(0, request.limit());
    }

    // 사용자 조회 이력 확인
    if (!articles.isEmpty()) {
      List<UUID> articleIds = articles.stream()
          .map(ArticleDto::id)
          .toList();

      Set<UUID> viewedArticleIds = articleViewRepository
          .findViewedArticleIdsByUserIdAndArticleIds(userId, articleIds);

      articles = articles.stream()
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

    // 다음 커서 생성
    String nextCursor = null;
    Instant nextAfter = null;
    if (hasNext && !articles.isEmpty()) {
      ArticleDto lastArticle = articles.get(articles.size() - 1);
      nextCursor = generateCursor(lastArticle, request.orderBy());
      nextAfter = Instant.now();
    }

    // 전체 개수 조회
    long totalElements = countArticlesWithConditions(
        request.keyword(),
        request.interestId(),
        request.sourceIn(),
        request.publishDateFrom(),
        request.publishDateTo());

    return new CursorPageResponseArticleDto(
        articles,
        nextCursor,
        nextAfter,
        articles.size(),
        totalElements,
        hasNext
    );
  }

  // BooleanExpression을 활용한 조건 메서드들
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

  private BooleanExpression cursorCondition(String cursor, String orderBy, String direction) {
    if (!StringUtils.hasText(cursor)) {
      return null;
    }

    try {
      switch (orderBy) {
        case "publishDate":
          Instant cursorDate = Instant.parse(cursor);
          return "DESC".equalsIgnoreCase(direction)
              ? article.publishedDate.lt(cursorDate)
              : article.publishedDate.gt(cursorDate);

        case "commentCount":
          Long cursorCommentCount = Long.parseLong(cursor);
          return "DESC".equalsIgnoreCase(direction)
              ? article.commentCount.lt(cursorCommentCount)
              : article.commentCount.gt(cursorCommentCount);

        case "viewCount":
          Long cursorViewCount = Long.parseLong(cursor);
          return "DESC".equalsIgnoreCase(direction)
              ? article.viewCount.lt(cursorViewCount)
              : article.viewCount.gt(cursorViewCount);

        default:
          return null;
      }
    } catch (Exception e) {
      return null;
    }
  }

  // 동적 조건 빌더
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
    builder.and(isNotDeleted());

    if (keywordCondition(keyword) != null) {
      builder.and(keywordCondition(keyword));
    }

    if (interestCondition(interestId) != null) {
      builder.and(interestCondition(interestId));
    }

    if (sourceCondition(sourceIn) != null) {
      builder.and(sourceCondition(sourceIn));
    }

    if (publishDateFromCondition(publishDateFrom) != null) {
      builder.and(publishDateFromCondition(publishDateFrom));
    }

    if (publishDateToCondition(publishDateTo) != null) {
      builder.and(publishDateToCondition(publishDateTo));
    }

    if (cursorCondition(cursor, orderBy, direction) != null) {
      builder.and(cursorCondition(cursor, orderBy, direction));
    }

    return builder;
  }

  // 정렬 조건 생성
  private OrderSpecifier<?> getOrderSpecifier(String orderBy, String direction) {
    boolean isDesc = "DESC".equalsIgnoreCase(direction);

    switch (orderBy) {
      case "commentCount":
        return isDesc ? article.commentCount.desc() : article.commentCount.asc();
      case "viewCount":
        return isDesc ? article.viewCount.desc() : article.viewCount.asc();
      case "publishDate":
      default:
        return isDesc ? article.publishedDate.desc() : article.publishedDate.asc();
    }
  }

  // 커서 생성
  private String generateCursor(ArticleDto articleDto, String orderBy) {
    switch (orderBy) {
      case "commentCount":
        return String.valueOf(articleDto.commentCount());
      case "viewCount":
        return String.valueOf(articleDto.viewCount());
      case "publishDate":
      default:
        return articleDto.publishDate().toString();
    }
  }

  @Override
  public long countArticlesWithConditions(
      String keyword,
      UUID interestId,
      List<String> sourceIn,
      Instant publishDateFrom,
      Instant publishDateTo) {

    Long nullableCount = queryFactory
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

    return nullableCount != null ? nullableCount : 0L;
  }
}
