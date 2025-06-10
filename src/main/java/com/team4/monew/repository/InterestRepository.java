package com.team4.monew.repository;

import com.team4.monew.entity.Interest;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterestRepository extends JpaRepository<Interest, UUID> {
  List<Interest> findByKeywordWithCursorPaging(
      String keyword,
      String orderBy,
      String direction,
      UUID cursor,
      Instant after,
      int limit
  );

  long countByKeyword(String keyword);
}
