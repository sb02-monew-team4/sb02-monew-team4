package com.team4.monew.repository;

import com.team4.monew.entity.Article;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, UUID> {

  boolean existsByOriginalLink(String originalLink);
}
