package com.team4.monew.repository;

import com.team4.monew.entity.Interest;
import java.util.List;

public interface InterestRepositoryCustom {
  List<Interest> findInterestsWithCursorPaging(
      String keyword,
      String orderBy,
      String direction,
      String cursor,
      String after,
      int limit
  );

  long countByKeyword(String keyword);
}