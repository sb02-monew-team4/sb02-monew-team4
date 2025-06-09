package com.team4.monew.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.team4.monew.entity.Notification;
import com.team4.monew.entity.QNotification;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NotificationRepositoryCustomImpl implements NotificationRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<Notification> findUnconfirmedByCursor(
      Instant cursor, Instant after, int limit, UUID userId
  ) {
    QNotification notification = QNotification.notification;

    BooleanBuilder whereCondition = new BooleanBuilder();
    whereCondition.and(notification.user.id.eq(userId));
    whereCondition.and(notification.confirmed.isFalse());
    if (after != null) {
      whereCondition.and(notification.createdAt.goe(after));
    }
    if (cursor != null) {
      whereCondition.and(notification.createdAt.gt(cursor));
    }

    return queryFactory
        .selectFrom(notification)
        .where(whereCondition)
        .orderBy(notification.createdAt.asc())
        .limit(limit)
        .fetch();
  }


}
