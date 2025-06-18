package com.team4.monew.repository;

import com.team4.monew.entity.ArticleView;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticleViewRepository extends JpaRepository<ArticleView, UUID> {

  boolean existsByArticleIdAndUserId(UUID articleId, UUID userId);

  @Query("SELECT av.article.id FROM ArticleView av WHERE av.user.id = :userId "
      + "AND av.article.id IN :articleIds")
  Set<UUID> findViewedArticleIdsByUserIdAndArticleIds(
      @Param("userId") UUID userid,
      @Param("articleIds") List<UUID> articleIds);

  List<ArticleView> findTop10ByUserIdOrderByViewedAtDesc(UUID userId);

  Optional<ArticleView> findByArticleIdAndUserId(UUID articleId, UUID userId);
}
