package com.team4.monew.repository;

import com.team4.monew.entity.Interest;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InterestRepository extends JpaRepository<Interest, UUID>, InterestRepositoryCustom {

  long countByKeyword(String keyword);

  // keywords 까지 즉시 로딩, N+1 문제 방지
  @EntityGraph(attributePaths = "keywords")
  @Query("SELECT i FROM Interest i")
  List<Interest> findAllWithKeywords();

  @Query("SELECT DISTINCT i FROM Interest i JOIN i.keywords k "
      + "WHERE LOWER(k.keyword) = LOWER(:keyowrd)")
  List<Interest> findByKeywordContaining(String keyword);
}