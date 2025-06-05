package com.team4.monew.repository;

import com.team4.monew.entity.ArticleView;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleViewRepository extends JpaRepository<ArticleView, UUID> {

}
